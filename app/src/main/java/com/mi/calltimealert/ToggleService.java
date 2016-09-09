package com.mi.calltimealert;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by neha on 14/7/16.
 */
public class ToggleService extends IntentService {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    public static String PREF_KEY = "TOGGLE_ON_OFF";
    public static String PREF_ON_OFF_KEY = "PREF_ON_OFF";

    private static final String TAG = "ToggleService";
    private HTTPRequestor httpRequestor;

    public ToggleService() {
        super(ToggleService.class.getName());
        httpRequestor = new HTTPRequestor();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");

        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            /* Update UI: Download Service is Running */
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);

            Map<String, String> params = new HashMap<String, String>();
            params.put("apiKey", CallWidget.API_KEY);
            params.put("secretKey", CallWidget.SECRET_KEY);


            try {
                String results = httpRequestor.downloadData(url, "GET", params);

                /* Sending result back to activity */
                if (null != results) {
                    bundle.putString("result", results);
                    receiver.send(STATUS_FINISHED, bundle);
                    SharedPreferences preference = getApplicationContext().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
                    //results = resultData.getString("result");
                    if (results != null && results.equals("false")) {
                        preference.edit().putBoolean(PREF_ON_OFF_KEY, false).commit();
                    } else if (results != null && results.equals("true")) {
                        preference.edit().putBoolean(PREF_ON_OFF_KEY, true).commit();
                    }
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                    ComponentName thisWidget = new ComponentName(getApplicationContext(), CallWidget.class);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                    if (appWidgetIds != null && appWidgetIds.length > 0) {
                        for (int appWidgetId : appWidgetIds) {
                            CallWidget.updateContents(getApplicationContext(), appWidgetManager, appWidgetId, appWidgetIds);
                            CallWidget.updateCallReceiver(getApplicationContext(), results.equals("true"));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                /* Sending error message back to activity */
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);


            }
        }
        Log.d(TAG, "Service Stopping!");
        this.stopSelf();
    }


}