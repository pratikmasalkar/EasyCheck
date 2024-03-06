package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private DatabaseReference batchRef, attendanceRef;
    private Map<String, String> teachers;
    private List<String> teacherNames, subjectNames;
    private String batchName, selectedSubject,attendanceDateTime;
    private TextView currentDateTv;
    private Button submitButton;
    private String name, rollno, course, currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attendance);
        batchName = getIntent().getStringExtra("batchName");
        name = getIntent().getStringExtra("name");
        rollno = getIntent().getStringExtra("rollno");
        course = getIntent().getStringExtra("course");
        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
         attendanceDateTime= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        currentDateTv=findViewById(R.id.currentDateTv);
        currentDateTv.setText(attendanceDateTime);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        teacherSpinner = findViewById(R.id.teacherSpinner);
        submitButton=findViewById(R.id.submitButton);
        attendanceCodeEt = findViewById(R.id.attendanceCodeEt);

        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");
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
                checkAttendanceStatus(selectedSubject);
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
        attendanceRef.child(course).child(batchName).child(selectedSubject).child(currentDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Date node doesn't exist, student can add attendance
                            addAttendance();
                        } else {
                            // Date node exists, check if student already marked attendance
                            if (!snapshot.child(name).exists()) {
                                // Student hasn't marked attendance yet, allow them to add
                                addAttendance();
                            } else {
                                // Student record exists, check flag
                                String completeStatus = (String) snapshot.child(name).child("flag").getValue();
                                String attendanceStatus = (String) snapshot.child(name).child("status").getValue();
                                if(attendanceStatus.isEmpty()==false && completeStatus==null){
                                    attendanceCodeEt.setText("");
                                    Toast.makeText(AddAttendanceActivity.this, "You Already Marked Attendance", Toast.LENGTH_SHORT).show();
                                }
                                else if (completeStatus != null && completeStatus.equals("Completed")) {
                                    // Attendance is marked and locked, notify student
                                    attendanceCodeEt.setText("");
                                    Toast.makeText(AddAttendanceActivity.this, "Attendance Completed and locked by the teacher", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Student can still add attendance (flag not "Completed")
                                    addAttendance();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors here
                        Toast.makeText(AddAttendanceActivity.this, "Failed to check attendance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addAttendance() {
        String selectedSubject = subjectSpinner.getSelectedItem().toString();
        String attendanceCode = attendanceCodeEt.getText().toString();

        if (attendanceCode.isEmpty()) {
            attendanceCodeEt.setError("Enter Code First");
            return;
        }
        // Get current date and time
        String attendanceDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Create attendance data
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("name", name);
        attendanceData.put("roll", rollno);
        attendanceData.put("code", attendanceCode);
        attendanceData.put("status", "Present");
        attendanceData.put("attendanceTime", attendanceDateTime);


        // Push attendance data to Firebase
        attendanceRef.child(course)
                .child(batchName)
                .child(selectedSubject)
                .child(currentDate)
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

    private void checkAttendanceStatus(String subject) {
        TextView yellow = findViewById(R.id.yellow);
        TextView green = findViewById(R.id.green);
        TextView red = findViewById(R.id.red);

        attendanceRef.child(course).child(batchName).child(subject).child(currentDate)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Date node doesn't exist, attendance not yet taken
                            // No need to update UI in this case
                            red.setVisibility(View.INVISIBLE);
                            yellow.setVisibility(View.GONE);
                            green.setVisibility(View.GONE);
                        } else {
                            String completeStatus = null;

                            // Check if student record exists before accessing its child
                            if (snapshot.child(name).exists()) {
                                completeStatus = (String) snapshot.child(name).child("flag").getValue();


                                String attendanceStatus = (String) snapshot.child(name).child("status").getValue();

                                if (attendanceStatus.equals("Present") && completeStatus == null) {
                                    // Attendance marked, not verified yet
                                    red.setVisibility(View.GONE);
                                    yellow.setText("You Marked Your Attendance\nTO Verify");
                                    yellow.setTextColor(getResources().getColor(R.color.colorInvalid)); // Set yellow color
                                    yellow.setVisibility(View.VISIBLE);
                                    green.setVisibility(View.GONE);
                                } else if (attendanceStatus.equals("Present") && completeStatus.equals("Completed")) {
                                    // Attendance marked and finalized
                                    red.setVisibility(View.GONE);
                                    yellow.setVisibility(View.GONE);
                                    green.setText("Attendance Completed\nYour Status: Present");
                                    green.setTextColor(getResources().getColor(R.color.colorPresent)); // Set green color
                                    green.setVisibility(View.VISIBLE);
                                } else if (attendanceStatus.equals("Absent") && completeStatus.equals("Completed")) {
                                    // Marked absent and finalized
                                    red.setText("Attendance Completed\nYour Status: Absent");
                                    red.setTextColor(getResources().getColor(R.color.colorAbsent)); // Set red color
                                    red.setVisibility(View.VISIBLE);
                                    yellow.setVisibility(View.GONE);
                                    green.setVisibility(View.GONE);
                                } else if (attendanceStatus.equals("Invalid") && completeStatus.equals("Completed")) {
                                    // Marked invalid and finalized
                                    red.setText("Attendance Completed\nYour Status: Invalid");
                                    red.setTextColor(getResources().getColor(R.color.colorInvalid)); // Set red color
                                    red.setVisibility(View.VISIBLE);
                                    yellow.setVisibility(View.GONE);
                                    green.setVisibility(View.GONE);
                                } else {
                                    // No attendance record for the user yet
                                    red.setVisibility(View.GONE);
                                    yellow.setVisibility(View.GONE);
                                    green.setVisibility(View.GONE);
                                }
                            }
                            else {
                                red.setVisibility(View.INVISIBLE);
                                yellow.setVisibility(View.GONE);
                                green.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors here
                        Toast.makeText(AddAttendanceActivity.this, "Failed to check attendance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void dashboard(View view) {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}