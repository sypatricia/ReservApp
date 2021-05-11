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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShuttleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener  {

    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PEMISSIONS_FINE_LOCATION = 99;

    TextView txtDriverName, txtStatus, txtDestination, txtCapacity, txtReserved, txtFrom, txtSchedule, txtEstimated;
    Button btnReserve;

    GoogleMap gMap;
    Marker markerLocation, markerDestination;

    String studentId;
    boolean reserved = false;
    boolean reservedInOther = false;
    String id, driverName, status, destinationId, destinationName = "", transitId, fromId, fromName, schedule, scheduleId, estimated = "";
    double locationLatitude, locationLongitude, destinationLatitude, destinationLongitude;
    int reservations = 0, capacity = 0, hour = 0, currentHour;

    LatLng shuttleLocation, destinationLocation;

    DatabaseReference refRoot;
    DatabaseReference refDriver;
    DatabaseReference refLocation;
    //DatabaseReference refReservations;
    DatabaseReference refStudent;
    DatabaseReference refTransit;
    DatabaseReference refSchedule;
    DatabaseReference refReservations;
    ValueEventListener locationListener = null;
    ValueEventListener reservationsListener = null;
    ValueEventListener studReservationListener = null;
    private List<Polyline> polylines = null;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    @Override
    protected void onResume() {
        super.onResume();
        startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeLocationListener();
        removeReservationListener();
        removeStudentReservationListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeLocationListener();
        removeReservationListener();
        removeStudentReservationListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocationListener();
        removeReservationListener();
        removeStudentReservationListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.frgMap);
        mapFragment.getMapAsync(this);

        btnReserve = findViewById(R.id.btnReserve);
        txtDriverName = findViewById(R.id.txtDriverName);
        txtStatus = findViewById(R.id.txtStatus);
        txtDestination = findViewById(R.id.txtDestination);
        txtCapacity = findViewById(R.id.txtCapacity);
        txtReserved = findViewById(R.id.txtReserved);
        txtFrom = findViewById(R.id.txtFrom);
        txtSchedule = findViewById(R.id.txtSchedule);
        txtEstimated = findViewById(R.id.txtEstimated);

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
        //refReservations = refRoot.child("Reservations/" + id);
        refStudent = refRoot.child("Students/" + studentId);

        startListening();

        refDriver.addListenerForSingleValueEvent(new ValueEventListener() {
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

        btnReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentHour > hour){
                    Toast.makeText(ShuttleActivity.this,"Cannot reserve to transit past its departure time", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!reserved){
                    if(reservations >= capacity){
                        Toast.makeText(ShuttleActivity.this,"This shuttle is full", Toast.LENGTH_LONG).show();
                    }
                    else{
                        refTransit.child("reservations").child(studentId).setValue("Reserved");
                        refStudent.child("reservations").child(transitId).setValue(hour);
                        reserved = true;
                        Toast.makeText(ShuttleActivity.this,"You are now reserved for this shuttle", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    refTransit.child("reservations").child(studentId).removeValue();
                    refStudent.child("reservations").child(transitId).removeValue();
                    reserved = false;
                    Toast.makeText(ShuttleActivity.this,"You have cancelled the reservation", Toast.LENGTH_LONG).show();
                }

                updateInfo();
                startListening();


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

//        if(reserved){
//            refReservations.child(studentId).child("latitude").setValue(latitude);
//            refReservations.child(studentId).child("longitude").setValue(longitude);
//            refReservations.child(studentId).child("accuracy").setValue(accuracy);
//            refReservations.child(studentId).child("altitude").setValue(altitude);
//            refReservations.child(studentId).child("speed").setValue(speed);
//            refReservations.child(studentId).child("address").setValue(address);
//        }

    }


    void removeLocationListener(){
        if(locationListener != null){
            refLocation.orderByValue().removeEventListener(locationListener);
            locationListener = null;
        }
    }
    void removeReservationListener(){
        if(reservationsListener != null){
            refReservations.orderByValue().removeEventListener(reservationsListener);
            reservationsListener = null;
        }
    }
    void removeStudentReservationListener(){
        if(studReservationListener != null){
            refStudent.child("reservations").removeEventListener(studReservationListener);
            studReservationListener = null;
        }
    }

    public void startListening(){
        removeLocationListener();
        locationListener = refLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    finish();
                }
                else{
                    if(dataSnapshot.child("destination").exists()){
                        destinationId = dataSnapshot.child("destination").getValue().toString();
                        fromId = dataSnapshot.child("from").getValue().toString();
                        status = dataSnapshot.child("status").getValue().toString();
                        final DatabaseReference destination = refRoot.child("Stations/"+ destinationId);
                        final DatabaseReference from = refRoot.child("Stations/"+ fromId);
                        transitId = dataSnapshot.child("transit").getValue().toString();
                        refTransit = refRoot.child("Transits").child(transitId);

                        if(transitId.equals("none")){
                            fromName = "";
                            destinationName = "";
                            estimated = "";
                        }
                        else{

                            destination.addListenerForSingleValueEvent(new ValueEventListener() {
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

                            from.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    fromName = dataSnapshot.child("name").getValue().toString();
                                    updateInfo();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            refReservations = refTransit.child("reservations");
                            removeReservationListener();
                            reservationsListener = refReservations.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    reservations = (int)dataSnapshot.getChildrenCount();
                                    if(dataSnapshot.child(studentId).exists()){
                                        String status = dataSnapshot.child(studentId).getValue().toString();
                                        txtReserved.setText(status);
                                    }
                                    else{
                                        txtReserved.setText("");
                                    }
                                    updateInfo();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            refTransit.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String schedId = dataSnapshot.child("sched").getValue().toString();

                                    refSchedule = refRoot.child("Schedules").child(schedId);

                                    refSchedule.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int h = Integer.valueOf(dataSnapshot.child("hour").getValue().toString()) ;
                                            int m = Integer.valueOf(dataSnapshot.child("minute").getValue().toString());

                                            Date currentTime = Calendar.getInstance().getTime();
                                            Calendar cal = Calendar.getInstance();
                                            cal.setTime(currentTime);
                                            int currentHours = cal.get(Calendar.HOUR_OF_DAY);
                                            int currentMins = cal.get(Calendar.MINUTE);

                                            if(status.equals("Waiting")){
                                                estimated = "ETD " + (((h*60) + m) - ((currentHours*60) + currentMins)) + " min(s)";
                                            }
                                            else{

                                            }

                                            currentHour = Integer.parseInt(new DecimalFormat("00").format(currentHours) + new DecimalFormat("00").format(currentMins));
                                            hour = Integer.parseInt(new DecimalFormat("00").format(h) + new DecimalFormat("00").format(m));


                                            final SimpleDateFormat f24hours = new SimpleDateFormat("HH:mm");

                                            String time = h + ":" + m;

                                            final Date date;
                                            try {
                                                date = f24hours.parse(time);

                                                final SimpleDateFormat f12hours = new SimpleDateFormat("hh:mm aa");

                                                final String time12hr = f12hours.format(date);

                                                schedule = time12hr;
                                                updateInfo();

                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            removeStudentReservationListener();
                                            studReservationListener = refStudent.child("reservations").orderByValue().equalTo(hour).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull final DataSnapshot reservationSnapshot) {


                                                    if(reservationSnapshot.exists()){
                                                        if(reservationSnapshot.hasChild(transitId)){
                                                            reserved = true;
                                                        }
                                                        else{
                                                            for(DataSnapshot reservation : reservationSnapshot.getChildren()){
                                                                if(reservation.exists()){
                                                                    reservedInOther = true;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    else{
                                                        reserved = false;
                                                        reservedInOther = false;
                                                    }


                                                    updateInfo();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                        updateInfo();
                        updateMarkers();
                    }
                    else{
                        refLocation.removeEventListener(this);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateInfo(){
        txtDriverName.setText(driverName);
        txtStatus.setText("Status: " + status);
        txtCapacity.setText(reservations + "/" + capacity);
        txtDestination.setText(destinationName);
        txtFrom.setText(fromName);
        txtSchedule.setText(schedule);
        txtEstimated.setText(estimated);

        //if shuttle is waiting and cap is not full
        if(status.equals("Waiting") && reservations < capacity){
            btnReserve.setEnabled(true);
        }

        if(!reserved){//if student is not reserved
            btnReserve.setText("Reserve Seat");
            btnReserve.setEnabled(true);
            txtReserved.setText("");

            if( reservations >= capacity){
                btnReserve.setEnabled(false);
                btnReserve.setText("Full");
            }
        }
        else {

            btnReserve.setEnabled(true);
            btnReserve.setText("Cancel Reservation");
            //txtReserved.setText("Reserved");

            if(reservedInOther){//if student is reserved in a different shuttle
                btnReserve.setText("Reserve Seat");
                txtReserved.setText("");
                btnReserve.setEnabled(false);
            }
        }

        //if shuttle isnt waiting
        if(!status.equals("Waiting")){
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

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

}
