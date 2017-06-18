package com.example.paulm.travelapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import models.Country;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Country destCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // The passed Country Object
        destCountry = (Country) getIntent().getSerializableExtra("destCountry");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    // Method edited from built in Android Studio Map Activity Template
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng ireland = new LatLng(53.0,-8.0);
        mMap.addMarker(new MarkerOptions().position(ireland).title("Marker in Ireland"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ireland));

        LatLng destination = new LatLng(destCountry.getLat(),destCountry.getLng());
        mMap.addMarker(new MarkerOptions().position(destination).title("Marker in " + destCountry.getName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));

        // PolylineOptions taken from stackoverflow.com
        PolylineOptions line= new PolylineOptions().add(new LatLng(53.0,-8.0), destination).width(5).color(Color.RED);
        mMap.addPolyline(line);
    }
}
