package com.agrinetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.agrinetwork.R;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);




    }
}