package com.rizki.gurukudev;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Splash extends AppCompatActivity {

    RelativeLayout rellay1;
    Button btn_Guru, btn_Murid;
    ImageView iv_logo;
    Handler handlder = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
            iv_logo.setVisibility(View.GONE);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        iv_logo = findViewById(R.id.iv_logo);
        rellay1 = findViewById(R.id.rellay1);
        handlder.postDelayed(runnable, 2000);

        requestPermission();

        btn_Guru = findViewById(R.id.btn_Guru);
        btn_Guru.setOnClickListener(v -> {
            Intent i = new Intent(Splash.this, GuruLoginRegisActivity.class);
            startActivity(i);
            finish();
        });

        btn_Murid = findViewById(R.id.btn_Murid);
        btn_Murid.setOnClickListener(v -> {
            Intent i = new Intent(Splash.this, MuridLoginRegisActivity.class);
            startActivity(i);
            finish();
        });
    }

    //request permission
    private void requestPermission() {
        int ALL_PERMISSIONS = 1;

        final String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE
        };

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
    }
}
