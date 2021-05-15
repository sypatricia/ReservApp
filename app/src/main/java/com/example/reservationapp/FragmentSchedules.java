package com.example.reservationapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSchedules#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSchedules extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View rootView;

    ListView lstSchedules;

    String studentId;

    ArrayList<ScheduleModel> schedules = new ArrayList<>();

    DatabaseReference refSchedules;
    ValueEventListener schedulesListener = null;

    FirebaseListOptions<ScheduleModel> options;


    public FragmentSchedules() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSchedules.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSchedules newInstance(String param1, String param2) {
        FragmentSchedules fragment = new FragmentSchedules();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        populateList();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeSchedulesListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeSchedulesListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeSchedulesListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_schedules, container, false);
        lstSchedules = rootView.findViewById(R.id.lstSchedules);
        studentId = getActivity().getIntent().getStringExtra("studentId");

        refSchedules = FirebaseDatabase.getInstance().getReference("Schedules");
        populateList();

        lstSchedules.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ActivityTransitList.class);
                intent.putExtra("schedId", schedules.get(i).getId());
                intent.putExtra("studentId", studentId);
                startActivity(intent);
            }
        });
        return rootView;
    }

    void removeSchedulesListener(){
        if(schedulesListener != null){
            refSchedules.orderByChild("time").removeEventListener(schedulesListener);
            schedulesListener = null;
        }
    }

    void populateList(){
        removeSchedulesListener();
        schedulesListener = refSchedules.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot scheduleSnapshot) {

                if(scheduleSnapshot.exists()){
                    schedules = new ArrayList<>();

                    final ArrayList<ModelScheduleListItem> listitems = new ArrayList<>();

                    for(DataSnapshot schedule: scheduleSnapshot.getChildren()){
                        ScheduleModel model = new ScheduleModel();
                        model.setId(schedule.getKey());

                        if(schedule.hasChild("transits")){
                            if(schedule.child("hour").exists())
                                model.setHour(Integer.valueOf(schedule.child("hour").getValue().toString()));
                            else{
                                model.setHour(0);
                            }
                            if(schedule.child("time").exists())
                                model.setMinute(Integer.valueOf(schedule.child("minute").getValue().toString()));
                            else{
                                model.setMinute(0);
                            }
                            schedules.add(model);

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

                                time = time12hr;

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            listitems.add(new ModelScheduleListItem(time));
                            AdapterScheduleList adapterScheduleList = new AdapterScheduleList(rootView.getContext(), R.layout.list_item_schedule, listitems);
                            lstSchedules.setAdapter(adapterScheduleList);
                        }
                    }
                }
                else{
                    lstSchedules.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}