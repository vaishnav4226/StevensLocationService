package com.example.srivaishnav.beaconsprototype;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {


    Button sendButton;
    ListView listView;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //initialize layout
        listView = (ListView) findViewById(R.id.listView);
        sendButton = (Button) findViewById(R.id.sendButton);
        editText=(EditText)findViewById(R.id.editText);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        final String MESSAGES_BASE_URL = bundle.getString("url");

        FetchMessageTask messageTask = new FetchMessageTask();
        messageTask.execute(MESSAGES_BASE_URL);



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostMessageTask messageTask = new PostMessageTask();
                messageTask.execute(MESSAGES_BASE_URL,editText.getText().toString());
                editText.setText("");
            }
        });
    }

    public class PostMessageTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = PostMessageTask.class.getSimpleName();


        @Override
        protected String[] doInBackground(String... params) {
            String content;
            String MESSAGES_BASE_URL;
            HttpURLConnection urlConnection = null;


            try {
                MESSAGES_BASE_URL = params[0];
                content=params[1];

                Uri builtUri = Uri.parse(MESSAGES_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI POST " + builtUri.toString());

                // Create the request, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));

                HashMap data = new HashMap();
                // Put elements to the map
                data.put("content",content);
                data.put("sender", "Gregg");
                data.put("date", Long.toString(Calendar.getInstance().getTimeInMillis()));

                writer.write(getPostDataString(data));

                writer.flush();
                writer.close();
                os.close();

                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){
                    Log.v(LOG_TAG,"Works fine");
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

            }


            return null;
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public class FetchMessageTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMessageTask.class.getSimpleName();

        private void getMessageDataFromJson(String messagesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_MESSAGES = "messages";
            final String OWM_SENDER = "sender";
            final String OWM_CONTENT = "content";
            final String OWM_DATE = "date";


            JSONObject messagesJson = new JSONObject(messagesJsonStr);
            JSONArray messagesArray = messagesJson.getJSONArray(OWM_MESSAGES);

            for (int i = 0; i < messagesArray.length(); i++) {
                String sender;
                String content;
                String date;

                // Get the JSON object
                JSONObject message = messagesArray.getJSONObject(i);
                sender = message.getString(OWM_SENDER);
                content = message.getString(OWM_CONTENT);
                date = message.getString(OWM_DATE);
                Log.v("JSON",sender);
            }

        }

        @Override
        protected String[] doInBackground(String... params) {

            String MESSAGES_BASE_URL;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String messagesJsonStr = null;

            try {
                MESSAGES_BASE_URL =params[0];

                Uri builtUri = Uri.parse(MESSAGES_BASE_URL).buildUpon()
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
                messagesJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Beacon string: " + messagesJsonStr);
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
                getMessageDataFromJson(messagesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
    }

}
