package com.example.reservationapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProfile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btnSave, btnCancel;
    EditText txtStudentId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;
    String defFirstName, defLastName;

    String studentId;

    DatabaseReference student;

    public FragmentProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentProfile newInstance(String param1, String param2) {
        FragmentProfile fragment = new FragmentProfile();
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
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        txtStudentId = rootView.findViewById(R.id.txtStudentId2);
        txtFirstName = rootView.findViewById(R.id.txtFirstName2);
        txtLastName = rootView.findViewById(R.id.txtLastName2);
        btnCancel = rootView.findViewById(R.id.btnCancel2);
        btnSave = rootView.findViewById(R.id.btnSave2);
        txtPassword = rootView.findViewById(R.id.txtPassword2);
        txtConfirmPass = rootView.findViewById(R.id.txtConfirmPass2);

        studentId = getActivity().getIntent().getStringExtra("studentId");
        txtStudentId.setText(studentId);

        student = FirebaseDatabase.getInstance().getReference("Students/" + studentId);
        student.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                defFirstName = dataSnapshot.child("firstName").getValue().toString();
                defLastName = dataSnapshot.child("lastName").getValue().toString();
                txtFirstName.setText(defFirstName);
                txtLastName.setText(defLastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                txtFirstName.setText(defFirstName);
                txtLastName.setText(defLastName);
                clearPW();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String firstName = txtFirstName.getText().toString();
                final String lastName = txtLastName.getText().toString();
                final String password = txtPassword.getText().toString();
                final String confirmPass = txtConfirmPass.getText().toString();

                if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPass.isEmpty()){
                    showToast("All fields are required");
                }
                else if (!password.equals(confirmPass)){
                    showToast("Passwords do not match");
                }
                else{
                    final DatabaseReference student = FirebaseDatabase.getInstance().getReference("Students/" + studentId);
                    student.child("firstName").setValue(firstName);
                    student.child("lastName").setValue(lastName);
                    student.child("password").setValue(password);

                    showToast("Account Updated Successfully");
                    clearPW();
                }
            }
        });

        return rootView;
    }

    void clearPW(){
        txtPassword.setText("");
        txtConfirmPass.setText("");
    }

    void showToast(String message){ Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); }
}