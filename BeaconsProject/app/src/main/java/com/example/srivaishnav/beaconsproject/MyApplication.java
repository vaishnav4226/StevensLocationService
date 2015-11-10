package com.example.srivaishnav.beaconsproject;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

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
import java.util.UUID;

/**
 * Created by SriVaishnav on 10/31/15.
 */
public class MyApplication extends Application {
    private BeaconManager beaconManager;
    ArrayList<Beacons> currentBeacons = new ArrayList<Beacons>();
    @Override
    public void onCreate(){
        super.onCreate();
//        TextView schedule = (TextView) findViewById(R.id.schedule);
//        schedule.setText(beaconsJsonStr);

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"),
                        1004, 419));
            }
        });
        Log.v("Test message", "to check");
        FetchBeaconTask beaconTask = new FetchBeaconTask();
        beaconTask.execute();


    }

    public void showNotification(String title, String message){
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{ notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
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
                if (params.length == 0) {

                     BEACONS_BASE_URL =
                            "http://155.246.204.140:3000/api/buildings/10/floors/04/beacons";

                }
                else{
                     BEACONS_BASE_URL =
                            "http://155.246.204.140:3000/api/buildings/10/floors/04/beacons/"+params[0];}
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
                if(params.length ==0) {
                    getBeaconDataFromJson(beaconsJsonStr);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("beaconId:", currentBeacons.get(0).beaconId);
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
    }



}
