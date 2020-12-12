package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShuttleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener  {

    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PEMISSIONS_FINE_LOCATION = 99;

    TextView txtDriverName, txtStatus, txtDestination, txtCapacity, txtReserved;
    Button btnBack, btnReserve;

    GoogleMap gMap;
    Marker markerLocation, markerDestination;

    String studentId, studentName;
    boolean reserved = false;
    boolean reservedInOther = false;
    String id, driverName, status, destinationId, destinationName = "";
    double locationLatitude, locationLongitude, destinationLatitude, destinationLongitude;
    int reservations = 0, capacity = 0;

    LatLng shuttleLocation, destinationLocation;

    DatabaseReference refRoot;
    DatabaseReference refDriver;
    DatabaseReference refLocation;
    DatabaseReference refReservations;
    DatabaseReference refStudent;
    private List<Polyline> polylines = null;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

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
        txtReserved = findViewById(R.id.txtReserved);

        studentId = getIntent().getStringExtra("studentId");

        id = getIntent().getStringExtra("id");
        driverName = getIntent().getStringExtra("driverName");
        status = getIntent().getStringExtra("status");
        destinationId = getIntent().getStringExtra("destination");
        locationLatitude = getIntent().getDoubleExtra("locationLatitude",14.2439236);
        locationLongitude = getIntent().getDoubleExtra("locationLongitude",121.1123045);

        shuttleLocation = new LatLng(locationLatitude, locationLongitude);

        updateInfo();

        refRoot = FirebaseDatabase.getInstance().getReference();
        refLocation = refRoot.child("Tracking/" + id);
        refDriver = refRoot.child("Drivers/" + id);
        refReservations = refRoot.child("Reservations/" + id);
        refStudent = refRoot.child("Students/" + studentId);

        refStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentName = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();

                if(!dataSnapshot.hasChild("reserved")) {//if student is not reserved to anyone
                    reserved = false;
                    reservedInOther = false;
                }
                else {
                    reserved = true;
                    if(dataSnapshot.child("reserved").getValue().toString().equals(id)){
                        reservedInOther = false;
                    }
                    else
                        reservedInOther = true;
                }


                updateInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
                            destinationLocation = new LatLng(destinationLatitude, destinationLongitude);
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
                if(!dataSnapshot.hasChild(studentId)){
                    reserved = false;
                    reservedInOther = false;
                    refStudent.child("reserved").removeValue();
                }
                updateInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateReserveLocation(locationResult.getLastLocation());
            }
        };

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        btnReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status.equals("Waiting")){
                    if(!reserved){//if student clicks reserve
                        refReservations.child(studentId).child("name").setValue(studentName);
                        refStudent.child("reserved").setValue(id);
                        startLocationUpdates();

                        Toast.makeText(ShuttleActivity.this,"You are now reserved for this shuttle", Toast.LENGTH_LONG).show();

                    }
                    else{//if student clicks cancel
                        stopLocationUpdates();
                        refReservations.child(studentId).removeValue();
                        refStudent.child("reserved").removeValue();

                        Toast.makeText(ShuttleActivity.this,"You have cancelled the reservation", Toast.LENGTH_LONG).show();

                    }
                    updateInfo();
                }
            }
        });

        updateGPS();
    }//end of onCreate

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(locationLatitude,locationLongitude) , 16f) );

        updateMarkers();
    }

    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void updateGPS(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ShuttleActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateReserveLocation(location);
                }
            });
        }
        else{
            //permission not granted yet

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PEMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateReserveLocation(Location location){
        //update student in firebase database

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final float accuracy = location.getAccuracy();
        String altitude;
        String speed;
        String address;

        if(location.hasAltitude()){
            altitude = String.valueOf(location.getAltitude());
        }
        else altitude = "Not Available";

        if(location.hasSpeed()){
            speed = String.valueOf(location.getSpeed());
        }
        else speed = "Not Available";

        Geocoder geocoder = new Geocoder(ShuttleActivity.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        }
        catch (Exception e){
            address = "Unable to get street address";
        }

        if(reserved){
            refReservations.child(studentId).child("latitude").setValue(latitude);
            refReservations.child(studentId).child("longitude").setValue(longitude);
            refReservations.child(studentId).child("accuracy").setValue(accuracy);
            refReservations.child(studentId).child("altitude").setValue(altitude);
            refReservations.child(studentId).child("speed").setValue(speed);
            refReservations.child(studentId).child("address").setValue(address);
        }

    }

    public void updateInfo(){
        txtDriverName.setText(driverName);
        txtStatus.setText("Status: " + status);
        txtCapacity.setText(reservations + "/" + capacity);
        txtDestination.setText("Destination: " + destinationName);

        //if shuttle is waiting and cap is not full
        if(status.equals("Waiting") && reservations < capacity){
            btnReserve.setEnabled(true);
            btnBack.setEnabled(true);
        }

        if(!reserved){//if student is not reserved
            btnReserve.setText("Reserve Seat");
            btnReserve.setEnabled(true);
            btnBack.setEnabled(true);
            txtReserved.setText("");
        }
        else {
            btnBack.setEnabled(false);
            btnReserve.setText("Cancel Reservation");
            txtReserved.setText("Reserved");

            if(reservedInOther){//if student is reserved in a different shuttle
                btnReserve.setText("Reserve Seat");
                txtReserved.setText("");
                btnReserve.setEnabled(false);
                btnBack.setEnabled(true);
            }
        }

        //if shuttle isnt waiting or cap is full
        if(!status.equals("Waiting") || reservations >= capacity){
            btnReserve.setEnabled(false);
            btnReserve.setText("In Transit");
        }

    }

    public void updateMarkers(){

        if(gMap != null){

            if (markerLocation != null){
                markerLocation.remove();
            }
            if (markerDestination != null){
                markerDestination.remove();
            }

            BitmapDescriptor descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

            if(status.equals("Waiting")){
                descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }

            //add marker for shuttle location
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(descriptor);
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

            //finding route is commented out due unauthorized API key from paid directions service
            //Findroutes(shuttleLocation, destinationLocation);

        }

    }

    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(ShuttleActivity.this,"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyACKj8z0tkOsTpJqmg529lfxmulGPnRVf0")
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(ShuttleActivity.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = gMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {

            }

            //Add Marker on route starting position
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(polylineStartLatLng);
            startMarker.title("My Location");
            gMap.addMarker(startMarker);

            //Add Marker on route ending position
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(polylineEndLatLng);
            endMarker.title("Destination");
            gMap.addMarker(endMarker);

        }
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(shuttleLocation, destinationLocation);
    }

}
