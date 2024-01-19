package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Registration extends AppCompatActivity {
    private EditText etName, etEmail, etMobile, etRollno, etPassword;
    private Spinner etBatch;
    private FirebaseAuth auth;
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration1);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.mobileNo);
        etRollno = findViewById(R.id.rollNo);
        etBatch = findViewById(R.id.mySpinner);
        etPassword = findViewById(R.id.password);

        auth = FirebaseAuth.getInstance();
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private String name, email, mobile, rollno, batch, password;

    private void registerUser() {

        this.name = etName.getText().toString();
        this.email = etEmail.getText().toString();
        this.mobile = etMobile.getText().toString();
        this.rollno = etRollno.getText().toString();
        this.batch = etBatch.getSelectedItem().toString();
        this.password = etPassword.getText().toString();


        // Input validation
        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || rollno.isEmpty() || batch.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.length() < 3) {
            etName.setError("Name should have at least 3 characters");
            return;
        } else if (!name.matches("[a-zA-Z ]+")) {
            etName.setError("Name should only contain letters and spaces");
            return;
        }
        if (rollno.length() != 7) {
            etRollno.setError("Roll No Must Have 7 Digits");
            etRollno.requestFocus();
            return;
        } else if (mobile.length() != 10 || mobile.length() < 10 || mobile.length() > 10) {
            etMobile.setError("Mobile No Have 10 Digits Only");
            etMobile.requestFocus();
            return;
        }
        checkMobileNumberUniqueness(mobile);
    }

    private void checkMobileNumberUniqueness(String mobile) {
        studentsRef.orderByChild("mobile").equalTo(mobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etMobile.setError("Mobile number already registered");
                    etMobile.requestFocus();
                } else {
                    checkRollNumberUniqueness(rollno);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayErrorToUser("Error checking for existing users");
            }
        });
    }


    private void checkRollNumberUniqueness(String rollno) {
        studentsRef.orderByChild("rollno").equalTo(rollno).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etRollno.setError("This Roll Number already registered in DataBase");
                    etRollno.requestFocus();
                } else {
                    createUserInFirebase(email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayErrorToUser("Error checking for existing users");
            }
        });
    }


    private void createUserInFirebase(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();
                            saveStudentData(uid, name, email, mobile, rollno, batch);
                            Toast.makeText(Registration.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                            // Clear input fields
                            etName.setText("");
                            etEmail.setText("");
                            etMobile.setText("");
                            etRollno.setText("");
                            etPassword.setText("");
                            // Navigate to another activity
                            startActivity(new Intent(Registration.this, LoginActivity.class));
                            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                            finish();

                        } else {
                            String errorMsg = task.getException().getMessage().toString();
                            if(errorMsg.contains("password")){
                                etPassword.setError(errorMsg);
                                etPassword.requestFocus();
                            } else if (errorMsg.contains("email")) {
                                etEmail.setError(errorMsg);
                                etEmail.requestFocus();
                            }else {
                                Toast.makeText(Registration.this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    private void saveStudentData(String uid, String name, String email, String mobile, String
            rollno, String batch) {
        try {
            Student student = new Student(name, email, mobile, rollno, batch);
            studentsRef.child(uid).setValue(student);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG);
        }
    }

    private void displayErrorToUser(String errorCheckingForExistingUsers) {
        Toast.makeText(this, errorCheckingForExistingUsers, Toast.LENGTH_LONG);
    }


    public void onLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}