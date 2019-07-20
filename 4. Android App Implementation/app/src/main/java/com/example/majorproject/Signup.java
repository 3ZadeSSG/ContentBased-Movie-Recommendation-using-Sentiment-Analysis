package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity implements View.OnClickListener {
    //Layout holders
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignup;
    Button buttonLogin;
    LinearLayout details;

    //Firebase object to perform the registration
    FirebaseAuth firebaseAuth;

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

        //if user is already logged in then start main activity screen and kill this present activity
        if (firebaseAuth.getCurrentUser() != null) {
            showToast("User is already logged in");
            finish();
            Intent mainActivity = new Intent(Signup.this, MainActivity.class);
            startActivity(mainActivity);
        }

    }

    /*Method to register user using Firebase authentication using email address and password*/
    void registerUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        if (email.equals("") || password.equals("")) {
            showToast("Invalid data");
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //successfully registered
                                showToast("Registered");
                                Intent mainActivity = new Intent(Signup.this, MainActivity.class);
                                startActivity(mainActivity);
                            } else {
                                //if registration failed then show error
                                showToast("Registration Failed");
                            }
                        }
                    });
        }
    }

    /*Perform necessary actions based on views clicked on screen*/
    @Override
    public void onClick(View view) {
        if (view == buttonSignup) {
            registerUser();
        }
        //if login button is pressed then call the login activity and kill current activity
        if (view == buttonLogin) {
            Intent loginActivity = new Intent(Signup.this, Login.class);
            startActivity(loginActivity);
        }
    }

    /*Method to show short time toast messages*/
    public void showToast(String toastMessage) {
        Toast.makeText(Signup.this, toastMessage,
                Toast.LENGTH_LONG).show();
    }
}
