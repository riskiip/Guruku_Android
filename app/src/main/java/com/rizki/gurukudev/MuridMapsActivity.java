package com.rizki.gurukudev;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
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

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MuridMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location LastLocation;
    LocationRequest locationRequest;

    private Button PanggilGuruButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference MuridDatabaseRef;
    private LatLng MuridPickUpLocation;

    private DatabaseReference GuruTersediaRef, LokasiGuruRef, GuruRef;
    private int radius = 1;

    private Boolean guruDitemukan = false, requestType = false;
    private String guruDitemukanID, txt_nohp, muridID;
    Marker GuruMarker, PickUpMarker;
    GeoQuery geoQuery;

    private ValueEventListener LokasiGuruRefListner;

    private TextView txtName, txtPhone, txtSpesialisGuru;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;
    private ImageView iv_smsGuru, iv_telfonGuru;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_murid_maps);

        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        muridID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MuridDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Permintaan Murid");
        GuruTersediaRef = FirebaseDatabase.getInstance().getReference().child("Guru yang Tersedia");
        LokasiGuruRef = FirebaseDatabase.getInstance().getReference().child("Guru sedang Mengajar");

        PanggilGuruButton = findViewById(R.id.panggil_guru_button);

        txtName = findViewById(R.id.name_guru);
        txtPhone = findViewById(R.id.phone_guru);
        txtSpesialisGuru = findViewById(R.id.spesialis_guru);
        profilePic = findViewById(R.id.profile_image_driver);
        relativeLayout = findViewById(R.id.rel1);
        iv_smsGuru = findViewById(R.id.smsGuru);
        iv_telfonGuru = findViewById(R.id.telfonGuru);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        iv_smsGuru.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Guru-guru").child(guruDitemukanID);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        txtPhone.setText(phone);
                        txt_nohp = txtPhone.getText().toString();

                        Intent sms = new Intent(Intent.ACTION_VIEW);
                        sms.putExtra("address", txt_nohp);
                        sms.setType("vnd.android-dir/mms-sms");
                        startActivity(sms);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });

        iv_telfonGuru.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Guru-guru").child(guruDitemukanID);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        txtPhone.setText(phone);
                        txt_nohp = txtPhone.getText().toString();

                        Intent telfon = new Intent(Intent.ACTION_CALL);
                        telfon.setData(Uri.parse("tel:" + txt_nohp));

                        if (ActivityCompat.checkSelfPermission(MuridMapsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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



        PanggilGuruButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (requestType)
                {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    LokasiGuruRef.removeEventListener(LokasiGuruRefListner);

                    if (guruDitemukan != null)
                    {
                        GuruRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Guru-guru").child(guruDitemukanID).child("BelajarID");

                        GuruRef.removeValue();

                        guruDitemukanID = null;
                    }

                    guruDitemukan = false;
                    radius = 1;

                    String muridID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(MuridDatabaseRef);
                    geoFire.removeLocation(muridID);

                    if (PickUpMarker != null)
                    {
                        PickUpMarker.remove();
                    }
                    if (GuruMarker != null)
                    {
                        GuruMarker.remove();
                    }

                    PanggilGuruButton.setText("Panggil Guru");
                    relativeLayout.setVisibility(View.GONE);
                }
                else
                {
                    requestType = true;

                    String muridID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(MuridDatabaseRef);
                    geoFire.setLocation(muridID, new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()));

                    MuridPickUpLocation = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(MuridPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    PanggilGuruButton.setText("Mencari Guru yang Senggang . . .");
                    getClosetGuru();
                }
            }
        });
    }




    private void getClosetGuru()
    {
        GeoFire geoFire = new GeoFire(GuruTersediaRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(MuridPickUpLocation.latitude, MuridPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {

                if(!guruDitemukan && requestType)
                {
                    guruDitemukan = true;
                    guruDitemukanID = key;

                    GuruRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Guru-guru").child(guruDitemukanID);
                    HashMap driversMap = new HashMap();
                    driversMap.put("BelajarID", muridID);
                    GuruRef.updateChildren(driversMap);

                    GettingGuruLocation();
                    PanggilGuruButton.setText("Mendapatkan Lokasi Guru ...");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if(!guruDitemukan)
                {
                    radius = radius + 1;
                    getClosetGuru();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingGuruLocation()
    {
        LokasiGuruRefListner = LokasiGuruRef.child(guruDitemukanID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists()  &&  requestType)
                        {
                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double LocationLat = 0;
                            double LocationLng = 0;
                            PanggilGuruButton.setText("Guru Ditemukan");


                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssignedDriverInformation();


                            if(driverLocationMap.get(0) != null)
                            {
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if(driverLocationMap.get(1) != null)
                            {
                                LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            //adding marker - to pointing where driver is - using this lat lng
                            LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                            if(GuruMarker != null)
                            {
                                GuruMarker.remove();
                            }


                            Location location1 = new Location("");
                            location1.setLatitude(MuridPickUpLocation.latitude);
                            location1.setLongitude(MuridPickUpLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLng.latitude);
                            location2.setLongitude(DriverLatLng.longitude);

                            float Distance = location1.distanceTo(location2);

                            if (Distance < 90)
                            {
                                PanggilGuruButton.setText("Guru Sudah Sampai");
                            }
                            else
                            {
                                PanggilGuruButton.setText("Guru Ditemukan");
                            }

                            GuruMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Gurumu di sini").icon(BitmapDescriptorFactory.fromResource(R.drawable.guruotw)));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
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

    @Override
    public void onLocationChanged(Location location)
    {
        //getting the updated location
        LastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
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
    }


    public void LogOutUser()
    {
        Intent startPageIntent = new Intent(MuridMapsActivity.this, Splash.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }



    private void getAssignedDriverInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guru-guru").child(guruDitemukanID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String spesialis = dataSnapshot.child("spesialis").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtSpesialisGuru.setText(spesialis);

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

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent reg = new Intent(this, DashboardMuridActivity.class);
        startActivity(reg);
        finish();
    }
}
