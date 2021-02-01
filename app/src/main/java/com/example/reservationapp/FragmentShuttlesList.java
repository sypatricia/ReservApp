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

    ListView lstShuttles;
    Spinner spnDestination;

    DatabaseReference refRoot;
    DatabaseReference refDrivers;
    DatabaseReference refLocations;
    DatabaseReference refDestinations;

    FirebaseListOptions<ModelLocation> options;
    ModelLocation[] shuttles;

    FirebaseListOptions<ModelDestination> optionsDestination;
    ModelDestination[] destinationArr;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_shuttles_list, container, false);

        //hook up view fields
        lstShuttles = rootView.findViewById(R.id.lstShuttles2);
        spnDestination = rootView.findViewById(R.id.spnDestination2);

        studentId = getActivity().getIntent().getStringExtra("studentId");

        //gets references to database
        refRoot = FirebaseDatabase.getInstance().getReference();
        refLocations = refRoot.child("Tracking");
        refDrivers = refRoot.child("Drivers");
        refDestinations = refRoot.child("Stations");

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
                intent.putExtra("id", shuttles[i].getId());
                intent.putExtra("status", shuttles[i].getStatus());
                intent.putExtra("destination", shuttles[i].getDestination());
                intent.putExtra("locationLatitude", shuttles[i].getLatitude());
                intent.putExtra("locationLatitude", shuttles[i].getLatitude());
                intent.putExtra("locationLongitude", shuttles[i].getLongitude());
                TextView txtDriver = view.findViewById(R.id.txtDriverName);
                String driverName = txtDriver.getText().toString();
                intent.putExtra("driverName", driverName);

                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateList(){
        //listens for changes in tracking table
        refLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String count  = String.valueOf(dataSnapshot.getChildrenCount());
                shuttles = new ModelLocation[ Integer.valueOf(count)];

                //build options for the firebase list adapter
                options = new FirebaseListOptions.Builder<ModelLocation>().setQuery(refLocations.orderByChild("destination").equalTo(destinationArr[destinationSelectIndex].getId()), ModelLocation.class).setLayout(R.layout.list_item_shuttle).build();

                FirebaseListAdapter<ModelLocation> firebaseListAdapter = new FirebaseListAdapter<ModelLocation>(options) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull ModelLocation model, int position) {

                        DatabaseReference itemRef = getRef(position);
                        //gets info from driver and destination table using the linked IDs
                        DatabaseReference driver = refDrivers.child(itemRef.getKey());
                        DatabaseReference destination = refDestinations.child(model.getDestination());
                        //gets the view text fields
                        final TextView txtDriverName = v.findViewById(R.id.txtDriverName);
                        final TextView txtDestination = v.findViewById(R.id.txtDestination);
                        TextView txtStatus = v.findViewById(R.id.txtStatus);

                        //sets the driver name   in the list item using retrieved driver info
                        driver.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String fName = dataSnapshot.child("firstName").getValue().toString();
                                String lName = dataSnapshot.child("lastName").getValue().toString();
                                txtDriverName.setText(fName + " " + lName);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //sets the destination name in the list item using retrieved destination info
                        destination.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                txtDestination.setText("To: " + dataSnapshot.child("name").getValue());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        txtStatus.setText(String.valueOf(model.getStatus()));

                        //store ids in array to pass in shuttle activity
                        shuttles[position] = new ModelLocation();
                        shuttles[position].setId(itemRef.getKey());
                        shuttles[position].setStatus(model.getStatus());
                        shuttles[position].setLatitude(model.getLatitude());
                        shuttles[position].setLongitude(model.getLongitude());
                        shuttles[position].setDestination(model.getDestination());

                    }
                };

                firebaseListAdapter.startListening();
                lstShuttles.setAdapter(firebaseListAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void ShowToast(String message){ Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); }

}