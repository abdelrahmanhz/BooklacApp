package com.appbook.booklac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    ProgressBar progressBar;
    EditText usernameET,passwordET,emailET,phoneET,ageET;
    private ImageView passVisi;
    FirebaseAuth mAuth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameET = findViewById(R.id.usernameUp);
        passwordET = findViewById(R.id.passwordUp);
        emailET = findViewById(R.id.emailUp);
        phoneET = findViewById(R.id.phoneUp);
        ageET = findViewById(R.id.ageUp);
        passVisi = findViewById(R.id.pass_visi);
        progressBar = findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.sign_up_btn).setOnClickListener(this);
        findViewById(R.id.signin_btn).setOnClickListener(this);
        findViewById(R.id.pass_visi).setOnClickListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){

            finish();
            startActivity(new Intent(SignUp.this, Profile.class));
        }
    }

    private void showHidePass(){

        if(passwordET.getTransformationMethod() == null){
            passVisi.setImageResource(R.drawable.outline_visibility_24);
            passwordET.setTransformationMethod(new PasswordTransformationMethod());
        }
        else {
            passVisi.setImageResource(R.drawable.outline_visibility_off_24);
            passwordET.setTransformationMethod(null);
        }
    }

    private void registerUser(){

        final String name = usernameET.getText().toString();
        final String password = passwordET.getText().toString().trim();
        final String email = emailET.getText().toString().trim();
        final String phoneNumber = phoneET.getText().toString().trim();
        final String age = ageET.getText().toString().trim();

        if(name.isEmpty()){

            usernameET.setError("Username is required");
            usernameET.requestFocus();
            return;

        }
        else if(name.length() > 20){

            passwordET.setError("Name length must be 20 maximum");
            passwordET.requestFocus();
            return;

        }


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

        if(phoneNumber.isEmpty()){

            phoneET.setError("Phone number is required");
            phoneET.requestFocus();
            return;

        }
        else if (!Patterns.PHONE.matcher(phoneNumber).matches()){

            phoneET.setError("Please enter a valid phone number");
            phoneET.requestFocus();
            return;

        }

        if(age.isEmpty()){

            ageET.setError("Age is required");
            ageET.requestFocus();
            return;

        }
        else if (Integer.parseInt(age) < 13){

            ageET.setError("You must be at least 13 years so you can sign up");
            ageET.requestFocus();
            return;

        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);

                if(task.isSuccessful()){
                    User user = new User(
                            age,
                            email,
                            name,
                            password,
                            phoneNumber
                    );


                    FirebaseDatabase.getInstance().getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {

                            if(task2.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "user object is working", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    finish();
                    Toast.makeText(getApplicationContext(), "SignUp Successed", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignUp.this, Profile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else{

                    if(task.getException() instanceof FirebaseAuthUserCollisionException){

                        Toast.makeText(getApplicationContext(), "E-mail is already registered", Toast.LENGTH_LONG).show();
                    }
                    else{

                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                }

            }
        });


    }



    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.sign_up_btn:
                registerUser();
                break;

            case R.id.pass_visi:
                showHidePass();
                break;

            case R.id.signin_btn:
                finish();
                startActivity(new Intent(SignUp.this, MainActivity.class));
                break;

        }

    }
}