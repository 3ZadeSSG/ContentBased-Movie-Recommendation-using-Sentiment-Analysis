package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity implements View.OnClickListener {
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignup;
    Button buttonLogin;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    LinearLayout details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        firebaseAuth = FirebaseAuth.getInstance();

        //initialize views
        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextPassword = findViewById(R.id.editTextSignUpPassword);
        buttonSignup = findViewById(R.id.buttonSignUp);
        buttonLogin = findViewById(R.id.buttonStartLoginScreen);
        details = findViewById(R.id.details);

        //add onclick listener
        buttonSignup.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null) {
            Toast.makeText(Signup.this, "User is already logged in",
                    Toast.LENGTH_LONG).show();
            finish();
            Intent mainActivity = new Intent(Signup.this, MainActivity.class);
            startActivity(mainActivity);
        }

        progressBar = findViewById(R.id.spin_kit);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);
    }

    void registerUser() {
        progressBar.setVisibility(View.VISIBLE);
        details.setVisibility(View.INVISIBLE);

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        if (email.equals("") || password.equals("")) {
            Toast.makeText(Signup.this, "Invalid data",
                    Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //successfully registered
                                Toast.makeText(Signup.this, "Registered",
                                        Toast.LENGTH_LONG).show();
                                Intent mainActivity = new Intent(Signup.this, MainActivity.class);
                                startActivity(mainActivity);
                            } else {
                                Toast.makeText(Signup.this, "Registeration Failed!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        progressBar.setVisibility(View.INVISIBLE);
        details.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view == buttonSignup) {
            registerUser();
        }
        if (view == buttonLogin) {
            Intent loginActivity = new Intent(Signup.this, Login.class);
            startActivity(loginActivity);
        }
    }
}
