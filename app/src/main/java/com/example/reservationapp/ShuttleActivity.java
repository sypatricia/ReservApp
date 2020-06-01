package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShuttleActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView txtDriverName, txtStatus, txtDestination, txtCapacity;
    Button btnBack, btnReserve;

    GoogleMap gMap;
    Marker markerLocation, markerDestination;

    String id, driverName, status, destinationId, destinationName = "";
    double locationLatitude, locationLongitude, destinationLatitude, destinationLongitude;
    int reservations = 0, capacity = 0;

    DatabaseReference refRoot;
    DatabaseReference refDriver;
    DatabaseReference refLocation;
    DatabaseReference refReservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.frgMap);
        mapFragment.getMapAsync(this);

        btnBack = findViewById(R.id.btnBack);
        btnReserve = findViewById(R.id.btnReserve);
        txtDriverName = findViewById(R.id.txtDriverName);
        txtStatus = findViewById(R.id.txtStatus);
        txtDestination = findViewById(R.id.txtDestination);
        txtCapacity = findViewById(R.id.txtCapacity);

        id = getIntent().getStringExtra("id");
        driverName = getIntent().getStringExtra("driverName");
        status = getIntent().getStringExtra("status");
        destinationId = getIntent().getStringExtra("destination");
        locationLatitude = getIntent().getDoubleExtra("locationLatitude",14.2439236);
        locationLongitude = getIntent().getDoubleExtra("locationLongitude",121.1123045);

        updateInfo();

        refRoot = FirebaseDatabase.getInstance().getReference();
        refLocation = refRoot.child("Tracking/" + id);
        refDriver = refRoot.child("Drivers/" + id);
        refReservations = refRoot.child("Reservations/" + id);

        refLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("latitude")){
                    finish();
                }
                else{
                    destinationId = dataSnapshot.child("destination").getValue().toString();
                    status = dataSnapshot.child("status").getValue().toString();
                    final DatabaseReference destination = refRoot.child("Destinations/"+ destinationId);

                    destination.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            destinationName = dataSnapshot.child("name").getValue().toString();
                            destinationLatitude = Double.valueOf(dataSnapshot.child("latitude").getValue().toString());
                            destinationLongitude = Double.valueOf(dataSnapshot.child("longitude").getValue().toString());
                            updateInfo();
                            updateMarkers();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    updateInfo();
                    updateMarkers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverName = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();
                capacity = Integer.valueOf(dataSnapshot.child("capacity").getValue().toString());
                updateInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refReservations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservations = (int)dataSnapshot.getChildrenCount();
                updateInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(locationLatitude,locationLongitude) , 16f) );

        updateMarkers();
    }

    public void updateInfo(){
        txtDriverName.setText(driverName);
        txtStatus.setText("Status: " + status);
        txtCapacity.setText(reservations + "/" + capacity);
        txtDestination.setText("Destination: " + destinationName);
    }

    public void updateMarkers(){

        if(gMap != null){

            if (markerLocation != null){
                markerLocation.remove();
            }
            if (markerDestination != null){
                markerDestination.remove();
            }

            //add marker for shuttle location
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(locationLatitude,locationLongitude));
            markerOptions.title(driverName);
            gMap.clear();
            gMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(locationLatitude,locationLongitude)));
            markerLocation = gMap.addMarker(markerOptions);


            //add marker for destination location
            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(destinationLatitude,destinationLongitude));
            markerOptions.title(destinationName);
            markerDestination = gMap.addMarker(markerOptions);

        }

    }

}
