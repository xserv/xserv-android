package com.mi.xserv.http;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class SimpleHttpTask extends AsyncTask<IRequest, Void, String> implements ITaskListener {
    private int mTimeoutConnection = 15000;
    private int mTimeoutSocket = 10000;

    private OnResponseListener onResponseListener;

    // set callback
    public void setOnResponseListener(OnResponseListener listener) {
        onResponseListener = listener;
    }

    public void setTimeout(int timeoutConnection, int timeoutSocket) {
        mTimeoutConnection = timeoutConnection;
        mTimeoutSocket = timeoutSocket;
    }

    private void disableConnectionReuseIfNecessary() {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    @Override
    protected String doInBackground(IRequest... args) {
        disableConnectionReuseIfNecessary();

        String value = null;
        try {
            value = callUrl(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String callUrl(IRequest req) throws IOException {
        InputStream is = null;

        try {
            Log.d(this.getClass().getName(), "Http request: " + req.getUrl());

            URL url = new URL(req.getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(mTimeoutSocket /* milliseconds */);
            conn.setConnectTimeout(mTimeoutConnection /* milliseconds */);

            if (req.getUserAgent() != null && req.getUserAgent().length() > 0) {
                conn.setRequestProperty("User-Agent", req.getUserAgent());
            }
            if (req.getContentType() != null && req.getContentType().length() > 0) {
                conn.setRequestProperty("Content-Type", req.getContentType());
            }
            // conn.setUseCaches(false);

            conn.setRequestMethod(req.getMethod().toUpperCase(Locale.getDefault()));

            if (req.getMethod().toLowerCase(Locale.getDefault()).equals(SimpleHttpRequest.POST)) {
                composeRequestPost(conn, req);
            }

            // Starts the query
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Log.d(this.getClass().getName(), "The response is: " + responseCode);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                return readIt(is);
            } else {
                return null;
            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                // Log.d(this.getClass().getName(), "CLOSEEE");
                is.close();
            }
        }
    }

    private void composeRequestPost(HttpURLConnection conn, IRequest req) throws IOException {
        conn.setDoOutput(true);

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String key : req.getParamsKey()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(key, "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(req.getParam(key), "UTF-8"));
        }

        String query = sb.toString();

        if (!query.isEmpty()) {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
        }
    }

    // Reads an InputStream and converts it to a String.
    private String readIt(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String output) {
        if (onResponseListener != null) {
            if (output != null)
                onResponseListener.onResponse(output);
            else
                onResponseListener.onFail(output);
        }
    }

    public void execute(IRequest req) {
        super.execute(req);
    }

}
