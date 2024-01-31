package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dashboard extends AppCompatActivity {
    private TextView userName;
    private String batchName,name,rollno,course;
    private ListView list_subjects;
    Map<String, String> subjects;
    private DatabaseReference batchRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userName = findViewById(R.id.userName);
        list_subjects = findViewById(R.id.list_subjects);

        // Check for current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
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
            // Handle the case where no user is logged in
            // (You might want to redirect to the login screen)
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
                            android.R.layout.simple_list_item_1, subjectNames);
                    list_subjects.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, error.getMessage(), Toast.LENGTH_LONG).show();
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

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}