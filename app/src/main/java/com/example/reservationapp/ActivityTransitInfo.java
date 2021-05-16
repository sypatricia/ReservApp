package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Calendar;
import java.util.Date;

public class ActivityTransitInfo extends AppCompatActivity {

    TextView txtName, txtPlateNum, txtSchedule, txtFrom, txtTo, txtReserved;
    Button btnReserve;
    ImageView btnBack;

    String studentId, transitId, driverId, schedId, fromId, toId;
    int capCount, resCount, hour, currentHour;
    boolean reservedHere = false, reservedDiff = false, inTransit = false, isFinished = false;

    DatabaseReference refRoot, refTransit, refDriver, refSchedule, refStations, refStudent;
    ValueEventListener transitListener = null;
    ValueEventListener studReservationListeener = null;

    @Override
    public void onResume() {
        super.onResume();
        getInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeTransitListener();
        removeReservationListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeTransitListener();
        removeReservationListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeTransitListener();
        removeReservationListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_info);

        txtName = findViewById(R.id.txtName);
        txtPlateNum = findViewById(R.id.txtPlateNum);
        txtSchedule = findViewById(R.id.txtSchedule);
        txtFrom = findViewById(R.id.txtFrom);
        txtTo = findViewById(R.id.txtTo);
        txtReserved = findViewById(R.id.txtReserved);
        btnReserve = findViewById(R.id.btnReserve);
        btnBack = findViewById(R.id.btnBack);

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

        getInfo();
        btnReserve.setEnabled(false);

        refStations.addListenerForSingleValueEvent(new ValueEventListener() {
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



        btnReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo: add checking of waiting/intransit status if transit is current transit
                if(!reservedHere){
                    if(resCount >= capCount){
                        Toast.makeText(ActivityTransitInfo.this,"This shuttle is full", Toast.LENGTH_LONG).show();
                    }
                    else{
                        refTransit.child("reservations").child(studentId).setValue("Reserved");
                        refStudent.child("reservations").child(transitId).setValue(hour);
                        reservedHere = true;
                        Toast.makeText(ActivityTransitInfo.this,"You are now reserved for this shuttle", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    refTransit.child("reservations").child(studentId).removeValue();
                    refStudent.child("reservations").child(transitId).removeValue();
                    reservedHere = false;
                    Toast.makeText(ActivityTransitInfo.this,"You have cancelled the reservation", Toast.LENGTH_LONG).show();
                }
                getInfo();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void removeTransitListener(){
        if(transitListener != null){
            refTransit.removeEventListener(transitListener);
            transitListener = null;
        }
    }

    void removeReservationListener(){
        if(studReservationListeener != null) {
            refStudent.child("reservations").orderByValue().equalTo(hour).removeEventListener(studReservationListeener);
            studReservationListeener = null;
        }
    }

    void getInfo(){
        removeTransitListener();
        transitListener = refTransit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                resCount = (int)dataSnapshot.child("reservations").getChildrenCount();
                UpdateResCap();

                refDriver.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();
                        String plateNum = dataSnapshot.child("plateNumber").getValue().toString();
                        capCount = Integer.parseInt(dataSnapshot.child("capacity").getValue().toString());

                        txtPlateNum.setText(plateNum);
                        txtName.setText(name);
                        UpdateResCap();

                        refSchedule.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                Date currentTime = Calendar.getInstance().getTime();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(currentTime);
                                int currentHours = cal.get(Calendar.HOUR_OF_DAY);
                                int currentMins = cal.get(Calendar.MINUTE);

                                currentHour = Integer.parseInt(new DecimalFormat("00").format(currentHours) + new DecimalFormat("00").format(currentMins));
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

                                removeReservationListener();
                                studReservationListeener = refStudent.child("reservations").orderByValue().equalTo(hour).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot reservationSnapshot) {
                                        if(reservationSnapshot.exists()){
                                            if(reservationSnapshot.hasChild(transitId)){
                                                reservedHere = true;
                                            }
                                            else{
                                                for(DataSnapshot reservation : reservationSnapshot.getChildren()){
                                                    if(reservation.exists()){
                                                        reservedDiff = true;
                                                    }
                                                }
                                            }
                                        }
                                        else{
                                            reservedHere = false;
                                            reservedDiff = false;
                                        }

                                        refTransit.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                //String status = dataSnapshot.child("status").getValue().toString();

                                                if(dataSnapshot.child("status").exists()){
                                                    inTransit = true;
                                                }
                                                else{
                                                    inTransit = false;
                                                }
                                                if(dataSnapshot.child("isFinished").exists()){
                                                    isFinished = true;
                                                }
                                                else{
                                                    isFinished = false;
                                                }
                                                updateReserveButton();
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

    void UpdateResCap(){
        String resCap = resCount + "/" + capCount;
        txtReserved.setText(resCap);
    }

    void updateReserveButton(){
        if(inTransit){
            btnReserve.setText("Shuttle in Transit");
            btnReserve.setEnabled(false);
        }
        else if(isFinished){
            btnReserve.setText("Transit is done");
            btnReserve.setEnabled(false);
        }
        else if(currentHour > hour){
            btnReserve.setText("Transit is done");
            btnReserve.setEnabled(false);
            clearReservations();
        }
        else if(reservedDiff){
            btnReserve.setText("Reserved in another");
            btnReserve.setEnabled(false);
        }
        else if(reservedHere){
            btnReserve.setText("Cancel Reservation");
            btnReserve.setEnabled(true);
        }
        else {
            if(resCount >= capCount){
                btnReserve.setEnabled(false);
                btnReserve.setText("Full");
            }
            else{
                btnReserve.setEnabled(true);
                btnReserve.setText("Make Reservation");
            }
        }

    }

    void clearReservations(){
        refTransit.child("reservations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String time =  new DecimalFormat("0000").format(hour) ;
                    String min = time.substring(time.length()-2);
                    String hour = time.substring(0, time.length()-2);
                    String format = hour + ":" + min;
                    SimpleDateFormat f24hours = new SimpleDateFormat(
                            "HH:mm"
                    );
                    final SimpleDateFormat f12hours = new SimpleDateFormat(
                            "hh:mm aa"
                    );



                    for(DataSnapshot reserved : dataSnapshot.getChildren()){
                        String studentId = reserved.getKey();
                        reserved.getRef().removeValue();
                        DatabaseReference refStudent = refRoot.child("Students").child(studentId);
                        refStudent.child("reservations").child(transitId).removeValue();

                        final Date date;
                        try {
                            date = f24hours.parse(format);
                            final String time12hr = f12hours.format(date);
                            String message = "Your reservation for "+time12hr+" has been cancelled. The shuttle did not depart according to the schedule.";
                            refRoot.child("Notifications").child(studentId).setValue(message);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}