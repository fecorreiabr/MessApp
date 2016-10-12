package br.iesb.messapp.model;

import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

/**
 * Created by Felipe on 10/10/2016.
 */
public class Location implements Serializable {
    private String id;
    private double lat, lng;

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(lat, lng);
    }

    public Location() {
    }

    public Location(String id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }
}
