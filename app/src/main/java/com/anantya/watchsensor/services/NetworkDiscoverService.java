package com.anantya.watchsensor.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;


public class NetworkDiscoverService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_LISTEN = "NetworkDiscoverService.action.listen";


    private static final int BROADCAST_DISCOVERY_PORT = 2000;
    private static final int MAX_DATA_SIZE = 1024;
    private static final int SOCKET_RECEIVE_TIMEOUT = 1000 * 10;
    private static final String TAG = "NetworkDiscoverService";


    public NetworkDiscoverService() {
        super("NetworkDiscoverService");
    }

    private boolean mIsRunning;

    public static void requestListen(Context context) {
        Intent intent = new Intent(context, NetworkDiscoverService.class);
        intent.setAction(ACTION_LISTEN);
        context.startService(intent);
    }

    public static void stopListen(Context context) {
        Intent intent = new Intent(context, NetworkDiscoverService.class);
        context.stopService(intent);

    }


    @Override
    public void onDestroy() {
        mIsRunning = false;
        super.onDestroy();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_LISTEN.equals(action)) {
                networkListen();
            }
        }
    }

    private void networkListen() {

        try {

            DatagramSocket socket = new DatagramSocket(BROADCAST_DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            // make it non blocking so that we can stop the service
            socket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT);
            mIsRunning = true;
            while ( mIsRunning ) {
                try {
//                    Log.d(TAG, "Ready to get broadcast messages");
                    byte[] buffer = new byte[MAX_DATA_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

//                    Log.d(TAG, "received " + packet.toString());
                    String message = new String(packet.getData());
                    StringReader stringReader = new StringReader(message);
                    JsonReader reader = new JsonReader(stringReader);
                    reader.beginObject();
                    while ( reader.hasNext()) {
                        String name = reader.nextName();
                        String value = reader.nextString();
                        if ( name.equals("type") && value.equals("SCS-DISCOVER")) {
//                            Log.d(TAG, "Discovery made");
                            sendDiscoveryMessage(socket, packet.getAddress());
                        }

                    }
                    reader.endObject();
                } catch (SocketException e) {
                } catch (JSONException e) {
                } catch (IOException e) {
                }
            }
        } catch (UnknownHostException e) {
        } catch (SocketException e) {
        }
    }
    protected void sendDiscoveryMessage(DatagramSocket socket, InetAddress toAddress) throws UnknownHostException, JSONException {

        // now get the local ip address and mac address, assuming that all network traffic goes through the wifi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String macAddress = wifiInfo.getMacAddress();
        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
                Integer.reverseBytes(ipAddress) : ipAddress;
        byte[] byteAddress = BigInteger.valueOf(ipAddress).toByteArray();
        InetAddress inetAddress = InetAddress.getByAddress(byteAddress);
        String localAddress = inetAddress.getHostAddress();

        JSONObject object = new JSONObject();
        object.put("type", "SCS-NOTIFY");
        object.put("ip",  localAddress);
        object.put("mac", macAddress);
        String sendMessage = object.toString();
        Log.d(TAG, sendMessage);
        DatagramPacket sendPacket = new DatagramPacket(sendMessage.getBytes(), sendMessage.length(), toAddress, BROADCAST_DISCOVERY_PORT );
        //                        socket.send(sendPacket);

    }
}
