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

public class Login extends AppCompatActivity implements View.OnClickListener {
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonLogin;
    Button buttonBack;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    LinearLayout details;

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

        //add listener to the butttons
        buttonLogin.setOnClickListener(this);
        buttonBack.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null) {
            showToast("User is already logged in");
            finish();
            Intent mainActivity = new Intent(Login.this, MainActivity.class);
            startActivity(mainActivity);
        }
        progressBar = findViewById(R.id.spin_kit);
        DoubleBounce myProgressBar = new DoubleBounce();
        progressBar.setIndeterminateDrawable(myProgressBar);
    }

    public void userLogin() {
        progressBar.setVisibility(View.VISIBLE);
        details.setVisibility(View.INVISIBLE);
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //successfully registered
                            showToast("Signed in");
                            finish();
                            Intent mainActivity = new Intent(Login.this, MainActivity.class);
                            startActivity(mainActivity);
                        } else {
                            showToast("Invalid information! Try again");
                        }
                    }
                });
        progressBar.setVisibility(View.INVISIBLE);
        details.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view == buttonLogin) {
            userLogin();
        }
        if (view == buttonBack) {
            finish();
            Intent signupActivity = new Intent(Login.this, Signup.class);
            startActivity(signupActivity);
        }
    }

    public void showToast(String toastMessage) {
        Toast.makeText(Login.this, toastMessage,
                Toast.LENGTH_LONG).show();
    }
}
