package com.shadowhunter.push.honor;

import android.util.Log;

import com.hihonor.push.sdk.HonorMessageService;
import com.hihonor.push.sdk.HonorPushDataMsg;

import org.apache.cordova.PluginResult;

public class MyHonorMsgService extends HonorMessageService {
    private static final String TAG = MyHonorMsgService.class.getSimpleName();
    //Token发生变化时，会以onNewToken方法返回
    @Override
    public void onNewToken(String pushToken) {
        // TODO: 处理新token。
        Log.d(TAG, "onNewToken: " + pushToken);
        if (null != HonorPush.instance.callbackContext) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, pushToken);
            result.setKeepCallback(true);
            HonorPush.instance.callbackContext.sendPluginResult(result);
        }
    }

    @Override
    public void onMessageReceived(HonorPushDataMsg msg) {
        // TODO: 处理收到的透传消息。
    }
}