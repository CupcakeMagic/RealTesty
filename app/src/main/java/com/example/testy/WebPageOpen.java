package com.example.testy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class WebPageOpen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_page_open);

        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        WebView myWebView = findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl(url);
        try {
            takeScreenshot();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void takeScreenshot() throws IOException {

        final View view = WebPageOpen.this.getWindow().getDecorView();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    // image naming and path  to include sd card  appending name you choose for file
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //TODO: call the async task
                    new GetMethodDemo().execute();

                } catch (Throwable e) {
                    // Several error may come out with file handling or DOM
                    e.printStackTrace();
                }
            }
        });


    }

    public void startPingAgain(){
        Intent menuIntent = new Intent(this, MainActivity.class);
        startActivity(menuIntent);
    }

    public class GetMethodDemo extends AsyncTask<String , Void ,String> {
        //String server_response;

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                //String mPath = Environment.getExternalStorageDirectory().toString() + "/OtherScreenshot6.jpg";
                SystemClock.sleep(4000);
                View view = WebPageOpen.this.getWindow().getDecorView();
                int width = view.getWidth();
                int height = view.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width,
                        height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                url = new URL("http://192.168.43.251:8080/phone");

                // 1. create HttpURLConnection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");


                OutputStream os = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                //
//                String manufacturer = Build.MANUFACTURER;
//                String model = Build.MODEL;
//                String version = Build.VERSION.RELEASE;
//                Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
//                ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
//                String packageName = resolveInfo.activityInfo.packageName;

                JSONObject jsonObject = new JSONObject();
//                    jsonObject.accumulate("device", manufacturer+model);
//                    jsonObject.accumulate("version",  version);
//                    jsonObject.accumulate("browser",  packageName);
                jsonObject.accumulate("picture", Base64.encodeToString(byteArray,0));
                jsonObject.accumulate("id", 2);

                //
                writer.write(jsonObject.toString());
                Log.i(MainActivity.class.toString(), byteArray.toString());
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"),8);
                System.out.println(reader.readLine());

                startPingAgain();
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
}
