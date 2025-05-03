package com.shadowhunter.push.vivo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.vivo.push.PushConfig;
import com.vivo.push.listener.IPushQueryActionListener;
import com.vivo.push.util.VivoPushException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class VivoPush extends CordovaPlugin {
    private static final String TAG = VivoPush.class.getSimpleName();
    public CallbackContext callbackContext;
    public static VivoPush instance;

    public String packageName = "";
    public String className = "";

    public VivoPush() {
        instance = this;
    }

    private static final int REQUEST_CODE = 192;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        if (ActivityCompat.checkSelfPermission(cordova.getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
        }
        PushConfig config = new PushConfig.Builder()
                .agreePrivacyStatement(true)
                .build();
        try {
            PushClient.getInstance(cordova.getContext()).initialize(config);
        } catch (VivoPushException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("register")) {
            PushClient.getInstance(this.cordova.getContext()).turnOnPush(new IPushActionListener() {
                @Override
                public void onStateChanged(int state) {
                    // 开关状态处理， 0代表成功
                    if (state == 0) {
                        PushClient.getInstance(cordova.getContext()).getRegId(new IPushQueryActionListener() {
                            @Override
                            public void onSuccess(String s) {
                                callbackContext.success(s);
                            }

                            @Override
                            public void onFail(Integer integer) {
                                callbackContext.error("state=" + integer);
                            }
                        });
                    } else {
                        callbackContext.error("state=" + state);
                    }
                }
            });
            return true;
        }
        if (action.equals("onNewToken")) {
            this.callbackContext = callbackContext;
            return true;
        }
        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        try {
            this.cordova.getActivity().setIntent(intent);
            this.getVivoIntentData(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void bridgeWebView(JSONObject object, String bridgeJs) {
        final String js = String.format(bridgeJs, object.toString());
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                instance.webView.loadUrl("javascript:" + js);
            }
        });
    }

    public void getVivoIntentData(Intent intent) {
        if (null != intent) {
            JSONObject jsonObject = new JSONObject();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    try {
                        String content = bundle.getString(key);
                        jsonObject.put(key, content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            VivoPush.instance.bridgeWebView(jsonObject, String.format("cordova.fireDocumentEvent('messageReceived', %s);", jsonObject.toString()));
        } else {
            Log.i(TAG, "intent is null");
        }
    }
}
