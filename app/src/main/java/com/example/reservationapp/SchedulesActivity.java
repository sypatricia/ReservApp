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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SchedulesActivity extends AppCompatActivity {

    ListView lstSchedules;

    ArrayList<ScheduleModel> schedules = new ArrayList<>();

    DatabaseReference refSchedules;

    FirebaseListOptions<ScheduleModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        lstSchedules = findViewById(R.id.lstSchedules);

        refSchedules = FirebaseDatabase.getInstance().getReference("Schedules");

        options = new FirebaseListOptions.Builder<ScheduleModel>().setQuery(refSchedules.orderByChild("hour"), ScheduleModel.class).setLayout(R.layout.list_item_schedule).build();

        FirebaseListAdapter<ScheduleModel> firebaseListAdapter = new FirebaseListAdapter<ScheduleModel>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull ScheduleModel model, int position) {

                DatabaseReference itemRef = getRef(position);

                TextView txtTime = v.findViewById(R.id.txtTime);

                String time = model.getHour() + ":" +model.getMinute();

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

                    schedules.add(new ScheduleModel(itemRef.getKey(), model.getHour(), model.getMinute()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        firebaseListAdapter.startListening();
        lstSchedules.setAdapter(firebaseListAdapter);

    }
}