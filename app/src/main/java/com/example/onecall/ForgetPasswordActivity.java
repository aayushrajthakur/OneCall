package com.example.onecall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    TextInputEditText verifyEmail;
    Button verify;
    TextView back;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);

        verifyEmail = findViewById(R.id.email_forget);
        verify = findViewById(R.id.resetPasswordButton);
        back = findViewById(R.id.backToLogin);
        progressBar = findViewById(R.id.progressbar);
        mAuth = FirebaseAuth.getInstance();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                String email = verifyEmail.getText().toString().trim();
                if(validEmail(email)){
                    resetPassword(email);
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    boolean validEmail(String email){
        if(email.isEmpty()) {
            verifyEmail.setError("Email cannot be empty");
            verifyEmail.requestFocus();
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(ForgetPasswordActivity.this, "Please, enter valid email", Toast.LENGTH_SHORT).show();
            verifyEmail.setError("Valid email are required");
            verifyEmail.requestFocus();
            return false;
        }
        return true;
    }

    void resetPassword(String email){
        mAuth = FirebaseAuth.getInstance();
        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ForgetPasswordActivity.this, "Verification mail sent!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                verify.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgetPasswordActivity.this, "Verification failed /n Try Again", Toast.LENGTH_SHORT).show();
            }
        });
    }

}