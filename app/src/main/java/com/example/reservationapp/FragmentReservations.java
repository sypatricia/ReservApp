package com.example.reservationapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
 * Use the {@link FragmentReservations#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentReservations extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View rootView;
    int count = 0, counter = 0;

    ListView lstReservations;
    //Button btnAddReservation;

    String studentId;

    ArrayList<ModelReservation> reservations = new ArrayList<>();

    DatabaseReference refRoot, refStudent;
    ValueEventListener reservationsListener = null;

    FirebaseListOptions<Integer> options;
    FloatingActionButton fab;

    public FragmentReservations() {
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
    public static FragmentReservations newInstance(String param1, String param2) {
        FragmentReservations fragment = new FragmentReservations();
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
        removeReservationsListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeReservationsListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeReservationsListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_reservations, container, false);

        lstReservations = rootView.findViewById(R.id.lstReservations);

//        FloatingActionButton fab = rootView.findViewById(R.id.fab);

        studentId = getActivity().getIntent().getStringExtra("studentId");

        //refSchedules = FirebaseDatabase.getInstance().getReference("Schedules");
        refRoot = FirebaseDatabase.getInstance().getReference();
        refStudent = refRoot.child("Students/" + studentId);

        populateList();

        lstReservations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ActivityTransitInfo.class);
                intent.putExtra("studentId", studentId);
                intent.putExtra("transitId", reservations.get(i).getTransitId());
                intent.putExtra("driverId", reservations.get(i).getDriverId());
                intent.putExtra("schedId", reservations.get(i).getSchedId());
                intent.putExtra("fromId", reservations.get(i).getFromId());
                intent.putExtra("toId", reservations.get(i).getDestinationId());
                startActivity(intent);
            }
        });

//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new Intent(getActivity(), ActivityReservationAdd.class);
////                intent.putExtra("studentId", studentId);
////                startActivity(intent);
//            }
//        });

        return rootView;
    }

    void removeReservationsListener(){
        if(reservationsListener != null){
            refStudent.child("reservations").orderByValue().removeEventListener(reservationsListener);
            reservationsListener = null;
        }
    }

    void populateList(){
        removeReservationsListener();
        reservationsListener = refStudent.child("reservations").orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot reservationsSnapshot) {
                if(reservationsSnapshot.exists()){
                    count = (int)reservationsSnapshot.getChildrenCount();
                    counter = 0;
                    reservations = new ArrayList<>();
                    final ArrayList<ModelReservationListItem> listitems = new ArrayList<>();

                    for(DataSnapshot reservation : reservationsSnapshot.getChildren()){
                        final String transitId = reservation.getKey();
                        DatabaseReference refTransit = refRoot.child("Transits").child(transitId);

                        final ModelReservation modelReservation = new ModelReservation();
                        modelReservation.setTransitId(transitId);

                        refTransit.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    modelReservation.setDriverId(dataSnapshot.child("driver").getValue().toString());
                                    modelReservation.setFromId(dataSnapshot.child("from").getValue().toString());
                                    modelReservation.setDestinationId(dataSnapshot.child("to").getValue().toString());
                                    modelReservation.setSchedId(dataSnapshot.child("sched").getValue().toString());
                                    reservations.add(modelReservation);

                                    final DatabaseReference refDriver = refRoot.child("Drivers").child(modelReservation.getDriverId());

                                    refDriver.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                final String driverName = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();

                                                final DatabaseReference refSchedule = refRoot.child("Schedules").child(modelReservation.getSchedId());

                                                refSchedule.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            String time = dataSnapshot.child("hour").getValue() + ":" + dataSnapshot.child("minute").getValue();
                                                            SimpleDateFormat f24hours = new SimpleDateFormat("HH:mm");

                                                            final Date date;
                                                            try {
                                                                date = f24hours.parse(time);

                                                                final SimpleDateFormat f12hours = new SimpleDateFormat("hh:mm aa");

                                                                final String time12hr = f12hours.format(date);

                                                                final String schedule = time12hr;

                                                                final DatabaseReference refStations = refRoot.child("Stations");

                                                                refStations.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        String from = "";
                                                                        String destination = "";
                                                                        if(dataSnapshot.child(modelReservation.getFromId()).exists()){
                                                                            from = dataSnapshot.child(modelReservation.getFromId()).child("name").getValue().toString();
                                                                        }
                                                                        if(dataSnapshot.child(modelReservation.getDestinationId()).exists()){
                                                                            destination = dataSnapshot.child(modelReservation.getDestinationId()).child("name").getValue().toString();
                                                                        }

                                                                        listitems.add(new ModelReservationListItem(driverName, from, destination, schedule));

                                                                        counter++;
                                                                        if(count == counter){
                                                                            AdapterReservationList adapterTransitList = new AdapterReservationList(rootView.getContext(), R.layout.list_item_reservation, listitems);
                                                                            lstReservations.setAdapter(adapterTransitList);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });

                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }
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

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
                else{
                    lstReservations.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}