package com.shadowhunter.scanner;

import android.content.Intent;
import android.util.Log;

import com.king.camera.scan.CameraScan;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.OpenCV;

/**
 * This class echoes a string called from JavaScript.
 */
public class ScannerPlugin extends CordovaPlugin {
    private static final String TAG = ScannerPlugin.class.getSimpleName();
    private static final int REQUEST_CODE = 196;
    public CallbackContext callbackContext;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("init")) {
            OpenCV.initOpenCV();

            WeChatQRCodeDetector.init(this.cordova.getActivity());

            callbackContext.success("init success");
            return true;
        }

        if (action.equals("scan")){
            this.callbackContext  = callbackContext;

            Intent intent = new Intent(this.cordova.getActivity(), WeChatMultiQRCodeActivity.class);
            if (args.length() != 0) {
                intent.putExtra("bottomText", args.getString(0));
            }
            cordova.startActivityForResult(this, intent, REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
                String result = intent.getStringExtra(CameraScan.SCAN_RESULT);
                callbackContext.success(result);
            } else {
                callbackContext.error("Scan failed");
            }
        }
    }
}
