package com.rizki.gurukudev;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class GuruLoginRegisActivity extends AppCompatActivity {

    private EditText etEmailGuru, etPasswordGuru;
    private Button btn_LoginGuru;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guru_login_regis);

        etEmailGuru      = findViewById(R.id.etEmailGuru);
        etPasswordGuru   = findViewById(R.id.etPasswordGuru);
        btn_LoginGuru    = findViewById(R.id.btn_LoginGuru);
        mAuth            = FirebaseAuth.getInstance();
        loadingBar       = new ProgressDialog(this);

        btn_LoginGuru.setOnClickListener(v -> {
            String email = etEmailGuru.getText().toString();
            String password = etPasswordGuru.getText().toString();
            loginGuru(email, password);
        });
    }

    private void loginGuru(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(GuruLoginRegisActivity.this, "E-mail nya jangan kosong dong", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(GuruLoginRegisActivity.this, "Hayo Password nya masih kosong tuh", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Mencoba Login . . .");
            loadingBar.setMessage("Tunggu sebentar ya! Data sedang diproses!");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(GuruLoginRegisActivity.this, "Selamat Datang Guru!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                                Intent i = new Intent(GuruLoginRegisActivity.this, GuruMapsActivity.class);
                                startActivity(i);
                        }
                        else{
                            Toast.makeText(GuruLoginRegisActivity.this, "Yah Login nya gagal. Coba lagi nanti ya!", Toast.LENGTH_SHORT).show();
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
