package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PickUpsActivity extends AppCompatActivity  implements OnMapReadyCallback {

    TextView txtAddress;
    Spinner spnPickUp;

    GoogleMap gMap;
    FirebaseListOptions<ModelPickUp> optionsDestination;
    FirebaseListOptions<String> optionsReservation;
    ModelPickUp[] pickUpsArr;
    String locationName;
    double locationLatitude, locationLongitude;
    LatLng pickupLocation;
    Marker markerLocation;
    boolean isLoaded = false;

    DatabaseReference refPickUps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_ups);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.frgMap);
        mapFragment.getMapAsync(this);

        txtAddress = findViewById(R.id.txtAddress);
        spnPickUp = findViewById(R.id.spnPickUp);

        refPickUps = FirebaseDatabase.getInstance().getReference("PickUps");


        pickupLocation = new LatLng(locationLatitude, locationLatitude);

        refPickUps.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count  = String.valueOf(dataSnapshot.getChildrenCount());
                pickUpsArr = new ModelPickUp[ Integer.valueOf(count)];

                optionsDestination = new FirebaseListOptions.Builder<ModelPickUp>().setQuery(refPickUps, ModelPickUp.class).setLayout(R.layout.spinner_item_pickup).build();

                FirebaseListAdapter<ModelPickUp> firebaseListAdapter = new FirebaseListAdapter<ModelPickUp>(optionsDestination) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull ModelPickUp model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtName = v.findViewById(R.id.txtName);

                        txtName.setText(model.getName());
                        pickUpsArr[position] = new ModelPickUp();
                        pickUpsArr[position].setId(itemRef.getKey());
                        pickUpsArr[position].setName(model.getName());
                        pickUpsArr[position].setLatitude(model.getLatitude());
                        pickUpsArr[position].setLongitude(model.getLongitude());
                        pickUpsArr[position].setAddress(model.getAddress());
                    }
                };

                firebaseListAdapter.startListening();
                spnPickUp.setAdapter(firebaseListAdapter);
                isLoaded = true;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        spnPickUp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                locationLatitude = pickUpsArr[position].getLatitude();
                locationLongitude = pickUpsArr[position].getLongitude();
                locationName = pickUpsArr[position].getName();
                txtAddress.setText(pickUpsArr[position].getAddress());
                updateMarkers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(14.242588033306548,121.11294060945512) , 16f) );

        if(isLoaded){
            int select = spnPickUp.getSelectedItemPosition();
            locationLatitude = pickUpsArr[select].getLatitude();
            locationLongitude = pickUpsArr[select].getLongitude();
            locationName = pickUpsArr[select].getName();
            txtAddress.setText(pickUpsArr[select].getAddress());
            updateMarkers();
        }
    }

    public void updateMarkers(){

        if(gMap != null){

            if (markerLocation != null){
                markerLocation.remove();
            }

            //add marker for pickup location
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(locationLatitude,locationLongitude));
            markerOptions.title(locationName);
            gMap.clear();
            gMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(locationLatitude,locationLongitude)));
            markerLocation = gMap.addMarker(markerOptions);

        }

    }


}
