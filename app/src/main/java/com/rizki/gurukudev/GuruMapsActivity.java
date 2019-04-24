package com.rizki.gurukudev;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GuruMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location LastLocation;
    LocationRequest locationRequest;

    private Button LogoutGuruBtn, SettingsGuruButton;
    private ImageView iv_sms, iv_telfon;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogOutUserStatus = false;

    //getting request customer's id
    private String muridID = "";
    private String guruID, txt_nohp;
    private DatabaseReference AssignedMuridRef, AssignedMuridPickUpRef;
    Marker PickUpMarker;

    private ValueEventListener AssignedMuridPickUpRefListner;

    private TextView txtName, txtPhone;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //notice
        setContentView(R.layout.activity_guru_maps);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        guruID = mAuth.getCurrentUser().getUid();

        LogoutGuruBtn = findViewById(R.id.logout_guru_btn);
        SettingsGuruButton = findViewById(R.id.settings_guru_btn);
        txtName = findViewById(R.id.name_murid);
        txtPhone = findViewById(R.id.phone_murid);
        profilePic = findViewById(R.id.profile_image_murid);
        relativeLayout = findViewById(R.id.rel2);
        iv_sms = findViewById(R.id.sms);
        iv_telfon = findViewById(R.id.telfon);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(GuruMapsActivity.this);

        SettingsGuruButton.setOnClickListener(view -> {
            Intent intent = new Intent(GuruMapsActivity.this, SettingsActivity.class);
            intent.putExtra("type", "Guru-guru");
            startActivity(intent);
        });

        LogoutGuruBtn.setOnClickListener(v -> {
            currentLogOutUserStatus = true;
            DisconnectGuru();

            mAuth.signOut();

            LogOutUser();
        });

        iv_sms.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Murid-murid").child(muridID);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        txtPhone.setText(phone);
                        txt_nohp = txtPhone.getText().toString();
                        //Toast.makeText(GuruMapsActivity.this, "Halo Murid "+txt_sms, Toast.LENGTH_SHORT).show();

                        Intent sms = new Intent(Intent.ACTION_VIEW);
                        sms.putExtra("address", txt_nohp);
                        sms.putExtra("sms_body", "Tunggu sebentar ya, saya sedang dalam perjalanan");
                        sms.setType("vnd.android-dir/mms-sms");
                        startActivity(sms);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });

        iv_telfon.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Murid-murid").child(muridID);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        txtPhone.setText(phone);
                        txt_nohp = txtPhone.getText().toString();

                        Intent telfon = new Intent(Intent.ACTION_CALL);
                        telfon.setData(Uri.parse("tel:" + txt_nohp));

                        if (ActivityCompat.checkSelfPermission(GuruMapsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(telfon);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });

        getAssignedMuridRequest();
    }



    private void getAssignedMuridRequest()
    {
        AssignedMuridRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Guru-guru").child(guruID).child("BelajarID");

        AssignedMuridRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    muridID = dataSnapshot.getValue().toString();
                    //getting assigned customer location
                    GetAssignedMuridPickupLocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedMuridInformation();
                }
                else
                {
                    muridID = "";

                    if (PickUpMarker != null)
                    {
                        PickUpMarker.remove();
                    }

                    if (AssignedMuridPickUpRefListner != null)
                    {
                        AssignedMuridPickUpRef.removeEventListener(AssignedMuridPickUpRefListner);
                    }

                    relativeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void GetAssignedMuridPickupLocation()
    {
        AssignedMuridPickUpRef = FirebaseDatabase.getInstance().getReference().child("Permintaan Murid")
                .child(muridID).child("l");

        AssignedMuridPickUpRefListner = AssignedMuridPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    List<Object> muridLocationMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    if(muridLocationMap.get(0) != null)
                    {
                        LocationLat = Double.parseDouble(muridLocationMap.get(0).toString());
                    }
                    if(muridLocationMap.get(1) != null)
                    {
                        LocationLng = Double.parseDouble(muridLocationMap.get(1).toString());
                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Belajar nya di sini").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // now let set user location enable
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState)
    {
        super.onCreate(savedInstanceState, persistentState);


    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(getApplicationContext() != null)
        {
            //getting the updated location
            LastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DriversAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Guru yang Tersedia");
            GeoFire geoFireAvailability = new GeoFire(DriversAvailabilityRef);

            DatabaseReference GuruMengajarRef = FirebaseDatabase.getInstance().getReference().child("Guru sedang Mengajar");
            GeoFire geoFireWorking = new GeoFire(GuruMengajarRef);

            switch (muridID)
            {
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }


    @Override
    protected void onStop()
    {
        super.onStop();

        if(!currentLogOutUserStatus)
        {
            DisconnectGuru();
        }
    }


    private void DisconnectGuru()
    {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference GuruTersediaRef = FirebaseDatabase.getInstance().getReference().child("Guru yang Tersedia");

        GeoFire geoFire = new GeoFire(GuruTersediaRef);
        geoFire.removeLocation(userID);
    }



    public void LogOutUser()
    {
        Intent startPageIntent = new Intent(GuruMapsActivity.this, Splash.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }




    private void getAssignedMuridInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Murid-murid").child(muridID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);

                    if (dataSnapshot.hasChild("image"))
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
