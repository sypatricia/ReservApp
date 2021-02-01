package com.example.reservationapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentPickups#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentPickups extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

    View rootView;

    public FragmentPickups() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentPickups.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentPickups newInstance(String param1, String param2) {
        FragmentPickups fragment = new FragmentPickups();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pickups, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.frgMap2);
        mapFragment.getMapAsync(this);

        txtAddress = rootView.findViewById(R.id.txtAddress2);
        spnPickUp = rootView.findViewById(R.id.spnPickUp2);

        refPickUps = FirebaseDatabase.getInstance().getReference("Stations");


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

        return rootView;
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