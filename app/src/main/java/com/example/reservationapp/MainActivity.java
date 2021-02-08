package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {

    Button btnEditAccount, btnSchedules, btnPickUps;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hook up view fields
        btnEditAccount = findViewById(R.id.btnEditAccount);
        btnSchedules = findViewById(R.id.btnSchedules);
        btnPickUps = findViewById(R.id.btnPickUps);
        lstShuttles = findViewById(R.id.lstShuttles);
        spnDestination = findViewById(R.id.spnDestination);

        studentId = getIntent().getStringExtra("studentId");

        //gets references to database
        refRoot = FirebaseDatabase.getInstance().getReference();
        refLocations = refRoot.child("Tracking");
        refDrivers = refRoot.child("Drivers");
        refDestinations = refRoot.child("Destinations");

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
                ShowToast(destinationArr[destinationSelectIndex].getId());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //triggered whenever an item in the list is clicked
        lstShuttles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ShuttleActivity.class);
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

        btnPickUps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PickUpsActivity.class);
                startActivity(intent);
            }
        });


        btnSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SchedulesActivity.class);
                startActivity(intent);
            }
        });

    }//end of on create

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
    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
