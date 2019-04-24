package com.rizki.gurukudev;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MuridLoginRegisActivity extends AppCompatActivity {

    private Button btnLoginMurid, btnRegisMurid;
    private TextView tvhalo, tvLogin;
    private EditText etEmailMurid, etPasswordMurid;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference MuridDbRef;
    private String onlineMuridId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_murid_login_regis);

        btnLoginMurid    = findViewById(R.id.btn_LoginMurid);
        tvLogin          = findViewById(R.id.tv_loginMurid);
        etEmailMurid     = findViewById(R.id.etEmailMurid);
        etPasswordMurid  = findViewById(R.id.etPasswordMurid);
        mAuth            = FirebaseAuth.getInstance();
        loadingBar       = new ProgressDialog(this);
        btnRegisMurid    = findViewById(R.id.btn_RegisMurid);
        tvhalo           = findViewById(R.id.tvhalo);

        btnRegisMurid.setVisibility(View.GONE);
        tvhalo.setOnClickListener(v -> {
            btnLoginMurid.setVisibility(View.GONE);
            tvhalo.setVisibility(View.GONE);
            btnRegisMurid.setVisibility(View.VISIBLE);
            btnRegisMurid.setText("Register Now");
            tvLogin.setText("Register\nsebagai Murid");
        });

        btnRegisMurid.setOnClickListener(v -> {
            String email = etEmailMurid.getText().toString();
            String password = etPasswordMurid.getText().toString();
            RegisterMurid(email, password);
        });

        btnLoginMurid.setOnClickListener(v -> {
            String email = etEmailMurid.getText().toString();
            String password = etPasswordMurid.getText().toString();
            LoginMurid(email, password);
        });
    }

    private void LoginMurid(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(MuridLoginRegisActivity.this, "E-mail nya jangan kosong dong", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(MuridLoginRegisActivity.this, "Hayo Password nya masih kosong tuh", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Mencoba Login . . .");
            loadingBar.setMessage("Tunggu sebentar ya! Data sedang diproses!");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Intent i = new Intent(MuridLoginRegisActivity.this, DashboardMuridActivity.class);
                            startActivity(i);
                            Toast.makeText(MuridLoginRegisActivity.this, "Halo! Selamat Datang!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        else{
                            Toast.makeText(MuridLoginRegisActivity.this, "Yah Login nya gagal. Coba lagi nanti ya!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void RegisterMurid(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(MuridLoginRegisActivity.this, "E-mail nya jangan kosong dong", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(MuridLoginRegisActivity.this, "Hayo Password nya masih kosong tuh", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Pendaftaran Murid");
            loadingBar.setMessage("Tunggu sebentar ya! Data sedang diproses");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            onlineMuridId    = mAuth.getCurrentUser().getUid();
                            MuridDbRef       = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child("Murid").child(onlineMuridId);
                            MuridDbRef.setValue(true);
                            Toast.makeText(MuridLoginRegisActivity.this, "Horay! Pendaftaran berhasil!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            Intent i = new Intent(MuridLoginRegisActivity.this, DashboardMuridActivity.class);
                            startActivity(i);

                        }
                        else{
                            Toast.makeText(MuridLoginRegisActivity.this, "Yah registrasi nya gagal. Coba lagi nanti ya!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent reg = new Intent(this, Splash.class);
        startActivity(reg);
        finish();
    }
}