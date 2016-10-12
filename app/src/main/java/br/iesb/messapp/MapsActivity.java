package br.iesb.messapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int MAP_REQUEST_FINE_LOCATION_PERMISSION = 9001;
    public static final String MAP_UPDATE = "br.iesb.messapp.MAP_UPDATE";
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private int mapZoom;
    private Marker marker;
    private List<Marker> usersMarkers;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (MAP_UPDATE.equals(action)){
                //List<Location> usersLocations = intent.getParcelableArrayListExtra("locations");
                List<br.iesb.messapp.model.Location> usersLocations = (ArrayList)intent.getSerializableExtra("locations");
                cleanUsersMarkers();
                usersMarkers = createMarker(usersLocations);
                Log.i(getClass().getSimpleName(), "Usuários adicionados no mapa.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapZoom = getResources().getInteger(R.integer.map_default_zoom);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MAP_REQUEST_FINE_LOCATION_PERMISSION);
        } else {
            getLastLocation();
            getLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        createReceiver();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);
        super.onStop();
    }

    private void getLastLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null){
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                LatLng myLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                marker = createMarker(myLatLng);
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, mapZoom));
            }
        }
    }

    private Marker createMarker(LatLng latLng){
        Drawable ic_navigation = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_navigation_24dp, null);
        BitmapDescriptor markerIcon = Utility.getMarkerIconFromDrawable(ic_navigation);
        return this.googleMap.addMarker(new MarkerOptions().position(latLng)
                .title(getResources().getString(R.string.my_last_location))
                .icon(markerIcon));
    }

    private List<Marker> createMarker(List<br.iesb.messapp.model.Location> locationList){
        List<Marker> markerList = new ArrayList<>();
        for (br.iesb.messapp.model.Location location: locationList) {
            markerList.add(this.googleMap.addMarker(new MarkerOptions().position(location.getLatLng())));
        }
        return markerList;
    }

    private void cleanUsersMarkers(){
        if (usersMarkers != null) {
            for (Marker marker : usersMarkers) {
                marker.remove();
            }
        }
    }

    private void createReceiver(){
        IntentFilter filter = new IntentFilter(MAP_UPDATE);
        registerReceiver(receiver, filter);
    }

    private void getLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (marker == null){
                        marker = createMarker(myLatLng);
                    }
                    marker.setPosition(myLatLng);
                    marker.setRotation(location.getBearing());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MAP_REQUEST_FINE_LOCATION_PERMISSION:
                boolean permissionsGranted = true;
                if (grantResults.length > 0){
                    for (int grantResult : grantResults){
                        if (grantResult != PackageManager.PERMISSION_GRANTED){
                            permissionsGranted = false;
                            break;
                        }
                    }
                } else {
                    permissionsGranted = false;
                }

                if (permissionsGranted){
                    getLastLocation();
                    getLocation();
                } else {
                    Utility.alertMsg(
                            this,
                            getString(R.string.title_permission_necessary),
                            getString(R.string.msg_permission_location)
                    );
                    finish();
                }

                break;
        }
    }
}
