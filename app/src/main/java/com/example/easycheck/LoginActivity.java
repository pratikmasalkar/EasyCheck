package com.example.easycheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmailMobileRoll, etPassword, etMobileOtp, etOtp;
    private Button btnLoginPassword, btnLoginOtp;
    private TextView dontAccUIdPass, tvLoginOtp, tvLoginPasswordBack, dontAccMobOtp;
    private LinearLayout downViewUIDPass, downViewMobOtp;
    private TextInputLayout etEmailMobileRollView, etPasswordView, etMobileOtpView, etOtpView;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, Welcome.class));
            finish();
        }


        etEmailMobileRoll = findViewById(R.id.etEmailMobileRoll);
        etPassword = findViewById(R.id.etPassword);


        etEmailMobileRollView = findViewById(R.id.etEmailMobileRollView);
        etPasswordView = findViewById(R.id.etPasswordView);
        btnLoginPassword = findViewById(R.id.btnLoginPassword);
        dontAccUIdPass = findViewById(R.id.dontAccUIdPass);
        downViewUIDPass = findViewById(R.id.downViewUIDPass);
        tvLoginOtp = findViewById(R.id.tvLoginOtp);

        etMobileOtpView = findViewById(R.id.etMobileOtpView);
        etOtpView = findViewById(R.id.etOtpView);
        btnLoginOtp = findViewById(R.id.btnLoginOtp);
        dontAccMobOtp = findViewById(R.id.dontAccMobOtp);
        downViewMobOtp = findViewById(R.id.downViewMobOtp);

        tvLoginPasswordBack = findViewById(R.id.tvLoginPasswordBack);

        String EmailMobileRoll = etEmailMobileRoll.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tvLoginOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEmailMobileRollView.setVisibility(View.GONE);
                etPasswordView.setVisibility(View.GONE);
                btnLoginPassword.setVisibility(View.GONE);
                tvLoginOtp.setVisibility(View.GONE);
                dontAccUIdPass.setVisibility(View.GONE);
                downViewUIDPass.setVisibility(View.GONE);

                etMobileOtpView.setVisibility(View.VISIBLE);
                etOtpView.setVisibility(View.VISIBLE);
                btnLoginOtp.setVisibility(View.VISIBLE);
                dontAccMobOtp.setVisibility(View.VISIBLE);
                downViewMobOtp.setVisibility(View.VISIBLE);
                tvLoginPasswordBack.setVisibility(View.VISIBLE);


            }
        });

        tvLoginPasswordBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEmailMobileRollView.setVisibility(View.VISIBLE);
                etPasswordView.setVisibility(View.VISIBLE);
                btnLoginPassword.setVisibility(View.VISIBLE);
                tvLoginOtp.setVisibility(View.VISIBLE);
                dontAccUIdPass.setVisibility(View.VISIBLE);
                downViewUIDPass.setVisibility(View.VISIBLE);

                etMobileOtpView.setVisibility(View.GONE);
                etOtpView.setVisibility(View.GONE);
                btnLoginOtp.setVisibility(View.GONE);
                dontAccMobOtp.setVisibility(View.GONE);
                downViewMobOtp.setVisibility(View.GONE);
                tvLoginPasswordBack.setVisibility(View.GONE);


            }
        });

        // ... (continued from previous code)

        btnLoginPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredId = etEmailMobileRoll.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validate input (add validation logic here)
                if (enteredId.isEmpty()) {
                    etEmailMobileRoll.setError("Enter User Id");
                    etEmailMobileRoll.requestFocus();
                    Toast.makeText(LoginActivity.this, "Please Enter User Id", Toast.LENGTH_LONG).show();
                    return;
                } else if ( password.isEmpty()) {
                    etPassword.setError("Enter Password");
                    Toast.makeText(LoginActivity.this, "Please Enter Password", Toast.LENGTH_LONG).show();
                    etPassword.requestFocus();
                    return;
                }

                // Determine ID type and perform authentication
                if (isValidEmail(enteredId)) {
                    fetchUserDataFromDatabase(enteredId, password,"email");
                } else if (isValidMobileNumber(enteredId)) {
                        fetchUserDataFromDatabase(enteredId,password,"mobile");
                } else if (isValidRollNumber(enteredId)) {
                    fetchUserDataFromDatabase(enteredId,password,"rollno");
                } else {
                    etEmailMobileRoll.setError("Invalid ID");
                    Toast.makeText(LoginActivity.this, "Enter Valid User Id", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void authenticateWithRollNumber(String enteredId, String password) {
    }

    private void initiateOTPAuthentication(String enteredId) {
    }


    private boolean isValidRollNumber(String enteredId) {
        if (enteredId.length() == 7) {
            return true;
        }
        return false;
    }

    private boolean isValidMobileNumber(String enteredId) {
        if (enteredId.length() == 10) {
            return true;
        }
        return false;
    }

    private boolean isValidEmail(String enteredId) {
        String emailRegex = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(enteredId);
        return matcher.matches();
    }



    private void fetchUserDataFromDatabase(String enteredId, String password,String id) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("students");
        userRef.orderByChild(id).equalTo(enteredId) // Or other ID type
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                            String email = userSnapshot.child("email").getValue(String.class);
                            String name=userSnapshot.child("name").getValue(String.class);

                            authenticateWithFirebaseAuth(email, password);
                        } else {
                            etEmailMobileRoll.setError("Invalid "+id);
                            Toast.makeText(LoginActivity.this, "Not found any record with "+enteredId, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void authenticateWithFirebaseAuth(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Authentication successful, proceed to Welcome Activity
                            startActivity(new Intent(LoginActivity.this, Welcome.class));
                        } else {
                            etPassword.setError("Wrong Password");
                            Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void register(View view){
        startActivity(new Intent(this, Registration.class));
    }


}
