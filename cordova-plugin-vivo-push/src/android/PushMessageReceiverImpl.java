package com.shadowhunter.push.vivo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.vivo.push.model.UnvarnishedMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

import org.apache.cordova.PluginResult;

public class PushMessageReceiverImpl extends OpenClientPushMessageReceiver {
  private static final String TAG = PushMessageReceiverImpl.class.getSimpleName();

  @Override
  public void onReceiveRegId(Context context, String s) {
    Log.d(TAG, " onReceiveRegId= " + s);
    if (null != VivoPush.instance.callbackContext) {
      PluginResult result = new PluginResult(PluginResult.Status.OK, s);
      result.setKeepCallback(true);
      VivoPush.instance.callbackContext.sendPluginResult(result);
    }
  }

  @Override
  public void onTransmissionMessage(Context context, UnvarnishedMessage unvarnishedMessage) {
    super.onTransmissionMessage(context, unvarnishedMessage);
  }

  /**
   * 广播发送角标数量
   *
   * @param context
   * @param count   角标数量
   */
  @SuppressLint("WrongConstant")
  private void sendBadgeBroadCast(Context context, int count) {
    Intent intent = new Intent();

    intent.setAction("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
    if (!TextUtils.isEmpty(VivoPush.instance.packageName)) {
      intent.putExtra("packageName", VivoPush.instance.packageName);
    }
    if (!TextUtils.isEmpty(VivoPush.instance.className)) {
      intent.putExtra("className", VivoPush.instance.className);
    }
    intent.putExtra("notificationNum", count);

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
      intent.addFlags(0x01000000);
    }

    context.sendBroadcast(intent);
  }

}
