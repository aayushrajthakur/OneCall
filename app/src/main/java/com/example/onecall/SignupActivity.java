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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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
        user = mAuth.getCurrentUser();

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

    void firebaseSignup(String uname, String email, String password){
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //saveToDatabase(uname,email,number,password);
                            Toast.makeText(SignupActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(uname)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                    }
                                }
                            });
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}