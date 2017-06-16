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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng ireland = new LatLng(53.0,-8.0);
        mMap.addMarker(new MarkerOptions().position(ireland).title("Marker in Ireland"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ireland));

        LatLng australia = new LatLng(-27.0,133.0);
        mMap.addMarker(new MarkerOptions().position(australia).title("Marker in Australia"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(australia));

        PolylineOptions line= new PolylineOptions().add(new LatLng(53.0,-8.0), new LatLng(-27.0,133.0)).width(5).color(Color.RED);

        mMap.addPolyline(line);
    }
}
