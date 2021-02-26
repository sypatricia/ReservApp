package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityTransitInfo extends AppCompatActivity {

    TextView txtName, txtSchedule, txtFrom, txtTo, txtReserved;
    Button btnReserve;

    String transitId, driverId, schedId, fromId, toId;
    int capCount, resCount;

    DatabaseReference refRoot, refTransit, refDriver, refSchedule, refStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_info);

        txtName = findViewById(R.id.txtName);
        txtSchedule = findViewById(R.id.txtSchedule);
        txtFrom = findViewById(R.id.txtFrom);
        txtTo = findViewById(R.id.txtTo);
        txtReserved = findViewById(R.id.txtReserved);
        btnReserve = findViewById(R.id.btnReserve);

        transitId = getIntent().getStringExtra("transitId");
        driverId = getIntent().getStringExtra("driverId");
        schedId = getIntent().getStringExtra("schedId");
        fromId = getIntent().getStringExtra("fromId");
        toId = getIntent().getStringExtra("toId");

        refRoot = FirebaseDatabase.getInstance().getReference();
        refTransit = refRoot.child("Transits/" + transitId);
        refDriver = refRoot.child("Drivers/" + driverId);
        refSchedule = refRoot.child("Schedules/" + schedId);
        refStations = refRoot.child("Stations");

        refTransit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                resCount = (int)dataSnapshot.child("transits").getChildrenCount();
                UpdateResCap();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();
                capCount = Integer.parseInt(dataSnapshot.child("capacity").getValue().toString());

                txtName.setText(name);
                UpdateResCap();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refSchedule.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //int hour = Integer.parseInt(dataSnapshot.child("hour").getValue().toString());

                String time = dataSnapshot.child("hour").getValue() + ":" + dataSnapshot.child("minute").getValue();
                SimpleDateFormat f24hours = new SimpleDateFormat("HH:mm");

                final Date date;
                try {
                    date = f24hours.parse(time);

                    final SimpleDateFormat f12hours = new SimpleDateFormat("hh:mm aa");

                    final String time12hr = f12hours.format(date);

                    txtSchedule.setText(time12hr);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refStations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String from = dataSnapshot.child(fromId).child("name").getValue().toString();
                String to = dataSnapshot.child(toId).child("name").getValue().toString();

                txtFrom.setText(from);
                txtTo.setText(to);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void UpdateResCap(){
        String resCap = resCount + "/" + capCount;
        txtReserved.setText(resCap);
    }
}