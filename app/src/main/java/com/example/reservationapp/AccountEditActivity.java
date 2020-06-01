package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountEditActivity extends AppCompatActivity {

    Button btnSave, btnCancel;
    EditText txtStudentId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;

    String studentId;

    DatabaseReference student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        txtStudentId = findViewById(R.id.txtStudentId);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);

        studentId = getIntent().getStringExtra("studentId");
        txtStudentId.setText(studentId);

        student = FirebaseDatabase.getInstance().getReference("Students/" + studentId);
        student.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtFirstName.setText(dataSnapshot.child("firstName").getValue().toString());
                txtLastName.setText(dataSnapshot.child("lastName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
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

                    showToast("Registration Successful");
                    finish();
                }
            }
        });

    }


    void showToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
