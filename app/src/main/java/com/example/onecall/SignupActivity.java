package com.example.onecall;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText email_signup, password_signup, name_signup;
    Button signup_btn;
    TextView login_back;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        email_signup = findViewById(R.id.emailSignup);
        password_signup = findViewById(R.id.passwordSignup);
        name_signup = findViewById(R.id.nameSignup);
        signup_btn = findViewById(R.id.signupButton);
        login_back = findViewById(R.id.login_Back);
        mAuth = FirebaseAuth.getInstance();

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = name_signup.getText().toString().trim();
                String email = email_signup.getText().toString();
                String password = password_signup.getText().toString();

                if(TextUtils.isEmpty(uname)){
                    name_signup.setError("Username is required");
                    name_signup.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    email_signup.setError("Email is required");
                    email_signup.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    password_signup.setError("Password is required");
                    password_signup.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    email_signup.setError("Invalid email format");
                    email_signup.requestFocus();
                    return;
                }
                if (password.length() < 6) {
                    password_signup.setError("Password must be at least 6 characters");
                    password_signup.requestFocus();
                    return;
                }



                firebaseSignup(uname, email, password);

            }
        });










        login_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }




    void firebaseSignup(String uname, String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if email is already registered
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            Toast.makeText(SignupActivity.this, "Email already in use. Please log in or use a different email.", Toast.LENGTH_LONG).show();
                        } else {
                            // Proceed with signup
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(signupTask -> {
                                        if (signupTask.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            String uid = user.getUid();

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("username", uname);
                                            userData.put("email", email);
                                            userData.put("uid", uid);
                                            userData.put("createdAt", FieldValue.serverTimestamp());

                                            db.collection("users").document(uid)
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(SignupActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                                        UpdateUser(uname);
                                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w(TAG, "Error writing document", e);
                                                        Toast.makeText(SignupActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                    });

                                        } else {
                                            Exception e = signupTask.getException();
                                            if (e instanceof FirebaseAuthUserCollisionException) {
                                                Toast.makeText(SignupActivity.this, "This email is already registered.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            Log.w(TAG, "createUserWithEmail:failure", e);
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Error checking email. Try again.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "fetchSignInMethodsForEmail:failure", task.getException());
                    }
                });
    }



    void UpdateUser(String uname){
        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();

        if (newUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(uname)
                    .build();

            newUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                    }
                }
            });
        }


    }
}