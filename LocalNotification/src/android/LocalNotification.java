package com.shadowhunter.push.local;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.cordova.hellocordova.R;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocalNotification extends CordovaPlugin {
    private static final int REQUEST_CODE = 191;

    private final static String CHANNEL_ID = "Note";

    private NotificationCompat.Builder builder;

    private BroadcastReceiver receiver = null;

    private CallbackContext callbackContext = null;

    private String callBackId = "";

    private int initFlag = -1;

    private String title = "";
    private String text = "";
    private String message = "";

    public static LocalNotification instance;

    public LocalNotification() {
        instance = this;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        if (ActivityCompat.checkSelfPermission(cordova.getContext(),
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, REQUEST_CODE);
        }
        registeBroadCastReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            throws JSONException {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (permissions.length == 1 && permissions[0].equals(android.Manifest.permission.POST_NOTIFICATIONS)) {
                if (grantResults.length == 1 && grantResults[0] == 0) {
                    if (initFlag != -1) {
                        buildNotification(message, title, text);
                    }
                }
            }
        }
    }

    private void registeBroadCastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_RECEIVE_LOCAL_NOTIFICATION");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.hasExtra("Message") ? intent.getStringExtra("Message") : "";
                callBack(msg);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cordova.getActivity().registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            cordova.getActivity().registerReceiver(receiver, filter);
        }
    }

    private void callBack(String msg) {
        try {
            JSONObject object = new JSONObject(msg);

            callbackContext.success(object);

            LocalNotification.instance.bridgeWebView(object,
                    String.format("cordova.fireDocumentEvent('onLocalNotificationClickd', %s);", object.toString()));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cordova.getActivity().unregisterReceiver(receiver);
    }

    private void buildNotification(String message, String title, String msg) {
        createNotificationChannel();

        Intent intent = new Intent("ACTION_RECEIVE_LOCAL_NOTIFICATION");
        intent.putExtra("Message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getContext(), 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(cordova.getActivity(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(builder);

    }

    private void showNotification(NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(cordova.getContext());
        if (ActivityCompat.checkSelfPermission(cordova.getContext(),
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 191);
            return;
        }
        notificationManager.notify(1, builder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LocalChannle";
            String description = "LocalChannle Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = cordova.getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("showNotification")) {
            message = args.getString(0);
            JSONObject object = new JSONObject(message);
            initFlag = 1;
            this.title = object.optString("title", "");
            this.text = object.optString("text", "");
            this.callbackContext = callbackContext;
            this.callBackId = callbackContext.getCallbackId();
            this.buildNotification(message, title, text);
            return true;
        }
        return false;
    }

}
