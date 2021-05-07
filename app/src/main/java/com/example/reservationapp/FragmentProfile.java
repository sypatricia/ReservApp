package com.example.reservationapp;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

    View rootView;

    Button btnSave, btnLogout;
    EditText txtStudentId, txtFirstName, txtLastName, txtPassword, txtNewPass, txtConfirmPass;
    String defFirstName, defLastName, defCurrentPass;

    boolean isFirstNameChanged, isLastNameChanged, isPasswordChanged;
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
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        txtStudentId = rootView.findViewById(R.id.txtStudentId2);
        txtFirstName = rootView.findViewById(R.id.txtFirstName2);
        txtLastName = rootView.findViewById(R.id.txtLastName2);
        btnSave = rootView.findViewById(R.id.btnSave2);
        btnLogout = rootView.findViewById(R.id.btnLogout);
        txtPassword = rootView.findViewById(R.id.txtPassword2);
        txtNewPass = rootView.findViewById(R.id.txtNewPass);
        txtConfirmPass = rootView.findViewById(R.id.txtConfirmPass2);

        // Flags
        isFirstNameChanged = false;
        isLastNameChanged = false;
        isPasswordChanged = false;

        studentId = getActivity().getIntent().getStringExtra("studentId");
        txtStudentId.setText(studentId);

        student = FirebaseDatabase.getInstance().getReference("Students/" + studentId);
        student.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                defFirstName = dataSnapshot.child("firstName").getValue().toString();
                defLastName = dataSnapshot.child("lastName").getValue().toString();
                defCurrentPass = dataSnapshot.child("password").getValue().toString();
                txtFirstName.setText(defFirstName);
                txtLastName.setText(defLastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String firstName = txtFirstName.getText().toString();
                final String lastName = txtLastName.getText().toString();
                final String password = txtPassword.getText().toString();
                final String newPass = txtNewPass.getText().toString();
                final String confirmPass = txtConfirmPass.getText().toString();

                boolean isUpdated = false;

                updateChanges(firstName,lastName,password,newPass,confirmPass);
                clearTextBoxErrors();

                if (!isFirstNameChanged && !isLastNameChanged && !isPasswordChanged) {
                    txtFirstName.setError("No Changes in the First Name and Last Name");
                    txtLastName.setError("No Changes in the First Name and Last Name");
                    txtFirstName.requestFocus();
                }

                else {
                    if (isFirstNameChanged && firstName.isEmpty()){
                        txtFirstName.setError("First Name cannot be empty.");
                        txtFirstName.requestFocus();
                    }

                    if (isLastNameChanged && lastName.isEmpty()){
                        txtLastName.setError("Last Name cannot be empty.");
                        txtLastName.requestFocus();
                    }

                    if (isPasswordChanged) {

                        if(!defCurrentPass.equals(AESEncryption.encrypt(password))){
                            txtPassword.setError("Invalid current password.");
                            txtPassword.requestFocus();
                        }

                        else if (newPass.isEmpty()) {
                            txtNewPass.setError("New Password cannot be empty.");
                            txtNewPass.requestFocus();
                        }

                        else if (!newPass.equals(confirmPass)){
                            txtNewPass.setError("Passwords do not match.");
                            txtConfirmPass.setError("Passwords do not match.");
                            txtConfirmPass.requestFocus();
                        }

                        else if (password.equals(newPass)){
                            txtPassword.setError("No password changes detected.");
                            txtNewPass.setError("No password changes detected.");
                            txtNewPass.requestFocus();
                        }
                    }

                    final DatabaseReference student = FirebaseDatabase.getInstance().getReference("Students/" + studentId);

                    if (txtFirstName.getError() == null && txtLastName.getError() == null) {
                        student.child("firstName").setValue(firstName);
                        student.child("lastName").setValue(lastName);

                        if (isFirstNameChanged || isLastNameChanged) {
                            isUpdated = true;
                        }
                    }

                    if (isPasswordChanged && txtPassword.getError() == null && txtNewPass.getError() == null && txtConfirmPass.getError() == null) {

                        if ((isLastNameChanged && txtLastName.getError() != null)
                                || (isFirstNameChanged && txtFirstName.getError() != null)
                        ) {
                            return;
                        }
                        student.child("password").setValue(AESEncryption.encrypt(newPass));
                        isUpdated = true;
                        clearPW();
                    }

                    if (isUpdated) {
                        showToast("Account updated successfully.");
                    }

                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(FragmentProfile.this.getActivity(), AccountLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return rootView;
    }

    void clearPW(){
        txtPassword.setText("");
        txtNewPass.setText("");
        txtConfirmPass.setText("");
    }

    void clearTextBoxErrors(){
        txtFirstName.setError(null);
        txtLastName.setError(null);
        txtPassword.setError(null);
        txtNewPass.setError(null);
        txtConfirmPass.setError(null);
    }

    void updateChanges(String firstName, String lastName, String password, String newPassword, String confirmPassword){
        if(!firstName.equals(defFirstName)) {
            isFirstNameChanged = true;
        }
        else{
            isFirstNameChanged = false;
        }

        if(!lastName.equals(defLastName)) {
            isLastNameChanged = true;
        }
        else {
            isLastNameChanged = false;
        }

        if(!password.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            isPasswordChanged = true;
        }
        else{
            isPasswordChanged = false;
        }
    }

    void showToast(String message){ Toast.makeText(rootView.getContext(), message, Toast.LENGTH_SHORT).show(); }
}