package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Dashboard extends AppCompatActivity {
    private TextView userName,greetingMsg;
    private String batchName,name,rollno,course,userId;
    private ListView list_subjects,attendaceStatusList;
    Map<String, String> subjects;
    private DatabaseReference batchRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle each item click event
                Integer id =item.getItemId();
                if(id==R.id.navigation_profile) {
                    // Open DashboardActivity
                    Intent intent = new Intent(Dashboard.this,Profile.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("course", course);
                    startActivity(intent);
                    return true;
                }else {
                    // Open ProfileActivity
                    return true;
                }
            }
        });

        userName = findViewById(R.id.userName);
        list_subjects = findViewById(R.id.list_subjects);
        attendaceStatusList=findViewById(R.id.attendaceStatusList);
        greetingMsg=findViewById(R.id.greetingMsg);
        setGreetingMessage();
        // Check for current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            // Fetch user data from Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("students");
            userRef.child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                name = snapshot.child("name").getValue(String.class);
                                rollno = snapshot.child("rollno").getValue(String.class);
                                course=snapshot.child("course").getValue(String.class);
                                batchName = snapshot.child("batch").getValue(String.class);
                                userName.setText(name.toUpperCase());
                                getSubjects();
                            } else {
                                Toast.makeText(Dashboard.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Dashboard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {

        }


    }

    private void getSubjects() {
        DatabaseReference batchRef = FirebaseDatabase.getInstance().getReference("batches");
        batchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot batchSnapshot : snapshot.getChildren()) {
                    String batch = batchSnapshot.child("name").getValue(String.class);
                    if (batch.equals(batchName)) {
                        subjects = (Map<String, String>) batchSnapshot.child("subjects").getValue();
                    }
                    List<String> subjectNames = new ArrayList<>();
                    if (subjects != null) {
                        subjectNames.addAll(subjects.values());
                    }
                    AttendanceUiStatusAdapter adapter = new AttendanceUiStatusAdapter(getBaseContext(), subjectNames);
                    list_subjects.setAdapter(adapter);
                    loadAttendanceStatusForToday(subjectNames,name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void loadAttendanceStatusForToday(List<String> subjectNames, String studentId) {
        String todayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");

        attendanceRef.child(course).child(batchName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> attendanceStatuses = new ArrayList<>();
                for (String subject : subjectNames) {
                    DataSnapshot subjectSnapshot = snapshot.child(subject).child(todayDate).child(studentId);
                    String attendanceStatus = "Incomplete";
                    if (subjectSnapshot.exists() ) {
                        if(subjectSnapshot.child("flag").exists() && subjectSnapshot.child("flag").getValue(String.class).equals("Completed")){
                            attendanceStatus = subjectSnapshot.child("status").getValue(String.class);
                        }
                        // Attendance data found for this subject

                    }
                    attendanceStatuses.add(attendanceStatus);
                }

                // Update UI with the list of attendance statuses
                AttendanceUiStatusAdapter adapter = new AttendanceUiStatusAdapter(getBaseContext(), attendanceStatuses);
                attendaceStatusList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, "Failed to load attendance data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void attendance(View view){
        Intent intent = new Intent(this, AddAttendanceActivity.class);
        intent.putExtra("batchName",batchName);
        intent.putExtra("name",name);
        intent.putExtra("rollno",rollno);
        intent.putExtra("course",course);
        startActivity(intent);
        finish();
    }

    public void view_attenda(View view){
        Intent intent = new Intent(this, View_Attendance.class);
        intent.putExtra("batchName",batchName);
        intent.putExtra("name",name);
        intent.putExtra("rollno",rollno);
        intent.putExtra("course",course);
        startActivity(intent);
        finish();
    }

    private void setGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        greetingMsg.setText(greeting);
    }
    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}