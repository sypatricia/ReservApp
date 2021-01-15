package com.example.reservationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountLoginActivity extends AppCompatActivity {

    //variables for ui elements
    Button btnLogin;
    EditText txtStudentId, txtPassword;
    TextView lblRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);

        //linking ui elements to variables
        btnLogin = findViewById(R.id.btnLogin);
        txtStudentId = findViewById(R.id.txtStudentId);
        txtPassword = findViewById(R.id.txtPassword);
        lblRegister = findViewById(R.id.lblRegister);

        lblRegister.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(AccountLoginActivity.this, AccountRegisterActivity.class);
                startActivity(intent);
                return false;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get current values in text field
                final String studentId = txtStudentId.getText().toString();
                final String pass = txtPassword.getText().toString();

                if (studentId.isEmpty() || pass.isEmpty()){
                    ShowToast("Please enter your Student Number and Password.");
                }
                else{
                    //gets database reference of students
                    DatabaseReference refStudents = FirebaseDatabase.getInstance().getReference("Students");
                    refStudents.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //check if students has someone with that stud id
                            if (!dataSnapshot.hasChild(studentId))
                                ShowToast("The account does not exist.");
                            //check if passwords match
                            else if (!dataSnapshot.child(studentId).child("password").getValue().toString().equals(pass))
                                ShowToast("Incorrect credentials entered.");
                            else {
                                ShowToast("Login successful.");

                                Intent intent = new Intent(AccountLoginActivity.this, MainActivity.class);
                                //pass student id in next activity
                                intent.putExtra("studentId", studentId);
                                startActivityForResult(intent,1);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            ShowToast("Failed to connect to server");
                        }
                    });
                }
            }
        });

    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
