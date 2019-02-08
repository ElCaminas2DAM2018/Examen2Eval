package org.ieselcaminas.pmdm.examen2eval2019;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private MyJSONRequestReceiver receiver;
    private TextView textViewDisplay;
    private boolean carrouselInRed;
    private CarrouselTask carrouselTask = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] stringsArray;

    public class MyJSONRequestReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "net.victoralonso.intent.action.PROCESS_RESPONSE";


        @Override
        public void onReceive(Context context, Intent intent) {
            String strJSON = intent.getStringExtra("JSON");

            try {
                JSONObject rootJSON = new JSONObject(strJSON);
                JSONObject resultsJSON = rootJSON.getJSONObject("results");
                JSONArray array = resultsJSON.getJSONArray("texts");

                stringsArray = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    stringsArray[i] = array.getString(i);
                }

                if (carrouselTask != null) {
                    carrouselTask.cancel(true);
                }

                carrouselTask = new CarrouselTask();
                carrouselTask.execute(stringsArray);


                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_EXTERNAL_STORAGE);
                } else {
                    save();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("JSON-VICTOR", strJSON);

        }



    }

    private class CarrouselTask extends AsyncTask<String,String,Void> {

        @Override
        protected Void doInBackground(String... strings) {

            while (!isCancelled()) {
                try {
                    for (int i=0; i<strings.length; i++) {
                        publishProgress(strings[i]);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values){
            textViewDisplay.setText(values[0]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewDisplay = (TextView) findViewById(R.id.textViewDisplay);

        Button b = (Button) findViewById(R.id.button_config);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyPreferencesActivity.class);
                startActivity(intent);
            }
        });

        Button go = (Button) findViewById(R.id.button_go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText1);
                Intent intent = new Intent(getApplicationContext(), MyService.class);
                intent.putExtra("URL", editText.getText().toString());
                startService(intent);
                Log.d("JSON-VICTOR", "button pressed");
            }
        });

        IntentFilter filter = new IntentFilter(MyJSONRequestReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyJSONRequestReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String textURL = pref.getString("option2", "");

        carrouselInRed = pref.getBoolean("option1", true);
        if (carrouselInRed) {
            textViewDisplay.setTextColor(Color.RED);
        } else {
            textViewDisplay.setTextColor(Color.BLACK);
        }

        EditText editText = (EditText) findViewById(R.id.editText1);
        editText.setText(textURL);

    }

    private void save() {
        PrintWriter writer = null;
        try {
            //---SD Card Storage---
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File (sdCard.getAbsolutePath());
            File file = new File(directory, "textfile.txt");
            FileOutputStream fOut = new FileOutputStream(file);
            writer = new PrintWriter(fOut);
            for (String s: stringsArray) {
                writer.println(s);
            }
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer!=null) {
                writer.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    save();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

}