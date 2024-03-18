package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class View_Attendance extends AppCompatActivity {
    private Spinner subjectSpinner;
    private FirebaseAuth auth;
    private String course, batchName, name;
    private RecyclerView recycler_view;
    private ViewAttendanceAdapter viewAttendanceAdapter;
    private Map<String, String> subjects;
    private DatabaseReference attendanceRef, batchesRef;
    private List<Attendance> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        name = getIntent().getStringExtra("name");
        course = getIntent().getStringExtra("course");
        batchName = getIntent().getStringExtra("batchName");
        recycler_view = findViewById(R.id.recycler_view);

        auth = FirebaseAuth.getInstance();
        populateSubjects();

    }

    public void populateSubjects() {
        batchesRef = FirebaseDatabase.getInstance().getReference("batches");
        batchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot batchSnapshot : snapshot.getChildren()) {
                    String name = batchSnapshot.child("name").getValue(String.class);
                    if (name.equals(batchName)) {
                        subjects = (Map<String, String>) batchSnapshot.child("subjects").getValue();
                        if (subjects != null) {
                            List<String> subjectNames = new ArrayList<>(subjects.values());
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(View_Attendance.this, android.R.layout.simple_spinner_item, subjectNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            subjectSpinner.setAdapter(adapter);
                            // Set a listener for the spinner to load attendance data when a subject is selected
                            subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    loadAttendance(subjectSpinner.getSelectedItem().toString());
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    // Handle case where nothing is selected
                                }
                            });
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Attendance.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void loadAttendance(String selectedSubject) {
        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");
        attendanceRef.child(course).child(batchName).child(selectedSubject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList = new ArrayList<>();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    for (DataSnapshot studentSnapshot : dateSnapshot.getChildren()) {
                        String studentName = studentSnapshot.getKey();
                        if (name.equals(studentName)) {
                            String roll = studentSnapshot.child("roll").getValue(String.class);
                            String status = studentSnapshot.child("status").getValue(String.class);
                            String code = studentSnapshot.child("code").getValue(String.class);
                            String flag = studentSnapshot.child("flag").getValue(String.class);
                            String dateNew = studentSnapshot.child("attendanceTime").getValue(String.class);
                            if(flag!=null && flag.equals("Completed")) {
                                attendanceList.add(new Attendance(roll, studentName, status, batchName, flag, selectedSubject, date, code));
                            }
                        }
                    }
                }
                // Here you can display the attendanceList as needed
                setRecyclerView(attendanceList);
                System.out.println(selectedSubject);
                System.out.println();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Attendance.this, "Failed to load attendance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRecyclerView(List<Attendance> attendanceList) {
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        viewAttendanceAdapter = new ViewAttendanceAdapter(this, attendanceList, this); // Assuming StudentsAdapter is implemented
        recycler_view.setAdapter(viewAttendanceAdapter);
    }


    public void dashboard(View view) {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
