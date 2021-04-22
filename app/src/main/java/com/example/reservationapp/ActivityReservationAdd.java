package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityReservationAdd extends AppCompatActivity {

    String studentId;
    boolean reserved = false, notAvailable = false, sameStations = true;
    int schedPos = 0, fromPos = 0, destinationPos = 0, time = 0;

    Button btnAdd, btnCancel;
    Spinner spnSchedule, spnFrom, spnDestination;

    DatabaseReference refRoot, refSchedules, refStations, refTransits, refStudent;

    FirebaseListOptions<ModelDestination> optionsStation;
    ModelDestination[] stationArr;
    FirebaseListOptions<ScheduleModel> optionsSchedule;
    ScheduleModel[] scheduleArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_add);

        studentId = getIntent().getStringExtra("studentId");

        btnAdd = findViewById(R.id.btnAdd);
        spnSchedule = findViewById(R.id.spnSchedule);
        spnFrom = findViewById(R.id.spnFrom);
        spnDestination = findViewById(R.id.spnDestination);

        refRoot = FirebaseDatabase.getInstance().getReference();
        refSchedules = refRoot.child("Schedules");
        refStations = refRoot.child("Stations");
        refStudent = refRoot.child("Students/" + studentId);
        refTransits = refRoot.child("Transits");

        refSchedules.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = String.valueOf(dataSnapshot.getChildrenCount());
                scheduleArr = new ScheduleModel[Integer.valueOf(count)];

                optionsSchedule = new FirebaseListOptions.Builder<ScheduleModel>().setQuery(refSchedules.orderByChild("hour"), ScheduleModel.class).setLayout(R.layout.spinner_item_schedule).build();

                FirebaseListAdapter<ScheduleModel> firebaseListAdapter = new FirebaseListAdapter<ScheduleModel>(optionsSchedule) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull ScheduleModel model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtTime = v.findViewById(R.id.txtTime);

                        String time = model.getHour() + ":" + model.getMinute();
                        SimpleDateFormat f24hours = new SimpleDateFormat(
                                "HH:mm"
                        );

                        final Date date;
                        try {
                            date = f24hours.parse(time);

                            final SimpleDateFormat f12hours = new SimpleDateFormat(
                                    "hh:mm aa"
                            );

                            final String time12hr = f12hours.format(date);

                            txtTime.setText(time12hr);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        scheduleArr[position] = new ScheduleModel();
                        scheduleArr[position].setId(itemRef.getKey());
                        scheduleArr[position].setHour(model.getHour());
                        scheduleArr[position].setMinute(model.getMinute());

                    }
                };

                firebaseListAdapter.startListening();
                spnSchedule.setAdapter(firebaseListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        refStations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = String.valueOf(dataSnapshot.getChildrenCount());
                stationArr = new ModelDestination[Integer.valueOf(count)];

                optionsStation = new FirebaseListOptions.Builder<ModelDestination>().setQuery(refStations, ModelDestination.class).setLayout(R.layout.spinner_item_destination).build();

                FirebaseListAdapter<ModelDestination> firebaseListAdapter = new FirebaseListAdapter<ModelDestination>(optionsStation) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull ModelDestination model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtName = v.findViewById(R.id.txtDestinationName);

                        txtName.setText(model.getName());
                        stationArr[position] = new ModelDestination();
                        stationArr[position].setId(itemRef.getKey());
                        stationArr[position].setName(model.getName());
                        stationArr[position].setLatitude(model.getLatitude());
                        stationArr[position].setLongitude(model.getLongitude());
                        stationArr[position].setAddress(model.getAddress());

                    }
                };

                firebaseListAdapter.startListening();
                spnFrom.setAdapter(firebaseListAdapter);
                spnDestination.setAdapter(firebaseListAdapter);
                //spnTo.setSelection(1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        spnSchedule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                schedPos = position;

                refStudent.child("reservations").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        time = Integer.parseInt(new DecimalFormat("00").format(scheduleArr[schedPos].getHour()) + new DecimalFormat("00").format(scheduleArr[schedPos].getMinute()));
                        reserved = false;

                        ShowToast(time + "");

                        for(DataSnapshot reservation : dataSnapshot.getChildren()){
                            if(reservation.getValue().toString().equals(time + ""))
                                reserved = true;
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spnFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromPos = position;
                CheckSchedule();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spnDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                destinationPos = position;
                CheckSchedule();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(reserved){
                    ShowToast("You already have a reservation for that time");
                }
                else if(sameStations){
                    ShowToast("You cannot set the same stations");
                }
                else {
                    String id = scheduleArr[schedPos].getId();
                    final String from = stationArr[fromPos].getId();
                    final String des = stationArr[destinationPos].getId();

                    refSchedules.child(id).child("transits").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                final int count = (int)dataSnapshot.getChildrenCount();
                                ShowToast(String.valueOf(count));
                                final DataSnapshot transit = ds;

                                refTransits.child(transit.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String driver = dataSnapshot.child("driver").getValue().toString();

                                        refRoot.child("Drivers").child(driver).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot driver) {
                                                int cap = Math.toIntExact((long)driver.child("capacity").getValue());
                                                int res = Math.toIntExact(count);

                                                if(transit.child(from).getValue().toString().equals("from") && transit.child(des).getValue().toString().equals("destination") && res < cap){

                                                    String transitId = transit.getKey();
                                                    refStudent.child("reservations").child(transitId).setValue(time);
                                                    refTransits.child(transitId).child("reservations").child(studentId).setValue(time);
                                                    ShowToast("You were successfully reserved to a shuttle");
                                                    finish();
                                                    return;

                                                }

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
                            ShowToast("Could not find an available shuttle :(");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        });

    }

    void CheckSchedule(){
        sameStations = false;
        if(fromPos == destinationPos){
            sameStations = true;
        }
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

}