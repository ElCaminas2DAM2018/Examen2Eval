package org.ieselcaminas.pmdm.examen2eval2019;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class MyService extends IntentService {
    public MyService() {
        super("MyService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        String requestString = intent.getStringExtra("URL");

        InputStream in;
        BufferedReader inReader;
        String s = null;
        String responseJSON = null;
        StringBuilder stBuilder = new StringBuilder();
        try {
            in = openHttpConnection(requestString);
            inReader = new BufferedReader(new InputStreamReader(in));
            while ((s = inReader.readLine()) != null) {
                stBuilder.append(s+"\n");
            }
            responseJSON = stBuilder.toString();
            in.close();
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.MyJSONRequestReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("JSON", responseJSON);
        sendBroadcast(broadcastIntent);

    }

    private InputStream openHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response;
        java.net.URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }
        catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }
}