package com.example.srivaishnav.beaconsprototype;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.View;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.jiahuan.svgmapview.SVGMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import java.util.List;


import com.jiahuan.svgmapview.SVGMapView;
import helper.AssetsHelper;

public class MyActivityBeacons extends AppCompatActivity {

    private BeaconManager beaconManager;
    ArrayList<Beacons> currentBeacons = new ArrayList<>();
    Beacon rightBeacon = null;
    FetchBeaconTask beaconTask=null;

    // static data
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static String HostIpAddress;
    private final String TAG = MainActivity.class.getSimpleName();
    private com.estimote.sdk.Region ALL_ESTIMOTE_BEACONS = new com.estimote.sdk.Region("Altofer-4th", null, null, null);



    //Layout
    FloatingActionButton buttonMessage;
    FloatingActionButton buttonBrowser;
    public SVGMapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity_beacons);
        // initialize layout
        buttonMessage = (FloatingActionButton) findViewById(R.id.fab);
        buttonBrowser = (FloatingActionButton) findViewById(R.id.fab1);
        mapView = (SVGMapView) findViewById(R.id.location_mapview);
        mapView.loadMap(AssetsHelper.getContent(this, "floor-plan-text.svg"));

        // initialize host
        HostIpAddress= getString(R.string.host_Address);


        // Android M Permission check 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
                  @TargetApi(Build.VERSION_CODES.M)
                  @Override
                  public void onDismiss(DialogInterface dialog){
                      requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
                  }
                });
                builder.show();
            }
        }

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> Beacons) {

                if (Beacons.size() != 0) {
                    if (currentBeacons.size() == 0 || (currentBeacons.size() != 0 && Integer.parseInt(currentBeacons.get(0).beaconId.substring(0, 4)) != Beacons.get(0).getMajor())) {
                        if(beaconTask==null) {
                            FetchBeaconTask beaconTask = new FetchBeaconTask();
                            beaconTask.execute(Integer.toString(Beacons.get(0).getMajor()));
                            beaconTask=null;
                        }
                    } else {
                        if(Beacons.get(0).getRssi()>(-72)){

                            rightBeacon=Beacons.get(0);
                            if(rightBeacon == null || (Beacons.get(0).getRssi() > (-72) && Beacons.get(0).getMinor() != rightBeacon.getMinor())){

                                String url = "http://"+HostIpAddress+"/api/buildings/" + Integer.toString(rightBeacon.getMajor()).substring(0, 2) + "/floors/" + Integer.toString(rightBeacon.getMajor()).substring(2) + "/offices/" + Integer.toString(rightBeacon.getMinor());
                                //Log.v("Look at here--------", url);
                                showButton(url);
                            }

                        }else{
                            rightBeacon=null;
                            hideButton();
                        }

                    }

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
        super.onPause();
    }

    public void hideButton() {
        //hide the buttons
        buttonMessage.setVisibility(View.INVISIBLE);
        buttonBrowser.setVisibility(View.INVISIBLE);
    }

    public void showButton(String passedUrl) {
        final String url4WebView = passedUrl;
        final String url4Message =passedUrl+"/messages";

        //show button "Message"
        buttonMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(getApplication(),ChatActivity.class);
                intent.putExtra("url",url4Message);
                startActivity(intent);
            }

        });
        buttonMessage.setVisibility(View.VISIBLE);

        //show button "webpage"

        buttonBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), WebView_Activity.class);
                intent.putExtra("url", url4WebView);
                startActivity(intent);
            }
        });
        buttonBrowser.setVisibility(View.VISIBLE);

    }


    public class FetchBeaconTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchBeaconTask.class.getSimpleName();

        private void getBeaconDataFromJson(String beaconsJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_BEACONS="beacons";
            final String OWM_BEACONID = "beaconId";
            final String OWM_COORDINATE = "coordinate";
            final String OWM_TRIGGER = "trigger";


            JSONObject beaconsJson = new JSONObject(beaconsJsonStr);
            JSONArray beaconsArray = beaconsJson.getJSONArray(OWM_BEACONS);

            for (int i = 0; i < beaconsArray.length(); i++) {
                String beaconID;
                String coordinate;
                boolean trigger;

                // Get the JSON object
                JSONObject beacon = beaconsArray.getJSONObject(i);
                beaconID = beacon.getString(OWM_BEACONID);
                coordinate = beacon.getString(OWM_COORDINATE);
                trigger = beacon.getBoolean(OWM_TRIGGER);

                Beacons beaconObj = new Beacons(beaconID, coordinate, trigger);
                currentBeacons.add(beaconObj);
            }

        }

        @Override
        protected String[] doInBackground(String... params) {
            // empty currentBeacons Array
            currentBeacons = new ArrayList<Beacons>();

            String BEACONS_BASE_URL;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String beaconsJsonStr = null;

            try {
                BEACONS_BASE_URL =
                        "http://"+HostIpAddress+"/api/buildings/" + params[0].substring(0, 2) + "/floors/"
                                + params[0].substring(2) + "/beacons";


                Uri builtUri = Uri.parse(BEACONS_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                beaconsJsonStr = buffer.toString();


                Log.v(LOG_TAG, "Beacon string: " + beaconsJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }


            try {
                getBeaconDataFromJson(beaconsJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("beaconId:", currentBeacons.get(0).beaconId);
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
    }


    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}