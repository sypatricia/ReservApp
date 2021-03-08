package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityTransitInfo extends AppCompatActivity {

    TextView txtName, txtSchedule, txtFrom, txtTo, txtReserved;
    Button btnReserve;

    String studentId, transitId, driverId, schedId, fromId, toId;
    int capCount, resCount, hour;
    boolean reservedHere = false, reservedDiff = false;

    DatabaseReference refRoot, refTransit, refDriver, refSchedule, refStations, refStudent;

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

        studentId = getIntent().getStringExtra("studentId");
        transitId = getIntent().getStringExtra("transitId");
        driverId = getIntent().getStringExtra("driverId");
        schedId = getIntent().getStringExtra("schedId");
        fromId = getIntent().getStringExtra("fromId");
        toId = getIntent().getStringExtra("toId");

        refRoot = FirebaseDatabase.getInstance().getReference();
        refStudent = refRoot.child("Students/" + studentId);
        refTransit = refRoot.child("Transits/" + transitId);
        refDriver = refRoot.child("Drivers/" + driverId);
        refSchedule = refRoot.child("Schedules/" + schedId);
        refStations = refRoot.child("Stations");

        refTransit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                resCount = (int)dataSnapshot.child("reservations").getChildrenCount();
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
//                int h = (int)dataSnapshot.child("hour").getValue();
//                int m = (int)dataSnapshot.child("minute").getValue();

                hour = Integer.parseInt(new DecimalFormat("00").format(dataSnapshot.child("hour").getValue()) + new DecimalFormat("00").format(dataSnapshot.child("minute").getValue()));

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

        refStudent.child("reservations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //checks if already has a reservation with same time
                for(DataSnapshot reservation : dataSnapshot.getChildren()){

                    if(!reservation.exists()){
                        reservedHere = false;
                        reservedDiff = false;
                    }
                    else if(transitId.equals(reservation.getKey())){
                        reservedHere = true;
                    }
                    else if(reservation.getValue().equals(hour)){
                        reservedDiff = true;
                    }
                }

                updateReserveButton();

//                if(!reserved){
//
//                    btnReserve.setEnabled(false);
//                    btnReserve.setText("You are reserved in another transit");
//                    //reserve student
//                }
//                else Toast.makeText(ActivityTransitInfo.this,"You already have a reservation for this time schedule", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo: add checking of waiting/intransit status if transit is current transit

                if(resCount >= capCount){
                    Toast.makeText(ActivityTransitInfo.this,"This shuttle is full", Toast.LENGTH_LONG).show();
                }
                else if(!reservedHere){
                    refTransit.child("reservations").child(studentId).setValue(hour);
                    refStudent.child("reservations").child(transitId).setValue(hour);
                    reservedHere = true;
                    Toast.makeText(ActivityTransitInfo.this,"You are now reserved for this shuttle", Toast.LENGTH_LONG).show();
                }
                else{
                    refTransit.child("reservations").child(studentId).removeValue();
                    refStudent.child("reservations").child(transitId).removeValue();
                    reservedHere = false;
                    Toast.makeText(ActivityTransitInfo.this,"You have cancelled the reservation", Toast.LENGTH_LONG).show();
                }
                updateReserveButton();

//                if(status.equals("Waiting")){
//                    if(!reserved){//if student is not reserve
//                        refReservations.child(studentId).child("name").setValue(studentName);
//                        refStudent.child("reserved").setValue(id);
//                        startLocationUpdates();
//
//                        //Toast.makeText(ShuttleActivity.this,"You are now reserved for this shuttle", Toast.LENGTH_LONG).show();
//
//                    }
//                    else{//if student clicks cancel
//                        stopLocationUpdates();
//                        refReservations.child(studentId).removeValue();
//                        refStudent.child("reserved").removeValue();
//
//                        //Toast.makeText(ShuttleActivity.this,"You have cancelled the reservation", Toast.LENGTH_LONG).show();
//
//                    }
//                    updateInfo();
//                }
            }
        });

    }

    void UpdateResCap(){
        String resCap = resCount + "/" + capCount;
        txtReserved.setText(resCap);
    }

    void updateReserveButton(){
        if(reservedDiff){
            btnReserve.setEnabled(false);
            btnReserve.setText("You are already reserved");
        }
        else if(reservedHere){
            btnReserve.setText("Cancel Reservation");
        }
        else {
            btnReserve.setText("Make Reservation");
        }

    }
}