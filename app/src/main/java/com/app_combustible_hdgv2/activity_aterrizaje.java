package com.app_combustible_hdgv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class activity_aterrizaje extends AppCompatActivity {
    String uname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aterrizaje);
        setTitle("HDGC-Aterrizajes");
        uname=getIntent().getStringExtra("nombre_usuario");
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_aterrizaje.this,MainActivity.class);
                intent.putExtra("nombre_usuario",uname);
                startActivity(intent);
                setResult(Activity.RESULT_OK);
            }
        });
    }



}