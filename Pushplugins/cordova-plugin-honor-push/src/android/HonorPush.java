package com.shadowhunter.push.honor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.hihonor.push.sdk.HonorPushCallback;
import com.hihonor.push.sdk.HonorPushClient;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class HonorPush extends CordovaPlugin {
    private static final String TAG = HonorPush.class.getSimpleName();
    private static final int REQUEST_CODE = 195;
    public CallbackContext callbackContext;
    public static HonorPush instance;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        if (ActivityCompat.checkSelfPermission(cordova.getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
        }

        boolean isSupport = HonorPushClient.getInstance().checkSupportHonorPush(cordova.getContext());
        if (isSupport) {
            HonorPushClient.getInstance().init(cordova.getContext(), true);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("register")){
            // 获取PushToken
            HonorPushClient.getInstance().getPushToken(new HonorPushCallback<String>() {
                @Override
                public void onSuccess(String pushToken) {
                    // TODO: 新Token处理
                    callbackContext.success(pushToken);
                }

                @Override
                public void onFailure(int errorCode, String errorString) {
                    // TODO: 错误处理
                    callbackContext.error("errorCode=" + errorCode);
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
            this.getHonorIntentData(intent);
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

    public void getHonorIntentData(Intent intent) {
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
            HonorPush.instance.bridgeWebView(jsonObject, String.format("cordova.fireDocumentEvent('messageReceived', %s);", jsonObject.toString()));
        } else {
            Log.i(TAG, "intent is null");
        }
    }
}
