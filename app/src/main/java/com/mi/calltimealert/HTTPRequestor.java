package com.mi.calltimealert;

import android.os.StrictMode;
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
import java.util.Iterator;
import java.util.Map;

/**
 * Handles network request
 * Created by neha on 14/7/16.
 */
public class HTTPRequestor {
    private static final String TAG = "HTTPRequestor";

    /**
     * Method to call the network service
     *
     * @param requestUrl
     * @param method
     * @param params
     * @return
     * @throws IOException
     * @throws DownloadException
     */
    public String downloadData(String requestUrl, String method, Map<String, String> params) throws IOException, DownloadException {
        InputStream inputStream = null;

        /* forming th java.net.URL object */
        URL url = new URL(requestUrl);

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String  urlParameters = bodyBuilder.toString();

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod(method);

        /* optional request header */
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
        dStream.writeBytes(urlParameters);
        dStream.flush();
        dStream.close();
        int statusCode = connection.getResponseCode();
        /* 200 represents HTTP OK */
        if (statusCode == 200) {


            inputStream = new BufferedInputStream(connection.getInputStream());

            String response = convertInputStreamToString(inputStream);

            return parseResult(response);
        } else {
            throw new DownloadException("Failed to fetch data!!");
        }
    }


    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

            /* Close Stream */
        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

    private String parseResult(String result) {


        String alertStatus = null;
        try {
            JSONObject response = new JSONObject(result);

           alertStatus = response.get("status").toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return alertStatus;
    }

    public class DownloadException extends Exception {

        public DownloadException(String message) {
            super(message);
        }

        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
