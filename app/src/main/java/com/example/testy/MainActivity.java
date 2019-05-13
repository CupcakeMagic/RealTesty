package com.example.testy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int random = new Random().nextInt(100000);

    private static final String CHANNEL_ID = "TestyChannel";
    public static final String EXTRA_MESSAGE = "com.example.testy.MESSAGE";
    public int ID = 0;
    private static final int PICK_CONTACT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        //startActivity(browserIntent);
        createNotificationChannel();
        if(ID == 0){
            ID = new Random().nextInt(100000);
        }
        new WaitForResponse().execute();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

    }

    public void OnServerMessageReceived(String site){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Ready for the challenge?")
                .setContentText("Press the notification to test the site.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent intent = new Intent(this, WebPageOpen.class);
        intent.putExtra(EXTRA_MESSAGE, site);
        startActivity(intent);
        //Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService((Context.NOTIFICATION_SERVICE));
        manager.notify(0,builder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class WaitForResponse extends AsyncTask<String , Void ,String> {
        //String server_response;


        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                while(true){


                //String mPath = Environment.getExternalStorageDirectory().toString() + "/OtherScreenshot6.jpg";
                //SystemClock.sleep(7000);

                url = new URL("http://192.168.43.251:8080/phone");

                // 1. create HttpURLConnection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");


                OutputStream os = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                String version = Build.VERSION.RELEASE;
                Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
                ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
                String packageName = resolveInfo.activityInfo.packageName;

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("device", manufacturer+model);
                jsonObject.accumulate("version",  version);
                jsonObject.accumulate("browser",  packageName);
                //jsonObject.accumulate("picture", Base64.encodeToString(byteArray,0));
                jsonObject.accumulate("id", 2);




                //
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"),8);
                String status = reader.readLine();
                System.out.println(status);
                if(status==null){
                    Thread.sleep(3000);
                }
                else{
                    System.out.println(status);
                    OnServerMessageReceived(status);
                    return null;
                }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Log.e("Response", "" + server_response);


        }
    }


    // Converting InputStream to String


}
