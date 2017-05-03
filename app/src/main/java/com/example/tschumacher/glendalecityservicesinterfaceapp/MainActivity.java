package com.example.tschumacher.glendalecityservicesinterfaceapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {

    private EditText respText;
    private static final String siteUrlSansActivityId = "https://csi.glendaleca.gov/csipropertyportal/jsp/frameContent.jsp?actStatus=Case&actId=";
    private String siteUrl;
    private EditText activityId;
    private Context mContext  = MainActivity.this;
    private int NOTIFICATION_ID = 1;
    private Notification noti;
    private NotificationManager nm;
    PendingIntent contentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        activityId = (EditText) findViewById(R.id.edtURL);

        activityId.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                siteUrl = siteUrlSansActivityId + activityId.getText().toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        Button btnGo = (Button) findViewById(R.id.btnGo);
        respText = (EditText) findViewById(R.id.edtResp);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Log.d("JSwa", "Connecting to [" + siteUrl + "]");
                    Document doc  = Jsoup.connect(siteUrl).get();
                    // Get document (HTML page) title
                    String title = doc.title();
                    Log.d("JSwA", "Title ["+title+"]");

                    Elements topicList = doc.select("td.tabletext");

                    String status = topicList.get(1).text();

                    respText.setText(status);
                }
                catch(Throwable t) {
                    t.printStackTrace();
                }

            }
        });

        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        ( new ParseURLLoop() ).execute(siteUrl);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ParseURLLoop extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            //String siteUrl = params[0];

            Long previousDate = new Long(System.currentTimeMillis());
            Log.d("doInBackground", "PreviousDate [" + previousDate + "]");
            Long intervall = new Long(System.currentTimeMillis());
            Log.d("doInBackground", "intervall [" + intervall + "]");

            Boolean a = false;
            while ( a == false){
                if ((intervall - previousDate) > 1000 * 60 ){

                    StringBuffer buffer = new StringBuffer();
                    try {
                        Log.d("JSwa", "In here");

                        Log.d("JSwa", "Connecting to [" + siteUrl + "]");
                        Document doc  = Jsoup.connect(siteUrl).get();
                        Log.d("JSwa", "Connected to [" + siteUrl + "]");
                        // Get document (HTML page) title
                        String title = doc.title();
                        Log.d("JSwA", "Title ["+title+"]");

                        Elements topicList = doc.select("td.tabletext");

                        if(!"In Process".equals(topicList.get(1).text())){
                            Log.d("Status", "status is [" + topicList.get(1).text() +"]");

                            createNotification( "Glendale AE" , topicList.get(1).text() , MainActivity.this);

                            Intent notificationIntent = new Intent(mContext.getApplicationContext(), MainActivity.class);
                            contentIntent = PendingIntent.getActivity(mContext.getApplicationContext(),
                                    0, notificationIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                        }
                    }
                    catch(Throwable t) {
                        t.printStackTrace();
                    }

                    previousDate = intervall;
                }
                intervall = new Long(System.currentTimeMillis());
            }

            return null;
        }

        private void createNotification(String contentTitle, String contentText,Context context) {

            Log.d("createNotification", "title is [" + contentTitle +"]");

            nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);


            //Show the notification
            nm.notify(NOTIFICATION_ID, builder.build());
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

}