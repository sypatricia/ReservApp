package com.example.reservationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountRegisterActivity extends AppCompatActivity {

    Button btnRegister;
    ImageView btnBack;
    EditText txtStudentId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;
    List<EditText> textBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_register);

        //link ui elements
        txtStudentId = findViewById(R.id.txtStudentId);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        btnRegister = findViewById(R.id.btnRegister);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);
        btnBack = findViewById(R.id.btnBack);

        textBoxes = new ArrayList<>();
        textBoxes.add(txtStudentId);
        textBoxes.add(txtFirstName);
        textBoxes.add(txtLastName);
        textBoxes.add(txtPassword);
        textBoxes.add(txtConfirmPass);

        txtStudentId.setTransformationMethod(new NumericKeyBoardTransformationMethod());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get values from ui elements
                final String studentId = txtStudentId.getText().toString();
                final String firstName = txtFirstName.getText().toString();
                final String lastName = txtLastName.getText().toString();
                final String password = txtPassword.getText().toString();
                final String confirmPass = txtConfirmPass.getText().toString();
                clearErrors();


                if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPass.isEmpty()){
                    checkIfEmpty();
                }

                else if(studentId.length()!=10){
                    txtStudentId.setError("Student Number must have 10 digits");
                    txtStudentId.requestFocus();
                }

                else if (!password.equals(confirmPass)){
                    txtPassword.setError("Passwords do not match.");
                    txtConfirmPass.setError("Passwords do not match.");
                    txtConfirmPass.requestFocus();
                }
                else{
                    final DatabaseReference students = FirebaseDatabase.getInstance().getReference("Students");
                    students.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(studentId)) {
                                txtStudentId.setError("Student Id already exists");
                                txtStudentId.requestFocus();
                            }
                            else {
                                DatabaseReference student = students.child(studentId);
                                student.child("firstName").setValue(firstName);
                                student.child("lastName").setValue(lastName);
                                student.child("password").setValue(AESEncryption.encrypt(password));

                                showToast("Account registered successfully.");
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            showToast("Failed to read database.");
                        }
                    });
                }
            }
        });
        //startActivity(new Intent(AccountRegisterActivity.this, AccountLoginActivity.class));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void showToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    void checkIfEmpty() {
        for (EditText txtBox : textBoxes) {
            if(txtBox.getText().toString().isEmpty()) {
                txtBox.setError("This field is required");
                txtBox.requestFocus();
            }
        }

    }

    void clearErrors(){
        for (EditText txtBox : textBoxes) {
            txtBox.setError(null);
        }
    }

    private class NumericKeyBoardTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source;
        }
    }
}
