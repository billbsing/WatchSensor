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
import com.anantya.watchsensor.data.StatusData;

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
    private TextView mTextViewMovement;

    private StatusData mStatusData;

    private final String PARAM_STATUS_DATA = "StatusFragment.status_data";

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

        mStatusData = new StatusData();
        if ( savedInstanceState != null) {
            mStatusData = savedInstanceState.getParcelable(PARAM_STATUS_DATA);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(PARAM_STATUS_DATA, mStatusData);
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
        mTextViewMovement = ( TextView ) view.findViewById(R.id.textViewMovement);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);


        fillViews(mStatusData);
        setUploadingView(mStatusData.isUploading());

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

    @Override
    public void onResume() {
        super.onResume();
        fillViews(mStatusData);
        setUploadingView(mStatusData.isUploading());
    }

    protected void fillViews(StatusData statusData) {

        mWifiStatus.setText(statusData.getWifiStatus());
        mBatteryStatus.setText(statusData.getBatteryStatus());
        mTextViewTotalStats.setText( String.format(Locale.UK, "%,d", statusData.getUploadTotalCount()));
        mTextViewUploadWait.setText(String.format(Locale.UK, "%,d", statusData.getUploadWaitCount()));
        mTextViewUploadProcessing.setText(String.format(Locale.UK, "%,d", statusData.getUploadProcessCount()));
        mTextViewUploadDone.setText(String.format(Locale.UK, "%,d", statusData.getUploadDoneCount()));
        mTextViewMovement.setText(String.format(Locale.UK, "%.1f", statusData.getMovementRate()));
        mProgressBar.setMax((int) statusData.getUploadTotalCount());
        mProgressBar.setProgress((int) statusData.getUploadDoneCount());
        mTextViewPercentDone.setText(String.format(Locale.UK, "%.0f%%", statusData.getPerecentUploaded()));
    }

    protected void setUploadingView(boolean value) {
        mTextViewPercentDone.setVisibility(value ? View.VISIBLE : View.GONE);
        mProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        View view = getView();
        if ( view != null ) {
            view.findViewById(R.id.tableRowRetry).setVisibility(value ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.tableRowUpload).setVisibility(value ? View.VISIBLE : View.GONE);
//            view.findViewById(R.id.tableRowInformation).setVisibility(value ? View.GONE : View.VISIBLE);
            view.findViewById(R.id.tableRowInformation).setVisibility( View.GONE);

            // hide the total line
            view.findViewById(R.id.tableRowTotal).setVisibility(View.GONE);
            // hide the information line
            view.findViewById(R.id.tableRowInformation).setVisibility(View.GONE);

            // hide the movement line
            view.findViewById(R.id.tableRowMovement).setVisibility(View.GONE);

        }

        if ( value ) {
            mTextViewUploadWaitTitle.setText(R.string.status_fragment_waiting);
        }
        else {
            mTextViewUploadWaitTitle.setText(R.string.status_fragment_recorded);
        }


    }

    public void setUploadingVisible(boolean value) {
        mStatusData.setUploading(value);
        setUploadingView(value);
    }

    public void setBatteryStatus(String value) {
        mStatusData.setBatterStatus(value);
        fillViews(mStatusData);
    }
    public void setWifiStatus(String value) {
        mStatusData.setWifiStatus(value);
        fillViews(mStatusData);
    }

    public void assignEventDataStat(EventDataStatItem item) {
        mStatusData.assignEventDataStat(item);
        fillViews(mStatusData);
    }

    public void setMovementRate(float value) {
        mStatusData.setMovementRate(value);
        fillViews(mStatusData);
    }

    public void setInformation(String text) {
        mTextViewInformation.setText(text);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
