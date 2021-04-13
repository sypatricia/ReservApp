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

    ListView lstReservations;
    Button btnAddReservation;

    String studentId;

    ArrayList<ModelReservation> reservations = new ArrayList<>();

    DatabaseReference refRoot, refStudent;

    FirebaseListOptions<Integer> options;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reservations, container, false);

        lstReservations = rootView.findViewById(R.id.lstReservations);
        btnAddReservation = rootView.findViewById(R.id.btnAddReservation);

        studentId = getActivity().getIntent().getStringExtra("studentId");

        //refSchedules = FirebaseDatabase.getInstance().getReference("Schedules");
        refRoot = FirebaseDatabase.getInstance().getReference();
        refStudent = refRoot.child("Students/" + studentId);

        options = new FirebaseListOptions.Builder<Integer>().setQuery(refStudent.child("reservations").orderByValue(), Integer.class).setLayout(R.layout.list_item_reservation).build();

        FirebaseListAdapter<Integer> firebaseListAdapter = new FirebaseListAdapter<Integer>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull final Integer hour, int position) {

                DatabaseReference itemRef = getRef(position);
                final String transitId = itemRef.getKey();

                final TextView txtDriver = v.findViewById(R.id.txtDriver);
                final TextView txtTime = v.findViewById(R.id.txtTime);
                final TextView txtFrom = v.findViewById(R.id.txtFrom);
                final TextView txtDestination = v.findViewById(R.id.txtDestination);

                DatabaseReference refTransit = FirebaseDatabase.getInstance().getReference("Transits/" + transitId);

                refTransit.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String driverId = "", schedId = "", fromId = "", destinationId = "";

                        driverId = dataSnapshot.child("driver").getValue().toString();
                        schedId = dataSnapshot.child("sched").getValue().toString();
                        fromId = dataSnapshot.child("from").getValue().toString();
                        destinationId = dataSnapshot.child("to").getValue().toString();

                        DatabaseReference refDriver = refRoot.child("Drivers/" + driverId);
                        DatabaseReference refSchedule = refRoot.child("Schedules/" + schedId);
                        DatabaseReference refFrom = refRoot.child("Stations/" + fromId);
                        DatabaseReference refDestination = refRoot.child("Stations/" + destinationId);

                        refDriver.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String drivername = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();
                                txtDriver.setText(drivername);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        refSchedule.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String time = dataSnapshot.child("hour").getValue() + ":" + dataSnapshot.child("minute").getValue();
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

                        refFrom.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String from = dataSnapshot.child("name").getValue().toString();
                                txtFrom.setText(from);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        refDestination.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String destination = dataSnapshot.child("name").getValue().toString();
                                txtDestination.setText(destination);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        reservations.add(new ModelReservation(transitId, driverId, schedId, fromId,destinationId));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        firebaseListAdapter.startListening();
        lstReservations.setAdapter(firebaseListAdapter);

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

        btnAddReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityReservationAdd.class);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
            }
        });

        return rootView;
    }
}