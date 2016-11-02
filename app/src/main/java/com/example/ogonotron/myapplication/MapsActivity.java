package com.example.ogonotron.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final String LOG_TAG = MapsActivity.class.getSimpleName();
    final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest lr;
    static int count = 0;
    List<LatLng> llList;
    private GoogleMap map;
    private GoogleApiClient gAPIc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.inflateMenu(R.menu.menu_activity_maps);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("yoyo");

        //Obtain the SupportMapFragment and call onMapReady when it is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        llList = new ArrayList<>();

        gAPIc = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        lr = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(1000)
                .setMaxWaitTime(12000)
                .setSmallestDisplacement(5);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        Log.d(LOG_TAG, "onPostResume");
        super.onPostResume();
        gAPIc.connect();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
        if (gAPIc.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(gAPIc, this);
            gAPIc.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        map = googleMap;
        enableLocation();
        Toast.makeText(getApplicationContext(), "Location layer enabled", Toast.LENGTH_SHORT).show();
    }

    private void enableLocation() {
        Log.d(LOG_TAG, "enableLocation");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //invokes onRequestPermissionsResult callback
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        map.setMyLocationEnabled(true); //API: Enables or disables the my-location layer
    }

    private void handleLocationUpdate(Location location) {
        Log.d(LOG_TAG, "handleLocationUpdate");
        Log.d(LOG_TAG, "Location is: " + location.toString());
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        this.llList.add(ll);

        //Count updates
        count++;
        String sCount = Integer.toString(count);

//        //Add Marker
//        MarkerOptions markerOptions = new MarkerOptions()
//            .position(ll)
//            .title(sCount);
//        map.addMarker(markerOptions);

        //Draw route
        PolylineOptions lineOptions = new PolylineOptions()
                .width(5)
                .color(Color.BLUE);

        Polyline line = map.addPolyline(lineOptions);
        line.setPoints(llList);

        //Toast
        Toast.makeText(getApplicationContext(), sCount, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "onLocationChanged");
        handleLocationUpdate(location);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(gAPIc);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                gAPIc, lr, this);

        if (location == null){
            Log.i(LOG_TAG, "How did this happen?");
        }
        else{
            handleLocationUpdate(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(LOG_TAG, "Location services connection failed using code: "
                    + connectionResult.getErrorCode());
        }
    }
}



































