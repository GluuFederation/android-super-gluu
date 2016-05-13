/*
 *  oxPush2 is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 *  Copyright (c) 2014, Gluu
 */

package org.gluu.oxpush2.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.gluu.oxpush2.app.GluuToast.GluuToast;
import org.gluu.oxpush2.app.listener.OxPush2RequestListener;
import org.gluu.oxpush2.model.OxPush2Request;

/**
 * Main activity fragment
 *
 * Created by Yuriy Movchan on 12/28/2015.
 */
public class MainActivityFragment extends Fragment implements TextView.OnEditorActionListener, View.OnClickListener {

    private static final String TAG = "main-activity-fragment";

    private OxPush2RequestListener oxPush2RequestListener;

    private LayoutInflater inflater;

    private Context context;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            if (context != null) {
                showToastWithText(message);
            }
        }
    };

    private BroadcastReceiver mPushMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            showPushToast(intent);
        }
    };

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        this.inflater = inflater;
        view.findViewById(R.id.button_scan).setOnClickListener(this);
        //Setup message receiver
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("ox_request-precess-event"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPushMessageReceiver,
                new IntentFilter(GluuMainActivity.QR_CODE_PUSH_NOTIFICATION));
        context = view.getContext();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OxPush2RequestListener) {
            oxPush2RequestListener = (OxPush2RequestListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        oxPush2RequestListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Toast.makeText(getActivity(), R.string.process_qr_code, Toast.LENGTH_SHORT).show();

        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
//                    showToastWithText(context.getString(R.string.process_qr_code));
                    // Parsing bar code reader result
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (BuildConfig.DEBUG) Log.d(TAG, "Parsing QR code result: " + result.toString());
                    OxPush2Request oxPush2Request = new Gson().fromJson(result.getContents(), OxPush2Request.class);
                    ((OxPush2RequestListener) getActivity()).onQrRequest(oxPush2Request);

                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    showToastWithText(context.getString(R.string.canceled_process_qr_code));
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        submit();
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            submit();
            return true;
        }

        return false;
    }

    private void showPushToast(Intent intent){
//        View view = inflater.inflate(R.layout.gluu_push_toast, null);
        String message = intent.getStringExtra(GluuMainActivity.QR_CODE_PUSH_NOTIFICATION_MESSAGE);
        final OxPush2Request oxPush2Request = new Gson().fromJson(message, OxPush2Request.class);
        oxPush2RequestListener.onQrRequest(oxPush2Request);
    }

    private void showToastWithText(String text){
        GluuToast gluuToast = new GluuToast(context);
        View view = inflater.inflate(R.layout.gluu_toast, null);
        gluuToast.showGluuToastWithText(view, text);
    }

    private void submit() {
        if (oxPush2RequestListener != null) {
//            String message = "{\"req_ip\":\"178.136.126.205\",\"app\":\"https://ce-release.gluu.org/identity/authentication/authcode\",\"username\":\"nazar2017\",\"method\":\"authenticate\",\"req_loc\":\"Ukraine%2C%20L%27vivs%27ka%20Oblast%27%2C%20Lviv\",\"state\":\"cd98df91-3b71-4911-9a15-84253c326c7c\",\"created\":\"2016-05-10T09:19:46.260000\",\"issuer\":\"https://ce-release.gluu.org\"}";
//            Intent intent = new Intent();
//            intent.putExtra(GluuMainActivity.QR_CODE_PUSH_NOTIFICATION_MESSAGE, message);
//            showPushToast(intent);
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt(getString(R.string.scan_oxpush2_prompt));
            integrator.initiateScan();
        }
    }

}
