package com.appbook.booklac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileInfo extends Fragment {

    private EditText name,email,password,phone,age;
    TextView emailVer;
    private Button edit,save,logout;
    private ImageView passVis;
    View parentHolder;

    String namee;
    String passworde;
    String emaile;
    String phonee;
    String agee;

    DatabaseReference dbr;
    private FirebaseAuth auth;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        auth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = auth.getCurrentUser();

         parentHolder = inflater.inflate(R.layout.fragment_profile_info, container, false);

        name = parentHolder.findViewById(R.id.profile_name);
        email = parentHolder.findViewById(R.id.profile_email);
        password = parentHolder.findViewById(R.id.profile_password);
        phone = parentHolder.findViewById(R.id.profile_phone);
        age = parentHolder.findViewById(R.id.profile_age);
        edit = parentHolder.findViewById(R.id.edit_btn);
        save = parentHolder.findViewById(R.id.save_btn);
        logout = parentHolder.findViewById(R.id.logout_btn);
        passVis = parentHolder.findViewById(R.id.pass_vis);
        emailVer = parentHolder.findViewById(R.id.email_ver);

        if(firebaseUser.isEmailVerified()){

            emailVer.setText("Email Verified");
        }
        else{

            emailVer.setText("Email is not Verified (Click To Verify)");
            emailVer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Toast.makeText(getContext(), "Verification email Sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        dbr = FirebaseDatabase.getInstance().getReference().child("Users").
                child(auth.getCurrentUser().getUid());

        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                name.setText(dataSnapshot.child("name").getValue().toString());
                password.setText(dataSnapshot.child("password").getValue().toString());
                phone.setText(dataSnapshot.child("phone").getValue().toString());
                age.setText(dataSnapshot.child("age").getValue().toString());
                email.setText(dataSnapshot.child("email").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });


        passVis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(password.getTransformationMethod() == null){
                    passVis.setImageResource(R.drawable.baseline_visibility_24);
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
                else {
                    passVis.setImageResource(R.drawable.baseline_visibility_off_24);
                    password.setTransformationMethod(null);
                }
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                auth.signOut();
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
                ((Activity) getActivity()).overridePendingTransition(0, 0);
                Toast.makeText(getContext(), "Logging out..", Toast.LENGTH_SHORT).show();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name.setEnabled(true);
                email.setEnabled(true);
                password.setEnabled(true);
                phone.setEnabled(true);
                age.setEnabled(true);
                edit.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
                save.setClickable(true);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 namee =  name.getText().toString();
                 passworde = password.getText().toString().trim();
                 emaile = email.getText().toString().trim();
                 phonee = phone.getText().toString().trim();
                 agee = age.getText().toString().trim();

                if(namee.isEmpty()){

                    name.setError("Username is required");
                    name.requestFocus();
                    return;

                }
                else if(namee.length() > 20){

                    name.setError("Name length must be 20 maximum");
                    name.requestFocus();
                    return;

                }


                if(passworde.isEmpty()){

                    password.setError("Password is required");
                    password.requestFocus();
                    return;

                }
                else if(passworde.length() < 6){

                    password.setError("Password length must be 6 at least");
                    password.requestFocus();
                    return;

                }

                if(emaile.isEmpty()){

                    email.setError("E-mail is required");
                    email.requestFocus();
                    return;

                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(emaile).matches()){

                    email.setError("Please enter a valid e-mail");
                    email.requestFocus();
                    return;

                }

                if(phonee.isEmpty()){

                    phone.setError("Phone number is required");
                    phone.requestFocus();
                    return;

                }
                else if (!Patterns.PHONE.matcher(phonee).matches()){

                    phone.setError("Please enter a valid phone number");
                    phone.requestFocus();
                    return;

                }

                if(agee.isEmpty()){

                    age.setError("Age is required");
                    age.requestFocus();

                }
                else if (Integer.parseInt(agee) < 13){

                    age.setError("You must be at least 13 years so you can sign up");
                    age.requestFocus();

                }

                FirebaseAuth.getInstance().getCurrentUser().updateEmail(emaile);
                FirebaseAuth.getInstance().getCurrentUser().updatePassword(passworde);
                User user = new User(agee, emaile, namee, passworde, phonee);
                dbr.setValue(user);

                name.setEnabled(false);
                email.setEnabled(false);
                password.setEnabled(false);
                phone.setEnabled(false);
                age.setEnabled(false);
                save.setClickable(false);
                save.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);

                Toast.makeText(getContext(), "User profile updated successfully", Toast.LENGTH_LONG).show();

            }
        });
        return parentHolder;
    }

}