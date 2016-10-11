package br.iesb.messapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import br.iesb.messapp.services.SendPositionService;

/**
 * Created by Felipe on 01/10/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            String uId = intent.getStringExtra("uid");
            if (uId != null) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng loc = null;
                if (lastKnownLocation == null) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (lastKnownLocation != null) {
                    loc = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                }

                Intent mServiceIntent = new Intent(context, SendPositionService.class);
                if (loc != null)
                    mServiceIntent.putExtra("uid", uId);
                    mServiceIntent.putExtra("location", loc);
                context.startService(mServiceIntent);
            }
            //Toast.makeText(context, "Teste alarme manager", Toast.LENGTH_LONG).show();

        }

    }
}
