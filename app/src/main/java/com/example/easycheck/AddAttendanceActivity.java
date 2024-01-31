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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddAttendanceActivity extends AppCompatActivity {
    private Spinner subjectSpinner, teacherSpinner;
    private EditText attendanceCodeEt;
    private DatabaseReference batchRef;
    private Map<String, String> teachers;
    private List<String> teacherNames, subjectNames;
    private String batchName, selectedSubject;
    private String name,rollno,course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attendance);
        batchName = getIntent().getStringExtra("batchName");
        name=getIntent().getStringExtra("name");
        rollno=getIntent().getStringExtra("rollno");
        course=getIntent().getStringExtra("course");



        subjectSpinner = findViewById(R.id.subjectSpinner);
        teacherSpinner = findViewById(R.id.teacherSpinner);
        attendanceCodeEt=findViewById(R.id.attendanceCodeEt);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        batchRef = databaseReference.child("batches");

        batchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot batchSnapshot : snapshot.getChildren()) {
                    String batch = batchSnapshot.child("name").getValue(String.class);
                    if (batch.equals(batchName)) {
                        teachers = (Map<String, String>) batchSnapshot.child("teachers").getValue();
                    }
                    setupSpinners();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddAttendanceActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSpinners() {
        subjectNames = new ArrayList<>();
        teacherNames = new ArrayList<>();
        if (teachers != null) {
            subjectNames.addAll(teachers.keySet());
        }
        if (teachers != null) {
            teacherNames.addAll(teachers.values());
        }
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_list_item_1, subjectNames);
        subjectSpinner.setAdapter(subjectAdapter);

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_list_item_1, teacherNames);
        teacherSpinner.setAdapter(teacherAdapter);

        // Add item selection listeners for both spinners
        subjectSpinner.setOnItemSelectedListener(new SubjectSelectedListener());
        teacherSpinner.setOnItemSelectedListener(new TeacherSelectedListener());
    }


    private class SubjectSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            selectedSubject = subjectSpinner.getSelectedItem().toString();
            String correspondingTeacher = teachers.get(selectedSubject);
            if (teacherNames != null) {
                int teacherIndex = teacherNames.indexOf(correspondingTeacher);
                teacherSpinner.setSelection(teacherNames.indexOf(correspondingTeacher));
            } else {
                // Handle the case where teacherNames is still null (e.g., display a loading indicator)
            }


        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // No action needed
        }
    }

    private class TeacherSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            String selectedTeacher = teacherSpinner.getSelectedItem().toString();
            int subjectIndex = -1; // Initialize to -1 in case of no match

            for (Map.Entry<String, String> entry : teachers.entrySet()) {
                if (entry.getValue().equals(selectedTeacher)) {
                    subjectIndex = subjectNames.indexOf(entry.getKey());
                    break;
                }
            }

            if (subjectIndex != -1 && subjectNames != null) {
                subjectSpinner.setSelection(subjectIndex);
            } else {
                // Handle null cases or no matching subject
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void submitAttendance(View view) {
        String selectedSubject = subjectSpinner.getSelectedItem().toString();
        String attendanceCode = attendanceCodeEt.getText().toString();

        if(attendanceCode.isEmpty()){
            attendanceCodeEt.setError("Enter Code First");
        }
        // Get current date and time
        String attendanceDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String attendanceDate=new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(new Date());

        // Create attendance data
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("name",name);
        attendanceData.put("roll",rollno);
        attendanceData.put("code", attendanceCode);
        attendanceData.put("status","Mark");
        attendanceData.put("attendanceTime", attendanceDateTime);

        // Get database reference
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");

        // Push attendance data to Firebase
        attendanceRef.child(course)
                .child(batchName)
                .child(selectedSubject)
                .child(attendanceDate)
                .child(name)
                .setValue(attendanceData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddAttendanceActivity.this, "Attendance submitted successfully", Toast.LENGTH_SHORT).show();
                        // Clear input fields or reset UI
                        attendanceCodeEt.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddAttendanceActivity.this, "Attendance submission failed", Toast.LENGTH_SHORT).show();
                        // Handle error gracefully
                    }
                });
    }

    public void dashboard(View view) {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}