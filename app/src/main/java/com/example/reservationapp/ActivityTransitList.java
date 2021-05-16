package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ActivityTransitList extends AppCompatActivity {

    TextView txtTime;
    ListView lstTransits;
    Spinner spnStation;
    ImageView btnBack;

    String studentId, schedId;
    int count = 0, counter = 0;

    DatabaseReference refRoot, refTransits, refSchedule, refDesinations, refDriver, refConfirmations;
    ValueEventListener transitListener = null;
    ValueEventListener confirmationsListener = null;

    FirebaseListOptions<ScheduleModel> options;

    ArrayList<ModelTransit> arrTansits = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
        populateList();
        startConfirmationListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeTransitListener();
        stopConfirmationListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeTransitListener();
        stopConfirmationListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeTransitListener();
        stopConfirmationListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_list);

        txtTime = findViewById(R.id.txtTime);
        lstTransits = findViewById(R.id.lstTransits);
        btnBack =  findViewById(R.id.btnBack);

        schedId = getIntent().getStringExtra("schedId");
        studentId = getIntent().getStringExtra("studentId");

        refRoot = FirebaseDatabase.getInstance().getReference();
        refDriver = refRoot.child("Drivers/");
        refTransits = refRoot.child("Transits");
        refSchedule = refRoot.child("Schedules/" + schedId);
        refDesinations = refRoot.child("Stations");
        refConfirmations = refRoot.child("Confirmations/" + studentId);

        populateList();
        startConfirmationListener();

        refSchedule.addListenerForSingleValueEvent(new ValueEventListener() {
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

        lstTransits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ActivityTransitList.this, ActivityTransitInfo.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("transitId", arrTansits.get(i).getId());
                intent.putExtra("driverId", arrTansits.get(i).getDriver());
                intent.putExtra("schedId", arrTansits.get(i).getSched());
                intent.putExtra("fromId", arrTansits.get(i).getFrom());
                intent.putExtra("toId", arrTansits.get(i).getTo());
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void stopConfirmationListener(){
        if(confirmationsListener != null){
            refConfirmations.removeEventListener(confirmationsListener);
            confirmationsListener = null;
        }
    }

    void startConfirmationListener(){
        stopConfirmationListener();
        confirmationsListener = refConfirmations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").exists()){
                    if(dataSnapshot.child("status").getValue().toString().equals("Waiting")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTransitList.this);
                        builder.setTitle("Arrival Confirmation")
                                .setMessage("The driver is attempting to finish this transit. Do you confirm the shuttle has arrived at its destination?")
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        refConfirmations.child("status").setValue("Denied");
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        refConfirmations.child("status").setValue("Confirmed");
                                    }
                                }).setCancelable(false);
                        AlertDialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void removeTransitListener(){
        if(transitListener != null){
            refSchedule.child("transits").removeEventListener(transitListener);
            transitListener = null;
        }
    }

    void populateList(){
        removeTransitListener();
        transitListener = refSchedule.child("transits").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot transitSnapshot) {
                if(transitSnapshot.exists()){
                    count = (int)transitSnapshot.getChildrenCount();
                    counter = 0;
                    arrTansits = new ArrayList<>();

                    final ArrayList<ModelTransitListItem> listitems = new ArrayList<>();

                    for(DataSnapshot transit : transitSnapshot.getChildren()){

                        final ModelTransit modelTransit = new ModelTransit();
                        modelTransit.setId(transit.getKey());

                        refTransits.child(modelTransit.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    modelTransit.setDriver(dataSnapshot.child("driver").getValue().toString());
                                    modelTransit.setFrom(dataSnapshot.child("from").getValue().toString());
                                    modelTransit.setTo(dataSnapshot.child("to").getValue().toString());
                                    modelTransit.setSched(dataSnapshot.child("sched").getValue().toString());
                                    arrTansits.add(modelTransit);

                                    refDriver.child(modelTransit.getDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                final String name = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();

                                                refDesinations.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        if(dataSnapshot.exists()){

                                                            String from =  dataSnapshot.child(modelTransit.getFrom()).child("name").getValue().toString();
                                                            String to =  dataSnapshot.child(modelTransit.getTo()).child("name").getValue().toString();

                                                            listitems.add(new ModelTransitListItem(from, to, name));

                                                            counter++;
                                                            if(count == counter) {
                                                                AdapterTransitList adapterTransitList = new AdapterTransitList(ActivityTransitList.this, R.layout.list_item_transit, listitems);
                                                                lstTransits.setAdapter(adapterTransitList);
                                                            }

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
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
                else{
                    lstTransits.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}