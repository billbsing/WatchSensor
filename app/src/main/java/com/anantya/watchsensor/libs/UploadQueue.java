package com.anantya.watchsensor.libs;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.data.EventDataItem;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.services.EventDataCacheService;
import com.anantya.watchsensor.services.UploadService;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by bill on 10/17/17.
 */

public class UploadQueue implements RequestQueue.RequestFinishedListener<StringRequest> {

    private static UploadQueue mInstance;
    private static final long SEND_DATA_TIMEOUT = DateUtils.MINUTE_IN_MILLIS;
    private static final long SAS_TOKEN_CACHE_TIMEOUT = DateUtils.HOUR_IN_MILLIS * 2;       // time to refresh the SAS token
    private static final String TAG = "UploadQueue";


    private RequestQueue mRequestQueue;
    private Context mContext;
    private String mSASToken;
    private long mSASTokenExpireTime;
    private int mQueueSize;

    private UploadQueue(Context context)   {
        mContext = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(context);
        mRequestQueue.addRequestFinishedListener(this);
        mSASToken = "";
        mQueueSize = 0;
    }

    public static UploadQueue getInstance(Context context) {
        if ( mInstance == null) {
            mInstance = new UploadQueue(context);
        }
        return mInstance;
    }

    public void sendMessages(ConfigData configData, EventDataList eventDataList) {


        final long[] idList = eventDataList.getIdList();
        final String sasToken = getSASToken(configData);
        final String packageData = generateDataAsJSON(configData.getWatchId(), eventDataList);

//        Log.d(TAG, packageData);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, configData.getFullURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                EventDataCacheService.updateUploadCacheIds(mContext, idList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Upload error " + error.toString());
            }
        }) {
            @Override
            public Map<String,String> getHeaders() {
                Map<String,String> headers = new HashMap<>();
                headers.put("Authorization", sasToken);
//                headers.put("ContentType", "application/atom+xml;type=entry;charset=utf-8");
                headers.put("ContentType", "application/vnd.microsoft.servicebus.json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return packageData.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy((int) SEND_DATA_TIMEOUT, 2, 2));
        mQueueSize ++;
        mRequestQueue.add(stringRequest);
        Log.d(TAG, "Queue size " + mQueueSize);
    }

    public int getQueueSize() {
        return mQueueSize;
    }

    protected String getSASToken(ConfigData configData) {
        if ( mSASToken.isEmpty() || mSASTokenExpireTime < System.currentTimeMillis()) {
            mSASToken = generateSASToken(configData.getURL(), configData.getKeyname(), configData.getPrimanryKey());
            mSASTokenExpireTime = System.currentTimeMillis() + SAS_TOKEN_CACHE_TIMEOUT;
        }
        return mSASToken;
    }

    protected String generateSASToken(String url, String keyName, String primaryKey ) {
        String result = "";
        long timestamp = (System.currentTimeMillis() / 1000L) + (60 * 60  * 48);
        try {
            String signatureSource = URLEncoder.encode(url, "UTF-8") + "\n" + String.valueOf(timestamp);
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(primaryKey.getBytes(), "HmacSHA256");
            sha256HMAC.init(secretKeySpec);
            byte[] digest = sha256HMAC.doFinal(signatureSource.getBytes());
            String signature = Base64.encodeToString(digest, Base64.NO_WRAP);
            result = "SharedAccessSignature ";
            result += "sr=" + URLEncoder.encode(url, "UTF-8");
            result += "&sig=" + URLEncoder.encode(signature, "UTF-8");
            result += "&se=" + String.valueOf(timestamp);
            result += "&skn=" + keyName;
        } catch (NoSuchAlgorithmException e) {
            result = "";
        } catch (UnsupportedEncodingException e) {
            result = "";
        } catch (InvalidKeyException e) {
            result = "";
        }

        return result;
    }

    protected String generateDataAsJSON(String id, EventDataList eventDataList) {

        JSONArray rows = new JSONArray();
        for ( int i= 0 ; i < eventDataList.getItems().size(); i++ )  {
            try {
                JSONObject row = new JSONObject();
                EventDataItem eventDataItem = eventDataList.getItems().get(i);
                row.put("id", id);

                JSONObject data = new JSONObject();

                data.put("event_time", eventDataItem.getEventTimestamp());
                data.put("system_time", eventDataItem.getSystemTimestamp());
                data.put("sensor_name", eventDataItem.getName());
                switch (eventDataItem.getValues().length) {
                    case 1:
                        data.put("value", eventDataItem.getValues()[0] );
                        break;
                    case 2:
                        data.put("x", eventDataItem.getValues()[0] );
                        data.put("y", eventDataItem.getValues()[1] );
                        break;
                    case 3:
                        data.put("x", eventDataItem.getValues()[0] );
                        data.put("y", eventDataItem.getValues()[1] );
                        data.put("z", eventDataItem.getValues()[2] );
                        break;
                    case 4:
                        data.put("x", eventDataItem.getValues()[0] );
                        data.put("y", eventDataItem.getValues()[1] );
                        data.put("z", eventDataItem.getValues()[2] );
                        data.put("a", eventDataItem.getValues()[3] );
                        break;
                }
                row.put("data", data);
                rows.put(row);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rows.toString();
    }

    protected String generateDataAsCSV(String id, EventDataList eventDataList) {
        StringBuffer buffer = new StringBuffer();
        for ( int i = 0; i < eventDataList.getItems().size(); i ++ ) {
            EventDataItem eventDataItem = eventDataList.getItems().get(i);
            buffer.append(id);
            buffer.append(",");
            buffer.append(eventDataItem.getEventTimestamp());
            buffer.append(",");
            buffer.append(eventDataItem.getSystemTimestamp());
            buffer.append(",");
            buffer.append(eventDataItem.getName());
            for ( int valueIndex = 0; valueIndex < eventDataItem.getValues().length; valueIndex ++) {
                buffer.append(",");
                buffer.append(eventDataItem.getValues()[valueIndex]);
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    @Override
    public void onRequestFinished(Request<StringRequest> request) {
        mQueueSize --;
        Log.d(TAG, "Queue size " + mQueueSize);
    }
}
