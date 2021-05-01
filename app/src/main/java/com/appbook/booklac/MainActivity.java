package com.appbook.booklac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText emailET,passwordET;
    ImageView passVi;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;




    @Override
        protected void onStart() {
            super.onStart();



            if(mAuth.getCurrentUser() != null){

                finish();
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordET = findViewById(R.id.passwordIn);
        emailET = findViewById(R.id.emailIn);
        progressBar = findViewById(R.id.progress_bar);
        passVi = findViewById(R.id.pass_vi);
        mAuth = FirebaseAuth.getInstance();
        SignInButton signInButton = findViewById(R.id.google_signin_btn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.sign_in_btn).setOnClickListener(this);
        findViewById(R.id.signup_btn).setOnClickListener(this);
        findViewById(R.id.pass_vi).setOnClickListener(this);
        findViewById(R.id.google_signin_btn).setOnClickListener(this);

    }

    private void googleSignIn(){

        Toast.makeText(getApplicationContext(), "Hi ", Toast.LENGTH_LONG).show();

    }

    private void showHidePass(){

        if(passwordET.getTransformationMethod() == null){
            passVi.setImageResource(R.drawable.outline_visibility_24);
            passwordET.setTransformationMethod(new PasswordTransformationMethod());
        }
        else {
            passVi.setImageResource(R.drawable.outline_visibility_off_24);
            passwordET.setTransformationMethod(null);
        }
    }

    private void logIn(){


        String password = passwordET.getText().toString().trim();
        String email = emailET.getText().toString().trim();

        if(password.isEmpty()){

            passwordET.setError("Password is required");
            passwordET.requestFocus();
            return;

        }
        else if(password.length() < 6){

            passwordET.setError("Password length must be 6 at least");
            passwordET.requestFocus();
            return;

        }

        if(email.isEmpty()){

            emailET.setError("E-mail is required");
            emailET.requestFocus();
            return;

        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            emailET.setError("Please enter a valid e-mail");
            emailET.requestFocus();
            return;

        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);

                if(task.isSuccessful()){

                    finish();
                    Toast.makeText(getApplicationContext(), "Login Successed", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, Profile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
                else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.sign_in_btn:
                logIn();
                break;

            case R.id.pass_vi:
                showHidePass();
                break;

            case R.id.signup_btn:
                finish();
                startActivity(new Intent(MainActivity.this, SignUp.class));
                break;

            case R.id.google_signin_btn:
                googleSignIn();
                break;
        }

    }
}