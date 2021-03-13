package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.Date;

public class ActivityReservationAdd extends AppCompatActivity {

    String studentId;

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
        btnCancel = findViewById(R.id.btnCancel);
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

        //btnadd

        //btncancel

    }
}