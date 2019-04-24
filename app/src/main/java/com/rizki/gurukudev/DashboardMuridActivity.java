package com.rizki.gurukudev;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class DashboardMuridActivity extends AppCompatActivity {

    Button btnLogout, btnProfile;
    private CardView cvGuru, cvVideo;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_murid);

        mAuth = FirebaseAuth.getInstance();

        cvGuru = findViewById(R.id.cvGuru);
        cvGuru.setOnClickListener(v -> { Intent i = new Intent(DashboardMuridActivity.this, MuridMapsActivity.class);
        startActivity(i);
        finish();
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            LogOutUser();
        });

        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardMuridActivity.this, SettingsActivity.class);
            intent.putExtra("type", "Murid-murid");
            startActivity(intent);
            finish();
        });

        cvVideo = findViewById(R.id.cvVideo);
        cvVideo.setOnClickListener(v -> {
            Intent i = new Intent(DashboardMuridActivity.this, VideoActivity.class);
            startActivity(i);
            finish();
        });

    }

    public void LogOutUser()
    {
        Intent startPageIntent = new Intent(DashboardMuridActivity.this, Splash.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }
}
