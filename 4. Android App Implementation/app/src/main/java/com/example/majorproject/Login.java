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

public class Login extends AppCompatActivity implements View.OnClickListener {
    //Necessary views variables to be used on screen
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonLogin;
    Button buttonBack;
    FirebaseAuth firebaseAuth;
    LinearLayout details;

    /* Default method called by android os when a new view is created*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        //Initialize all layouts
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonBack = findViewById(R.id.buttonBack);
        editTextEmail = findViewById(R.id.editTextSignInEmail);
        editTextPassword = findViewById(R.id.editTextSignInPassword);
        details = findViewById(R.id.details);


        //add listener to the buttons
        buttonLogin.setOnClickListener(this);
        buttonBack.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null) {
            showToast("User is already logged in");
            finish();
            Intent mainActivity = new Intent(Login.this, MainActivity.class);
            startActivity(mainActivity);
        }
    }

    /*Function to perform user login task and show necessary messages about progress
    And start main activity after successful login
    * */
    public void userLogin() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //successfully registered toast message
                            showToast("Signed in");

                            //finish this activity and call new activity which is MainActivity of APP
                            finish();
                            Intent mainActivity = new Intent(Login.this, MainActivity.class);
                            startActivity(mainActivity);
                        } else {
                            //Show invalid message if UserID/Password doesn't match
                            showToast("Invalid information! Try again");
                        }
                    }
                });
    }

    /*For each view clicked perform necessary actions associated to that*/
    @Override
    public void onClick(View view) {
        if (view == buttonLogin) {
            userLogin();
        }
        if (view == buttonBack) {
            //if back button is pressed then clear this activity from memory and start registration screen
            finish();
            Intent signupActivity = new Intent(Login.this, Signup.class);
            startActivity(signupActivity);
        }
    }

    /*Method to show short length toast messages*/
    public void showToast(String toastMessage) {
        Toast.makeText(Login.this, toastMessage,
                Toast.LENGTH_LONG).show();
    }
}
