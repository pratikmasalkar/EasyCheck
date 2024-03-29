package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class Registration extends AppCompatActivity {
    private EditText etName, etEmail, etMobile, etRollno, etPassword;
    private Spinner courseSpinner,batchSpinner;
    private FirebaseAuth auth;
    private DatabaseReference studentsRef, courseRef,batchRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration1);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.mobileNo);
        etRollno = findViewById(R.id.rollNo);
        courseSpinner = findViewById(R.id.courseSpinner);
        batchSpinner=findViewById(R.id.batchSpinner);
        etPassword = findViewById(R.id.password);

        auth = FirebaseAuth.getInstance();
        studentsRef = FirebaseDatabase.getInstance().getReference("students");
        courseRef = FirebaseDatabase.getInstance().getReference("courses");
        batchRef = FirebaseDatabase.getInstance().getReference("batches");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> courseNames = new ArrayList<>();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseName = courseSnapshot.child("name").getValue(String.class);
                    courseNames.add(courseName);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Registration.this, android.R.layout.simple_spinner_item, courseNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                courseSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Registration.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Populate teacher spinners (implementation
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = parent.getItemAtPosition(position).toString();
                populateBatchSpinners(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void populateBatchSpinners(String courseName) {
        batchRef.child("").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> batches = new ArrayList<>();
                for (DataSnapshot batchSnapshot : snapshot.getChildren()) {
                    String batchCourse = batchSnapshot.child("course").getValue(String.class);
                    if (batchCourse != null && batchCourse.equals(courseName)) {
                        String batchName = batchSnapshot.child("name").getValue(String.class);
                        batches.add(batchName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Registration.this, android.R.layout.simple_spinner_item, batches);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                batchSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String name, email, mobile, rollno, course,batch, password;

    private void registerUser() {

        this.name = etName.getText().toString();
        this.email = etEmail.getText().toString();
        this.mobile = etMobile.getText().toString();
        this.rollno = etRollno.getText().toString();
        this.course = courseSpinner.getSelectedItem().toString();
        this.batch=batchSpinner.getSelectedItem().toString();
        this.password = etPassword.getText().toString();


        // Input validation
        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || rollno.isEmpty() || course.isEmpty() || password.isEmpty()) {
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
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();
                    saveStudentData(uid, name, email, mobile, rollno, course,batch);
                    Toast.makeText(Registration.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Clear input fields
                    etName.setText("");
                    etEmail.setText("");
                    etMobile.setText("");
                    etRollno.setText("");
                    etPassword.setText("");
                    // Navigate to another activity
                    // startActivity(new Intent(Registration.this, LoginActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                    finish();

                } else {
                    String errorMsg = task.getException().getMessage().toString();
                    if (errorMsg.contains("password")) {
                        etPassword.setError(errorMsg);
                        etPassword.requestFocus();
                    } else if (errorMsg.contains("email")) {
                        etEmail.setError(errorMsg);
                        etEmail.requestFocus();
                    } else {
                        Toast.makeText(Registration.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void saveStudentData(String uid, String name, String email, String mobile, String rollno, String course,String batch) {
        try {
            Student student = new Student(name, email, mobile, rollno,course, batch);
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