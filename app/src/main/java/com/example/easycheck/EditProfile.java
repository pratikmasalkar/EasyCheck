package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    private String userId, course, batch;
    private String name, mobile, mother, guardian, relegion, caste, gender, bloodgrp, abcid, prnno, oldName;
    private EditText edit_text_name, edit_text_mobile, edit_text_mother, edit_text_guardian,
            edit_text_relegion, edit_text_caste, edit_text_blood, edit_text_abc, edit_text_prn;
    private DatabaseReference studentRef;
    private Button button_save;
    private RadioGroup radioGroupGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userId = getIntent().getStringExtra("userId");

        button_save = findViewById(R.id.button_save);

        edit_text_name = findViewById(R.id.edit_text_name);
        edit_text_mobile = findViewById(R.id.edit_text_mobile);
        edit_text_mother = findViewById(R.id.edit_text_mother);
        edit_text_guardian = findViewById(R.id.edit_text_guardian);
        edit_text_relegion = findViewById(R.id.edit_text_relegion);
        edit_text_caste = findViewById(R.id.edit_text_caste);
        edit_text_blood = findViewById(R.id.edit_text_blood);
        edit_text_abc = findViewById(R.id.edit_text_abc);
        edit_text_prn = findViewById(R.id.edit_text_prn);
        radioGroupGender = findViewById(R.id.radio_group_gender);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        studentRef = databaseReference.child("students");
        studentRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    oldName = snapshot.child("name").getValue(String.class);
                    mobile = (String) snapshot.child("mobile").getValue();
                    mother = (String) snapshot.child("mothername").getValue();
                    guardian = (String) snapshot.child("guardian").getValue();
                    relegion = (String) snapshot.child("relegion").getValue();
                    caste = (String) snapshot.child("caste").getValue();
                    bloodgrp = (String) snapshot.child("bloodgroup").getValue();
                    gender = (String) snapshot.child("gender").getValue();
                    abcid = (String) snapshot.child("abcid").getValue();
                    prnno = (String) snapshot.child("prnno").getValue();

                    course = (String) snapshot.child("course").getValue();
                    batch = (String) snapshot.child("batch").getValue();
                    setData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_name = edit_text_name.getText().toString();
//                if(oldName.equals(new_name)==false) {
//                    updateBatchesAndTeachers(userId, new_name);
//                }
                saveStudent();
            }
        });
    }


    private void setData() {
        if (oldName == null || oldName.isEmpty()) {
            edit_text_name.setText("");
        } else {
            edit_text_name.setText(oldName);
        }
        if (mobile == null || mobile.isEmpty()) {
            edit_text_mobile.setText("");
        } else {
            edit_text_mobile.setText(mobile);
        }
        if (mother == null || mother.isEmpty()) {
            edit_text_mother.setText("");
        } else {
            edit_text_mother.setText(mother);
        }
        if (guardian == null || guardian.isEmpty()) {
            edit_text_guardian.setText("");
        } else {
            edit_text_guardian.setText(guardian);
        }
        if (relegion == null || relegion.isEmpty()) {
            edit_text_relegion.setText("");
        } else {
            edit_text_relegion.setText(relegion);
        }
        if (caste == null || caste.isEmpty()) {
            edit_text_caste.setText("");
        } else {
            edit_text_caste.setText(caste);
        }
        if (bloodgrp == null || bloodgrp.isEmpty()) {
            edit_text_blood.setText("");
        } else {
            edit_text_blood.setText(bloodgrp);
        }
        if (abcid == null || abcid.isEmpty()) {
            edit_text_abc.setText("");
        } else {
            edit_text_abc.setText(abcid);
        }
        if (prnno == null || prnno.isEmpty()) {
            edit_text_prn.setText("");
        } else {
            edit_text_prn.setText(prnno);
        }

        // Set gender radio button
        if (gender != null && !gender.isEmpty()) {
            if (gender.equalsIgnoreCase("Male")) {
                radioGroupGender.check(R.id.radio_button_male);
            } else if (gender.equalsIgnoreCase("Female")) {
                radioGroupGender.check(R.id.radio_button_female);
            } else {
                // Handle other cases if needed
            }
        }
    }


    private String new_name, new_mobile, new_mother, new_guardian, new_relegion,
            new_caste, new_bloodgrp, new_gender, new_abcid, new_prnno;

    private void saveStudent() {

        this.new_name = edit_text_name.getText().toString();
        this.new_mobile = edit_text_mobile.getText().toString();
        this.new_mother = edit_text_mother.getText().toString();
        this.new_guardian = edit_text_guardian.getText().toString();
        this.new_relegion = edit_text_relegion.getText().toString();
        this.new_caste = edit_text_caste.getText().toString();
        this.new_bloodgrp = edit_text_blood.getText().toString();
        this.new_abcid = edit_text_abc.getText().toString();
        this.new_prnno = edit_text_prn.getText().toString();

        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton radioButton = findViewById(selectedId);
            new_gender = radioButton.getText().toString();
        }


        // Input validation
        if (new_name.isEmpty() || new_mobile.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (new_name.length() < 3) {
            edit_text_name.setError("Name should have at least 3 characters");
            return;
        } else if (!new_name.matches("[a-zA-Z ]+")) {
            edit_text_name.setError("Name should only contain letters and spaces");
            return;
        }
        if (new_mobile.length() != 10 || new_mobile.length() < 10 || new_mobile.length() > 10) {
            edit_text_mobile.setError("Mobile No Have 10 Digits Only");
            edit_text_mobile.requestFocus();
            return;
        }
        saveStudentData(new_name, new_mobile, new_mother, new_guardian, new_relegion, new_caste,
                new_bloodgrp, new_gender, new_abcid, new_prnno);
    }


    private void saveStudentData(String name, String mobile, String mother, String guardian
            , String relegion, String caste, String blood, String gender, String abcid, String prnno) {
        try {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("students").child(userId);

            Map<String, Object> updates = new HashMap<>();
            if(name.equals(oldName)==false){
                checkStudentAttendance(course,batch,oldName,oldName,new_name);
            }
            updates.put("name", name);
            updates.put("mobile", mobile);
            updates.put("mothername", mother);
            updates.put("guardian", guardian);
            updates.put("relegion", relegion);
            updates.put("caste", caste);
            updates.put("bloodgroup", blood);
            updates.put("gender", gender);
            updates.put("abcid", abcid);
            updates.put("prnno", prnno);

            userRef.updateChildren(updates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                edit_text_name.setText("");
                                edit_text_mobile.setText("");
                                edit_text_mother.setText("");
                                edit_text_guardian.setText("");
                                edit_text_relegion.setText("");
                                edit_text_caste.setText("");
                                edit_text_abc.setText("");
                                edit_text_blood.setText("");
                                edit_text_prn.setText("");
                                Intent intent = new Intent(EditProfile.this, Profile.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("course", course);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                                finish();

                                Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG);
        }
    }

    private void checkStudentAttendance(String courseId, String batchId, String studentId, String oldName, String newName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference courseRef = databaseReference.child("attendance").child(courseId).child(batchId);

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot courseSnapshot) {
                if (courseSnapshot.exists()) {
                    for (DataSnapshot subjectSnapshot : courseSnapshot.getChildren()) { // Loop through subjects
                        for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) { // Loop through dates
                            // Access student data for specific date
                            if (dateSnapshot.hasChild(studentId)) { // Check if student data exists
                                DataSnapshot studentData = dateSnapshot.child(studentId); // Student data under the date
                                String studentName = studentData.child("name").getValue(String.class);
                                if (studentName != null && studentName.equals(oldName)) {
                                    // Old name found! Create a new node with the new name
                                    DatabaseReference newStudentRef = dateSnapshot.child(newName).getRef();
                                    newStudentRef.setValue(studentData.getValue());

                                    // Update the name inside the new node
                                    newStudentRef.child("name").setValue(newName);

                                    // Remove the old node (studentId) after confirming the new node has been created successfully
                                    newStudentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot newStudentSnapshot) {
                                            if (newStudentSnapshot.exists()) {
                                                studentData.getRef().removeValue(); // Delete the old node
                                            } else {
                                                // Handle error if the new node wasn't created successfully
                                                Toast.makeText(EditProfile.this, "Failed to create new node with the new name", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle error
                                        }
                                    });
                                }
                            }
                        }
                    }
                } else {
                    // Handle case where course or batch doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    public void backToProfile(View view) {
        Intent intent = new Intent(this, Profile.class);
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}