package com.supercoders.firebaseauth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView username=findViewById(R.id.username);
        TextView email=findViewById(R.id.phone);
        TextView phone=findViewById(R.id.email);

        FirebaseAuth auth=FirebaseAuth.getInstance();

        if(auth!=null){
            if(auth.getCurrentUser()!=null){
                if(auth.getCurrentUser().getPhoneNumber()!=null){
                    phone.setText("Phone : "+auth.getCurrentUser().getPhoneNumber());
                }
                if(auth.getCurrentUser().getDisplayName()!=null){
                    username.setText("Username : "+auth.getCurrentUser().getDisplayName());
                }
                if(auth.getCurrentUser().getEmail()!=null){
                    email.setText("Email : "+auth.getCurrentUser().getEmail());
                }
            }
        }
    }
}