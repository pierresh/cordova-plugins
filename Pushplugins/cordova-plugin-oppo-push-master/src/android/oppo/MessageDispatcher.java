package com.lifang123.push.oppo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.lifang123.push.OppoPush;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.List;

public class MessageDispatcher {

    public static void dispatch(Context context, String content) {
        Intent intent0 = null;
        try {
            intent0 = Intent.parseUri(content, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            if (isSupportByIntent(context, intent0)) {
                Intent intent = new Intent(context, OppoPush.instance.cordova.getActivity().getClass());
                intent.setAction(Intent.ACTION_VIEW);
                context.startActivity(intent);

            } else if (isJson(content)) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isSupportByIntent(Context context, Intent intent) {
        if (intent == null) return false;
        List list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

    private static boolean isJson(String json) {
        try {
            JSONObject object = new JSONObject(json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
