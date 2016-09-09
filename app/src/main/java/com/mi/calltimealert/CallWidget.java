package com.mi.calltimealert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 */
public class CallWidget extends AppWidgetProvider implements ToggleResultReceiver.Receiver {


    public static String PREF_KEY = "TOGGLE_ON_OFF";
    public static String PREF_ON_OFF_KEY = "PREF_ON_OFF";
    protected static boolean dontChangeState;
    private static long POLLING_INTERVAL = 10000L;

    public static final String API_KEY = "iPrag267";
    public static final String SECRET_KEY = "ACSHSHS";
    private static final String GET_TOGGLE_STATUS_URL = "http://socialapione.ipragmatech.com/socialapi/alert/get/status";
    public static final String POST_TOGGLE_STATUS_URL = "http://socialapione.ipragmatech.com/socialapi/alert/post/status";


    private static Boolean toggleValue = false;
    private static HTTPRequestor httpRequestor = new HTTPRequestor();

    private static MediaPlayer mediaPlayer;
    private Context context;

    public static MediaPlayer getMediaPlayer(Context context) {
        if (mediaPlayer == null) {
            float count=100*.01f;
            mediaPlayer = MediaPlayer.create(context, R.raw.myh9jorc);
            AudioManager audioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            mediaPlayer.setVolume(count,count);
        }
        return mediaPlayer;
    }

    public static void startPlay(Context context) {
        Log.d("CALL PLAY", "START PLAY CHECK");
        if (!getMediaPlayer(context).isPlaying()) {
            Log.d("CALL PLAY", "START PLAY");
            getMediaPlayer(context).start();
        }
    }

    public static void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
//           mediaPlayer.reset();
        }

    }

    public static void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
//           mediaPlayer.reset();
        }

    }

    /**
     * Update the widget and make call to the service
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param appWidgetIds
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] appWidgetIds) {

        if (dontChangeState) {
            dontChangeState = !dontChangeState;
        } else {
            toggleState(context);
        }

        updateContents(context, appWidgetManager, appWidgetId, appWidgetIds);

    }

    /**
     * Update the content and layout
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param appWidgetIds
     */
    static void updateContents(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] appWidgetIds) {

        CharSequence widgetOnText = context.getString(R.string.widget_on);
        CharSequence widgetOffText = context.getString(R.string.widget_off);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.call_widget);

        if (getState(context) == true) {
            views.setTextViewText(R.id.appwidget_toggle, widgetOnText);
            views.setTextViewCompoundDrawables(R.id.appwidget_toggle, 0, R.drawable.ic_on, 0, 0);

        } else {
            views.setTextViewText(R.id.appwidget_toggle, widgetOffText);
            views.setTextViewCompoundDrawables(R.id.appwidget_toggle, 0, R.drawable.ic_off, 0, 0);

        }


        // Register an onClickListener
        Intent intent = new Intent(context, CallWidget.class);

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_toggle, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    /**
     * Toggle between states
     *
     * @param context
     * @return
     */
    public static boolean toggleState(Context context) {
        System.out.println("CallWidget.toggleState");
        SharedPreferences preference = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        boolean state = !preference.getBoolean(PREF_ON_OFF_KEY, false);
        preference.edit().putBoolean(PREF_ON_OFF_KEY, state).commit();
        toggleValue = state;
        updateCallReceiver(context, state);
        //Send the status to the webservice
        new postToggleTask().execute();
        return state;
    }

    /**
     * Update the CallReciever based on state
     *
     * @param context
     * @param state
     */
    public static void updateCallReceiver(Context context, boolean state) {
        if (state == true) {
            CallReceiver.startlisten(context);
        } else {
            CallReceiver.stopListen();
        }
    }

    /**
     * retrieve the current state
     *
     * @param context
     * @return
     */
    public static boolean getState(Context context) {
        System.out.println("CallWidget.getState");
        SharedPreferences preference = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        return preference.getBoolean(PREF_ON_OFF_KEY, false);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        System.out.println("CallWidget.onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds);
        }

    }

    @Override
    public void onEnabled(Context context) {
        System.out.println("CallWidget.onEnabled");
        setUpAlarm(context);
        dontChangeState = true;
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        System.out.println("CallWidget.onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }


    public void setUpAlarm(Context context) {
        Intent intent = new Intent(context, ToggleService.class);
        ToggleResultReceiver mReceiver = new ToggleResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        intent.putExtra("url", GET_TOGGLE_STATUS_URL);
        intent.putExtra("receiver", mReceiver);
        PendingIntent pending_intent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarm_mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm_mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), POLLING_INTERVAL, pending_intent);
    }

    static class postToggleTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        String result = null;

        protected String doInBackground(String... urls) {
            try {
                Map<String, String> params = new HashMap<String, String>();
                params.put("apiKey", API_KEY);
                params.put("secretKey", SECRET_KEY);
                params.put("status", String.valueOf(toggleValue));

                result = httpRequestor.downloadData(POST_TOGGLE_STATUS_URL, "POST", params);
            } catch (Exception e) {
                this.exception = e;
                Log.e("EXception", e.getMessage(), e);
                return null;
            }
            return result;
        }

        protected void onPostExecute(String feed) {

        }
    }

    /**
     * Not using it at the moment
     *
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case ToggleService.STATUS_RUNNING:
                break;
            case ToggleService.STATUS_FINISHED:
                break;
            case ToggleService.STATUS_ERROR:
                break;
        }
    }

}

