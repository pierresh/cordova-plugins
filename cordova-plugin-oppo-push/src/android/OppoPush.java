package com.lifang123.push;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.heytap.msp.push.HeytapPushManager;
import com.heytap.msp.push.callback.ICallBackResultService;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;

public class OppoPush extends CordovaPlugin {
    private static final String TAG = "OppoPushTag";
    private String appKey;
    private String appSecret;
    public static OppoPush instance;

    private static final int REQUEST_CODE = 193;

    public OppoPush() {
        instance = this;

    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        if (ActivityCompat.checkSelfPermission(cordova.getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.cordova.getActivity().getPackageManager().getApplicationInfo(this.cordova.getContext().getPackageName(), this.cordova.getContext().getPackageManager().GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = appInfo.metaData;
        this.appKey = bundle.getString("OPPOPUSH_APPKEY");
        this.appSecret = bundle.getString("OPPOPUSH_APPSECRET");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("register")) {
            HeytapPushManager.init(this.cordova.getContext(), true);
            HeytapPushManager.register(this.cordova.getContext(), this.appKey, this.appSecret, new ICallBackResultService() {
                @Override
                public void onRegister(int code, String s, String s1, String s2) {
                    if (code == 0) {
                        callbackContext.success(s);
                        LOG.i("注册成功", "registerId:" + s);
                    } else {
                        callbackContext.error("code=" + code + ",msg=" + s);
                        LOG.i("注册失败", "code=" + code + ",msg=" + s);
                    }
                }

                @Override
                public void onUnRegister(int code, String s, String s1) {
                    if (code == 0) {
                        LOG.i("注销成功", "code=" + code);
                    } else {
                        LOG.i("注销失败", "code=" + code);
                    }
                }

                @Override
                public void onGetPushStatus(final int code, int status) {
                    if (code == 0 && status == 0) {
                        LOG.i("Push状态正常", "code=" + code + ",status=" + status);
                    } else {
                        LOG.i("Push状态错误", "code=" + code + ",status=" + status);
                    }
                }

                @Override
                public void onGetNotificationStatus(final int code, final int status) {
                    if (code == 0 && status == 0) {
                        LOG.i("通知状态正常", "code=" + code + ",status=" + status);
                    } else {
                        LOG.i("通知状态错误", "code=" + code + ",status=" + status);
                    }
                }

                @Override
                public void onError(int i, String s, String s1, String s2) {

                }

                @Override
                public void onSetPushTime(final int code, final String s) {
                    LOG.i("SetPushTime", "code=" + code + ",result:" + s);
                }
            });
            HeytapPushManager.requestNotificationPermission();
            return true;
        }
        return false;
    }


    @Override
    public void onNewIntent(Intent intent) {
        try {
            Log.i(TAG, "OppoPush onNewIntent");
            this.cordova.getActivity().setIntent(intent);
            this.getOppoIntentData(intent);
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

    public void getOppoIntentData(Intent intent) {
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
            OppoPush.instance.bridgeWebView(jsonObject, String.format("cordova.fireDocumentEvent('messageReceived', %s);", jsonObject.toString()));
        } else {
            Log.i(TAG, "intent is null");
        }
    }

}
