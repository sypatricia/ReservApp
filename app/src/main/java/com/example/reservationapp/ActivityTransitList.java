package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ActivityTransitList extends AppCompatActivity {

    TextView txtTime;
    ListView lstTransits;
    Spinner spnStation;

    String schedId;

    DatabaseReference refRoot, refTransits, refSchedule, refDesinations, refDriver;

    FirebaseListOptions<ScheduleModel> options;

    ArrayList<ModelTransit> arrTansits = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_list);

        txtTime = findViewById(R.id.txtTime);
        lstTransits = findViewById(R.id.lstTransits);
        spnStation = findViewById(R.id.spnStation);

        schedId = getIntent().getStringExtra("schedId");

        refRoot = FirebaseDatabase.getInstance().getReference();
        refDriver = refRoot.child("Drivers/");
        refTransits = refRoot.child("Transits");
        refSchedule = refRoot.child("Schedules/" + schedId);
        refDesinations = refRoot.child("Stations");

        options = new FirebaseListOptions.Builder<ScheduleModel>().setQuery(refSchedule.child("transits"), ScheduleModel.class).setLayout(R.layout.list_item_transit).build();

        FirebaseListAdapter<ScheduleModel> firebaseListAdapter = new FirebaseListAdapter<ScheduleModel>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull final ScheduleModel sched, int position) {

                DatabaseReference itemRef = getRef(position);

                final ModelTransit modelTransit = new ModelTransit();
                modelTransit.setId(itemRef.getKey());

                final TextView txtName = v.findViewById(R.id.txtName);
                final TextView txtFrom = v.findViewById(R.id.txtFrom);
                final TextView txtTo = v.findViewById(R.id.txtTo);

                refTransits.child(itemRef.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            modelTransit.setDriver(dataSnapshot.child("driver").getValue().toString());
                            modelTransit.setFrom(dataSnapshot.child("from").getValue().toString());
                            modelTransit.setTo(dataSnapshot.child("to").getValue().toString());
                            modelTransit.setSched(dataSnapshot.child("sched").getValue().toString());

                            refDriver.child(modelTransit.getDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();

                                    txtName.setText(name);

                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    //ShowToast("Failed to read database");
                                }
                            });

                            refDesinations.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()){

                                        String from =  dataSnapshot.child(modelTransit.getFrom()).child("name").getValue().toString();
                                        String to =  dataSnapshot.child(modelTransit.getTo()).child("name").getValue().toString();

                                        txtFrom.setText(from);
                                        txtTo.setText(to);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    //ShowToast("Failed to read database");
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                arrTansits.add(modelTransit);

            }
        };

        firebaseListAdapter.startListening();
        lstTransits.setAdapter(firebaseListAdapter);

        refSchedule.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String time =  dataSnapshot.child("hour").getValue() + ":" + dataSnapshot.child("minute").getValue();
                SimpleDateFormat f24hours = new SimpleDateFormat("HH:mm");
                final Date date;

                try {
                    date = f24hours.parse(time);
                    final SimpleDateFormat f12hours = new SimpleDateFormat("hh:mm aa");
                    final String time12hr = f12hours.format(date);

                    txtTime.setText(time12hr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}