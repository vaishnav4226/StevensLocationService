package com.example.android.stevenslocationservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

public class TutorialActivity extends Activity {

    int mFlipping = 0 ; // Initially flipping is off
    Button mButton ; // Reference to button available in the layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        /** Click event handler for button */
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper1);

                if(mFlipping==0){
                    /** Start Flipping */
                    flipper.startFlipping();
                    mFlipping=1;
                    mButton.setText("Stop");
                }
                else{
                    /** Stop Flipping */
                    flipper.stopFlipping();
                    mFlipping=0;
                    mButton.setText("Start");
                }
            }
        };

        /** Getting a reference to the button available in the resource */
        mButton = (Button) findViewById(R.id.btn);

        /** Setting click event listner for the button */
        mButton.setOnClickListener(listener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
