package com.example.reservationapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentShuttlesList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentShuttlesList extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View rootView;
    int count = 0, counter = 0;

    ListView lstShuttles;
    Spinner spnDestination;
    boolean initialized = false;

    DatabaseReference refRoot;
    DatabaseReference refDrivers;
    DatabaseReference refLocations;
    DatabaseReference refDestinations;;
    DatabaseReference refSchedules;
    ValueEventListener locationsListner = null;

    FirebaseListOptions<ModelLocation> options;
    ArrayList<ModelLocation> shuttles;

    FirebaseListOptions<ModelDestination> optionsDestination;
    ModelDestination[] destinationArr;

    String selectedId;

    String studentId;
    int destinationSelectIndex = 0;

    public FragmentShuttlesList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentShuttlesList.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentShuttlesList newInstance(String param1, String param2) {
        FragmentShuttlesList fragment = new FragmentShuttlesList();
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
    public void onResume() {
        super.onResume();
        if(initialized)
            updateList();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeLocationsListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeLocationsListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeLocationsListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_shuttles_list, container, false);

        //hook up view fields
        lstShuttles = rootView.findViewById(R.id.lstShuttles2);
        spnDestination = rootView.findViewById(R.id.spnDestination2);

        studentId = getActivity().getIntent().getStringExtra("studentId");

        //gets references to database
        refRoot = FirebaseDatabase.getInstance().getReference();
        refLocations = refRoot.child("Tracking");
        refDrivers = refRoot.child("Drivers");
        refDestinations = refRoot.child("Stations");
        refSchedules = refRoot.child("Schedules");


        refDestinations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = String.valueOf(dataSnapshot.getChildrenCount());
                destinationArr = new ModelDestination[Integer.valueOf(count)];

                optionsDestination = new FirebaseListOptions.Builder<ModelDestination>().setQuery(refDestinations, ModelDestination.class).setLayout(R.layout.spinner_item_destination).build();

                FirebaseListAdapter<ModelDestination> firebaseListAdapter = new FirebaseListAdapter<ModelDestination>(optionsDestination) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull ModelDestination model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtName = v.findViewById(R.id.txtDestinationName);

                        txtName.setText(model.getName());
                        destinationArr[position] = new ModelDestination();
                        destinationArr[position].setId(itemRef.getKey());
                        destinationArr[position].setName(model.getName());
                        destinationArr[position].setLatitude(model.getLatitude());
                        destinationArr[position].setLongitude(model.getLongitude());
                        destinationArr[position].setAddress(model.getAddress());

                    }
                };

                firebaseListAdapter.startListening();
                spnDestination.setAdapter(firebaseListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        spnDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                destinationSelectIndex = position;
                selectedId = destinationArr[position].getId();
                //refLocation.child("destination").setValue(destinationArr[destinationSelectIndex].getId());
                updateList();
                //ShowToast(destinationArr[destinationSelectIndex].getId());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //triggered whenever an item in the list is clicked
        lstShuttles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ShuttleActivity.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("id", shuttles.get(i).getId());
                intent.putExtra("status", shuttles.get(i).getStatus());
                intent.putExtra("destination", shuttles.get(i).getDestination());
                intent.putExtra("locationLatitude", shuttles.get(i).getLatitude());
                intent.putExtra("locationLatitude", shuttles.get(i).getLatitude());
                intent.putExtra("locationLongitude", shuttles.get(i).getLongitude());
                TextView txtDriver = view.findViewById(R.id.txtDriverName);
                String driverName = txtDriver.getText().toString();
                intent.putExtra("driverName", driverName);

                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    void removeLocationsListener(){
        if(locationsListner != null){
            refLocations.orderByChild("from").equalTo(selectedId).removeEventListener(locationsListner);
            locationsListner = null;
        }
    }

    public void updateList(){
        initialized = true;
        //listens for changes in tracking table
        removeLocationsListener();
        locationsListner = refLocations.orderByChild("from").equalTo(selectedId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot locationsSnapshot) {
                if(locationsSnapshot.exists()){
                    count = (int)locationsSnapshot.getChildrenCount();
                    counter = 0;
                    shuttles = new ArrayList<>();

                    final ArrayList<ModelShuttleListItem> listitems = new ArrayList<>();

                    for(DataSnapshot shuttle : locationsSnapshot.getChildren()){
                        final ModelLocation shuttleModel = new ModelLocation();
                        shuttleModel.setId(shuttle.getKey());
                        if(shuttle.child("status").exists())
                            shuttleModel.setStatus(shuttle.child("status").getValue().toString());
                        if(shuttle.child("latitude").exists())
                            shuttleModel.setLatitude((double)shuttle.child("latitude").getValue());
                        if(shuttle.child("longitude").exists())
                            shuttleModel.setLongitude((double)shuttle.child("longitude").getValue());
                        if(shuttle.child("destination").exists())
                            shuttleModel.setDestination(shuttle.child("destination").getValue().toString());
                        shuttles.add(shuttleModel);

                        final String status = shuttle.child("status").getValue().toString();

                        DatabaseReference driver = refDrivers.child(shuttle.getKey());
                        driver.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String fName = dataSnapshot.child("firstName").getValue().toString();
                                    String lName = dataSnapshot.child("lastName").getValue().toString();
                                    final String driverName = fName + " " + lName;

                                    DatabaseReference refDestination = refDestinations.child(shuttleModel.getDestination());
                                    refDestination.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                String destination = dataSnapshot.child("name").getValue().toString();

                                                listitems.add(new ModelShuttleListItem(driverName, status, destination));

                                                counter++;
                                                if(count == counter){
                                                    AdapterShuttlesList adapterTransitList = new AdapterShuttlesList(rootView.getContext(), R.layout.list_item_shuttle, listitems);
                                                    lstShuttles.setAdapter(adapterTransitList);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }
                else{
                    lstShuttles.setAdapter(null);
                }



                //String count  = String.valueOf(dataSnapshot.getChildrenCount());
//                shuttles = new ModelLocation[ Integer.valueOf(count)];

                //build options for the firebase list adapter
//                options = new FirebaseListOptions.Builder<ModelLocation>().setQuery(refLocations.orderByChild("from").equalTo(destinationArr[destinationSelectIndex].getId()), ModelLocation.class).setLayout(R.layout.list_item_shuttle).build();
//
//                FirebaseListAdapter<ModelLocation> firebaseListAdapter = new FirebaseListAdapter<ModelLocation>(options) {
//                    @Override
//                    protected void populateView(@NonNull View v, @NonNull ModelLocation model, int position) {
//
//                        DatabaseReference itemRef = getRef(position);
//                        //gets info from driver and destination table using the linked IDs
//                        DatabaseReference driver = refDrivers.child(itemRef.getKey());
//                        DatabaseReference destination = refDestinations.child(model.getDestination());
//                        //gets the view text fields
//                        final TextView txtDriverName = v.findViewById(R.id.txtDriverName);
//                        final TextView txtDestination = v.findViewById(R.id.txtDestination);
//                        TextView txtStatus = v.findViewById(R.id.txtStatus);
//
//                        //sets the driver name   in the list item using retrieved driver info
//                        driver.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                String fName = dataSnapshot.child("firstName").getValue().toString();
//                                String lName = dataSnapshot.child("lastName").getValue().toString();
//                                txtDriverName.setText(fName + " " + lName);
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                        //sets the destination name in the list item using retrieved destination info
//                        destination.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                txtDestination.setText("To: " + dataSnapshot.child("name").getValue());
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//
//                        txtStatus.setText(String.valueOf(model.getStatus()));
//
//                        //store ids in array to pass in shuttle activity
//                        shuttles[position] = new ModelLocation();
//                        shuttles[position].setId(itemRef.getKey());
//                        shuttles[position].setStatus(model.getStatus());
//                        shuttles[position].setLatitude(model.getLatitude());
//                        shuttles[position].setLongitude(model.getLongitude());
//                        shuttles[position].setDestination(model.getDestination());
//
//                    }
//                };
//
//                firebaseListAdapter.startListening();
//                lstShuttles.setAdapter(firebaseListAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}