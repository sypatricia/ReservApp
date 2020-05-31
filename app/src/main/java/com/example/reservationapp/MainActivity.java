package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button btnEditAccount, btnSchedules;
    ListView lstShuttles;

    DatabaseReference refRoot;
    DatabaseReference refDrivers;
    DatabaseReference refLocations;
    DatabaseReference refDestinations;

    FirebaseListOptions<ModelLocation> options;
    ModelLocation[] shuttles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hook up view fields
        btnEditAccount = findViewById(R.id.btnEditAccount);
        btnSchedules = findViewById(R.id.btnSchedules);
        lstShuttles = findViewById(R.id.lstShuttles);

        //gets references to database
        refRoot = FirebaseDatabase.getInstance().getReference();
        refLocations = refRoot.child("Tracking");
        refDrivers = refRoot.child("Drivers");
        refDestinations = refRoot.child("Destinations");

        updateList();

    }//end of on create

    public void updateList(){
        //listens for changes in tracking table
        refLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String count  = String.valueOf(dataSnapshot.getChildrenCount());
                shuttles = new ModelLocation[ Integer.valueOf(count)];

                //build options for the firebase list adapter
                options = new FirebaseListOptions.Builder<ModelLocation>().setQuery(refLocations, ModelLocation.class).setLayout(R.layout.list_item_shuttle).build();

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
                        //sets the destination name in the list item using retrieved driver info
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
}
