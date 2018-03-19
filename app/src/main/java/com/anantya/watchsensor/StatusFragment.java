package com.anantya.watchsensor;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anantya.watchsensor.data.EventDataStatItem;

import java.util.Locale;


public class StatusFragment extends Fragment {


    private TextView mTextViewTotalStats;
    private TextView mTextViewUploadWait;
    private TextView mTextViewUploadWaitTitle;
    private TextView mTextViewUploadProcessing;
    private TextView mTextViewUploadDone;
    private ProgressBar mProgressBar;
    private TextView mTextViewPercentDone;
    private TextView mBatteryStatus;
    private TextView mWifiStatus;
    private TextView mTextViewInformation;


    private OnFragmentInteractionListener mListener;

    public StatusFragment() {
        // Required empty public constructor
    }


    public static StatusFragment newInstance() {
        StatusFragment fragment = new StatusFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        mWifiStatus = (TextView) view.findViewById(R.id.textViewWifiStatus);
        mBatteryStatus = (TextView) view.findViewById(R.id.textViewBatteryStatus);
        mTextViewTotalStats = ( TextView ) view.findViewById(R.id.textViewTotalStats);
        mTextViewUploadWaitTitle = (TextView ) view.findViewById(R.id.textViewWaitingTitle);
        mTextViewUploadWait = ( TextView ) view.findViewById(R.id.textViewUploadWait);
        mTextViewUploadProcessing = ( TextView ) view.findViewById(R.id.textViewUploadProcessing);
        mTextViewUploadDone = ( TextView ) view.findViewById(R.id.textViewUploadDone);
        mTextViewPercentDone = ( TextView ) view.findViewById(R.id.textViewPercentDone);
        mTextViewInformation = ( TextView ) view.findViewById(R.id.textViewInformation);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        // hide the total line
        view.findViewById(R.id.tableRowTotal).setVisibility(View.GONE);
        // hide the information line
        view.findViewById(R.id.tableRowInformation).setVisibility(View.GONE);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setValues(EventDataStatItem item, String wifiStatus, String batteryStatus) {

        mWifiStatus.setText(wifiStatus);;
        mBatteryStatus.setText(batteryStatus);
        mTextViewTotalStats.setText( String.format(Locale.UK, "%,d", item.getTotal()));
        mTextViewUploadWait.setText(String.format(Locale.UK, "%,d", item.getUploadWait()));
        mTextViewUploadProcessing.setText(String.format(Locale.UK, "%,d", item.getUploadProcessing()));
        mTextViewUploadDone.setText(String.format(Locale.UK, "%,d", item.getUploadDone()));
        mProgressBar.setMax((int) item.getTotal());
        mProgressBar.setProgress((int) item.getUploadDone());
        double total = item.getTotal();
        double done = item.getUploadDone();
        double percentDone = 0;
        if ( total > 0.0 && done > 0.0 ) {

            percentDone = ( done / total ) * 100;
        }
        mTextViewPercentDone.setText(String.format(Locale.UK, "%.0f%%", percentDone));
    }


    public void setIsPowered(boolean value) {
        mTextViewPercentDone.setVisibility(value ? View.VISIBLE : View.GONE);
        mProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        View view = getView();
        if ( view != null ) {
            view.findViewById(R.id.tableRowRetry).setVisibility(value ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.tableRowUpload).setVisibility(value ? View.VISIBLE : View.GONE);
        }

        if ( value ) {
            mTextViewUploadWaitTitle.setText(R.string.status_fragment_waiting);
        }
        else {
            mTextViewUploadWaitTitle.setText(R.string.status_fragment_recorded);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
