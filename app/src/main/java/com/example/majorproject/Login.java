package com.example.majorproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();

        //Initialize all layouts
        buttonLogin=findViewById(R.id.buttonLogin);
        buttonBack=findViewById(R.id.buttonBack);
        editTextEmail=findViewById(R.id.editTextSignInEmail);
        editTextPassword=findViewById(R.id.editTextSignInPassword);

        //add listener to the butttons
        buttonLogin.setOnClickListener(this);
        buttonBack.setOnClickListener(this);

        if(firebaseAuth.getCurrentUser()!=null){
            Toast.makeText(Login.this, "User is already logged in",
                    Toast.LENGTH_LONG).show();
            finish();
            Intent mainActivity = new Intent(Login.this, MainActivity.class);
            startActivity(mainActivity);
        }
    }

    public void userLogin(){
        String email=editTextEmail.getText().toString();
        String password=editTextPassword.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //successfully registered
                            Toast.makeText(Login.this, "Signed in",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            Intent mainActivity = new Intent(Login.this, MainActivity.class);
                            startActivity(mainActivity);
                        }
                        else{
                            Toast.makeText(Login.this, "Invalid information! Try again",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    @Override
    public void onClick(View view) {
        if(view==buttonLogin){
            userLogin();
        }
        if(view==buttonBack){
            finish();
            Intent signupActivity = new Intent(Login.this, Signup.class);
            startActivity(signupActivity);
        }
    }
}
