package com.example.srivaishnav.beaconsprototype;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

//        searchView.setOnQueryTextListener(
//                new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextChange(String newText) {
//                        //Text has changed? Apply filtering
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onQueryTextSubmit(String query) {
//                        // perform the final search
//                        return false;
//                    }
//                }
//        );

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Stevens and move the camera
        LatLng stevens = new LatLng(40.7443226,-74.02586819999999);
        LatLng altofer = new LatLng(40.74359103237699, -74.02700679772681);
        LatLng babbio = new LatLng(40.74286352103865, -74.02658837312049);
        LatLng howe = new LatLng(40.744907848304614, -74.02397053712195);

        // Add a position for the camera to zoom in
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(40.7443226, -74.02586819999999), 16));
        // Adding custom and multiple markers in our maps
        Marker Altofer = mMap.addMarker(new MarkerOptions()
                .position(altofer)
                .title("Kenneth J. Altorfer")
                .snippet("The birth place of iBeacons project"));

        Marker Babbio = mMap.addMarker(new MarkerOptions()
                .position(babbio)
                .title("Babbio Center")
                .snippet("Center for Technology Management"));

        Marker Howe = mMap.addMarker(new MarkerOptions()
                .position(howe)
                .title("Wesley J. Howe")
                .snippet("School of Business"));

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(stevens));

        // Adding an current location button
        mMap.setMyLocationEnabled(true);

    }


    public void startBeacons(View v){
        Intent intent = new Intent(this, MyActivityBeacons.class);
        startActivity(intent);
    }
}
