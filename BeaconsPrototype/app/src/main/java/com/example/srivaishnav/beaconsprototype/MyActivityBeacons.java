package com.example.srivaishnav.beaconsprototype;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MyActivityBeacons extends AppCompatActivity {
    private BeaconManager beaconManager;
    public String beaconsStr = "test data";
    private Region region;
    ArrayList<Beacons> currentBeacons = new ArrayList<Beacons>();
    Beacon rightBeacon = null;

    public String url = "";
    private final String TAG = MainActivity.class.getSimpleName();
    public int counter = 0;
    private com.estimote.sdk.Region ALL_ESTIMOTE_BEACONS = new com.estimote.sdk.Region("Altofer-4th", null, null, null);

    //beacons ranging functions
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity_beacons);

        beaconManager = new BeaconManager(this);
        // add this below:
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> Beacons) {
                if(currentBeacons.size() == 0 ){
                    Log.v("testiing","this not fun1");

                    FetchBeaconTask beaconTask = new FetchBeaconTask();
                    beaconTask.execute(Integer.toString(Beacons.get(0).getMajor()));

                }

                else if(currentBeacons.size() != 0 && Beacons.size() != 0&& Integer.parseInt(new String(currentBeacons.get(0).beaconId).substring(0, 4)) != Beacons.get(0).getMajor()) {

                    Log.v("testiing","this not fun2");


                        FetchBeaconTask beaconTask = new FetchBeaconTask();
                        beaconTask.execute(Integer.toString(Beacons.get(0).getMajor()));


                }

                else{
                    //rightBeacon == null ||(Beacons.get(0).getRssi()> (-72) && Beacons.get(0).getMinor()!= rightBeacon.getMinor())
                    Log.v("testiing","this fun3");
                    rightBeacon = Beacons.get(0);
                    hideButton();
                    showButton();
                }


            }
        });

        region = new Region("ranged region",
                UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    public void hideButton(){
        //put the code to hide the buttons
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setVisibility(View.INVISIBLE); //Hide the button


        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);

        fab1.setVisibility(View.INVISIBLE); //Hide the button


    }

    public void showButton(){
        //show button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Message", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });

            fab.setVisibility(View.VISIBLE); //SHOW the button


        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplication(), WebView_Activity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
            fab1.setVisibility(View.VISIBLE); //SHOW the button

    }

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("1004:419", new ArrayList<String>() {{
            add("http://155.246.204.45:3000/test2");
            // read as: "Heavenly Sandwiches" is closest
            // to the beacon with major 22504 and minor 48827
            //add("Green & Green Salads");
            // "Green & Green Salads" is the next closest
            //add("Mini Panini");
            // "Mini Panini" is the furthest away
        }});

        placesByBeacons.put("1450:900", new ArrayList<String>() {{
            add("http://155.246.204.45:3000/test");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            counter += 1;
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }
//end

    public class FetchBeaconTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchBeaconTask.class.getSimpleName();
        private void getBeaconDataFromJson(String beaconsJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_BEACONID = "beaconId";
            final String OWM_COORDINATE = "coordinate";
            final String OWM_TRIGGER = "trigger";


            JSONObject beaconsJson = new JSONObject(beaconsJsonStr);
            JSONArray beaconsArray = beaconsJson.getJSONArray("beacons");

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.


            for (int i = 0; i < beaconsArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String beaconID;
                String coordinate;
                boolean trigger;

                // Get the JSON object representing the day
                JSONObject beacon = beaconsArray.getJSONObject(i);


                // description is in a child array called "weather", which is 1 element long.
                beaconID = beacon.getString(OWM_BEACONID);
                coordinate = beacon.getString(OWM_COORDINATE);
                trigger = beacon.getBoolean(OWM_TRIGGER);

                Beacons beaconObj = new Beacons(beaconID, coordinate, trigger);
                currentBeacons.add(beaconObj);
            }

        }

        @Override
        protected String[] doInBackground(String... params) {
            currentBeacons = new ArrayList<Beacons>();
            String BEACONS_BASE_URL;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String beaconsJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                    BEACONS_BASE_URL =
                            "http://155.246.204.45:3000/api/buildings/"+ params[0].substring(0 , 2) +"/floors/"
                                    + params[0].substring(2) +"/beacons";


//                final String BEACONS_BASE_URL =
//                        "http://155.246.204.140:3000/api/buildings/10/floors/04/beacons";


                Uri builtUri = Uri.parse(BEACONS_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
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
                beaconsStr = beaconsJsonStr;


                Log.v(LOG_TAG, "Beacon string: " + beaconsJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
//
//            if(params.length != 0){
//                Intent intent = new Intent();
//                intent.setClass(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//            }


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


//monitoring functions
//        beaconManager = new BeaconManager(getApplicationContext());
//        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
//            @Override
//            public void onEnteredRegion(Region region, List<Beacon> list) {
//                //launch a BG thread
//                showNotification(
//                        "Your gate closes in 47 minutes.",
//                        "Current security wait time is 15 minutes, "
//                                + "and it's a 5 minute walk from security to the gate. "
//                                + "Looks like you've got plenty of time!");
//                counter += 1;
//                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//                fab.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Snackbar.make(view, "Message", Snackbar.LENGTH_LONG)
//                                .setAction("Action", null).show();
//                    }
//
//                });
//                if (counter == 1)
//                {
//                    fab.setVisibility(View.VISIBLE); //SHOW the button
//                }
//
//                FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
//                fab1.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(getApplication(), WebView_Activity.class);
//                        startActivity(intent);
//                    }
//                });
//                if (counter == 1)
//                {
//                    fab1.setVisibility(View.VISIBLE); //SHOW the button
//                }
//            }
//            @Override
//            public void onExitedRegion(Region region) {
//                // could add an "exit" notification too if you want (-:
//            }
//        });
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                beaconManager.startMonitoring(new Region(
//                        "monitored region",
//                        UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"),
//                        1004, 419));
//            }
//        });
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//
//                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
//
//            }
//        });
//      }
//end


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
}
