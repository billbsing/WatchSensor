package com.anantya.watchsensor;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.anantya.watchsensor.data.ConfigData;


public class SettingsFragment extends Fragment {

    private static final String PARAM_CONFIG_DATA = "StatusFragment.config_data";

    private ConfigData mConfigData;
    private EditText mEditTextWatchId;
    private CheckBox mCheckboxTrackingEnabled;
    private CheckBox mCheckboxHeartRateActive;
    private CheckBox mCheckboxGPSActive;

    private OnSettingsFragmentListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(ConfigData configData) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(PARAM_CONFIG_DATA, configData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null) {
            mConfigData = getArguments().getParcelable(PARAM_CONFIG_DATA);
        }
        else {
            mConfigData = new ConfigData();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mEditTextWatchId = (EditText) view.findViewById(R.id.editTextWatchId);
        mEditTextWatchId.setText(mConfigData.getWatchId());

        mCheckboxTrackingEnabled = (CheckBox) view.findViewById(R.id.checkBoxTrackingEnabled);
        mCheckboxTrackingEnabled.setChecked(mConfigData.isTrackingEnabled());
        mCheckboxHeartRateActive = (CheckBox) view.findViewById(R.id.checkBoxHeartRateActive);
        mCheckboxHeartRateActive.setChecked(mConfigData.isHeartRateActive());
        mCheckboxGPSActive = (CheckBox) view.findViewById(R.id.checkBoxGPSActive);
        mCheckboxGPSActive.setChecked(mConfigData.isGPSActive());

        try {
            TextView textViewVersion = ( TextView) view.findViewById(R.id.textViewVersion);
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            textViewVersion.setText(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mCheckboxTrackingEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCheckboxHeartRateActive.setChecked(isChecked);
                mCheckboxGPSActive.setChecked(isChecked);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
    }

    @Override
    public void onStop() {
        super.onStop();

        ConfigData configData = mConfigData.clone();
        configData.setWatchId(mEditTextWatchId.getText().toString());
        configData.setTrackingEnabled(mCheckboxTrackingEnabled.isChecked());
        configData.setHeartRateActive(mCheckboxHeartRateActive.isChecked());
        configData.setGPSActive(mCheckboxGPSActive.isChecked());
        if ( ! configData.equals(mConfigData)) {
            if ( mListener != null) {
                mListener.onSettingsFragmentDataChange(configData);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        super.onAttach(context);
        if (context instanceof OnSettingsFragmentListener) {
            mListener = (OnSettingsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSettingsFragmentListener {
        public void onSettingsFragmentDataChange(ConfigData configData);
    }
}
