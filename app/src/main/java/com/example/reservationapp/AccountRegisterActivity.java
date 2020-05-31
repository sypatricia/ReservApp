package com.example.reservationapp;

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

public class AccountRegisterActivity extends AppCompatActivity {

    Button btnRegister, btnCancel;
    EditText txtStudentId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_register);

        txtStudentId = findViewById(R.id.txtStudentId);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        btnCancel = findViewById(R.id.btnCancel);
        btnRegister = findViewById(R.id.btnRegister);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String studentId = txtStudentId.getText().toString();
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
                    final DatabaseReference students = FirebaseDatabase.getInstance().getReference("Students");
                    students.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(studentId)) {
                                showToast("That student already exists");
                            }
                            else {
                                DatabaseReference driver = students.child(studentId);
                                driver.child("firstName").setValue(firstName);
                                driver.child("lastName").setValue(lastName);
                                driver.child("password").setValue(password);

                                showToast("Registration Successful");
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            showToast("Failed to read database");
                        }
                    });
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    void showToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
