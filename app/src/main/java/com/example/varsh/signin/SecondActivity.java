package com.example.varsh.signin;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SecondActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{

//    private ArrayList<String> permissionsToRequest;
//    private ArrayList<String> permissionsRejected = new ArrayList<>();
//    private ArrayList<String> permissions = new ArrayList<>();
//    private final static int ALL_PERMISSIONS_RESULT = 101;


    Button signOutBtn, mapButton;
    TextView textView;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;
    DatabaseReference mDatabase;
    LatLng myLocation;
    LocationTrack locationTrack;
    ValueEventListener dbListener;
    ArrayList<Loc> userList;
//    ArrayList<String> paired;
    MyRecyclerViewAdapter adapter;
    Date d;
    SimpleDateFormat sdf;
    ProgressBar progressBar;




    class Loc{
        double latitude;
        double longitude;
        String name;
        String email;
        String time;
        public Loc(Double latitude, Double longitude, String name, String email, String time){
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.email = email;
            this.time = time;
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        signOutBtn = findViewById(R.id.signOutBtn);
        mapButton = findViewById(R.id.button2);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        locationTrack = new LocationTrack(SecondActivity.this);
        userList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, userList);
        adapter.setClickListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);


        myLocation = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
        if (locationTrack.canGetLocation()) {
            if (!(locationTrack.getLatitude() == 0 || locationTrack.getLongitude() == 0)) {
                myLocation = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
                mDatabase.child(user.getUid()).child("latitude").setValue(myLocation.latitude);
                mDatabase.child(user.getUid()).child("longitude").setValue(myLocation.longitude);
                mDatabase.child(user.getUid()).child("name").setValue(user.getDisplayName());
                d = new Date();
                sdf = new SimpleDateFormat("dd-MM-yy   HH:mm:ss");
                mDatabase.child(user.getUid()).child("time").setValue(sdf.format(d));
                mDatabase.child(user.getUid()).child("email").setValue(user.getEmail());
            }
            else
                Toast.makeText(SecondActivity.this,"Getting location",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(SecondActivity.this,"Cant get location",Toast.LENGTH_SHORT).show();
        //locationTrack.showSettingsAlert();
//        paired = new ArrayList<>();
//        paired.add("yogeshj939@gmail.com");
        dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    Double lat = userSnapShot.child("latitude").getValue(Double.class);
                    Double lon = userSnapShot.child("longitude").getValue(Double.class);
                    String name = userSnapShot.child("name").getValue(String.class);
                    String email = userSnapShot.child("email").getValue(String.class);
                    String time = userSnapShot.child("time").getValue(String.class);
//                    for(String s: paired)
//                        if(s.equals(email)){
                        int f = 0;
                        for (Loc u : userList) {
                            if (u.email == email) {
                                u.latitude = lat;
                                u.longitude = lon;
                                u.time = time;
                                f = 1;
                                break;
                            }
                        }

                        if (f == 0) {
                            Loc user = new Loc(lat, lon, name, email,time);
                            userList.add(user);
                        }
//                    }





                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(dbListener);



//******************************************************************************
//        permissions.add(ACCESS_FINE_LOCATION);
//        permissions.add(ACCESS_COARSE_LOCATION);
//        permissionsToRequest = findUnAskedPermissions(permissions);
//        //get the permissions we have asked for before but are not granted..
//        //we will store this in a global list to access later.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (permissionsToRequest.size() > 0)
//                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
//        }




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(SecondActivity.this, MainActivity.class));

                }

            }
        };



        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationTrack.canGetLocation()) {
                    if (!(locationTrack.getLatitude() == 0 || locationTrack.getLongitude() == 0)) {
                        myLocation = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
                        Bundle args = new Bundle();
                        args.putParcelable("my_loc", myLocation);
                        args.putString("name","You are here");
                        Intent i = new Intent(SecondActivity.this, MapsActivity.class);
                        i.putExtra("bundle", args);
                        startActivity(i);
                        mDatabase.child(user.getUid()).child("latitude").setValue(myLocation.latitude);
                        mDatabase.child(user.getUid()).child("longitude").setValue(myLocation.longitude);
                        mDatabase.child(user.getUid()).child("name").setValue(user.getDisplayName());
                        d = new Date();
                        sdf = new SimpleDateFormat("dd-MM-yy  HH:mm:ss");
                        mDatabase.child(user.getUid()).child("time").setValue(sdf.format(d));
                        mDatabase.child(user.getUid()).child("email").setValue(user.getEmail());

                        //mDatabase.child(user.getUid()).child("paired").setValue(paired);
                        //mDatabase.child(user.getUid()).setValue(user.getDisplayName());
//                        String s = " ";
//                        for(Loc l: userList)
//                            s = s+l.name+"\n";
//                        textView.setText(s);

                    }
                    else
                        Toast.makeText(SecondActivity.this,"Getting location",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(SecondActivity.this,"Cant location",Toast.LENGTH_SHORT).show();
                    //locationTrack.showSettingsAlert();

            }
        });



    }

    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

    @Override
    public void onItemClick(View view, int position) {
        Loc p = userList.get(position);
        LatLng myLocation = new LatLng(p.latitude,p.longitude);
        Bundle args = new Bundle();
        args.putParcelable("my_loc", myLocation);
        args.putString("name",p.name);
        Intent i = new Intent(SecondActivity.this, MapsActivity.class);
        i.putExtra("bundle", args);
        startActivity(i);
    }



//************************************************************************
//    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
//        ArrayList result = new ArrayList();
//
//        for (String perm : wanted) {
//            if (!hasPermission(perm)) {
//                result.add(perm);
//            }
//        }
//
//        return result;
//    }
//
//    private boolean hasPermission(String permission) {
//        if (canMakeSmores()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
//            }
//        }
//        return true;
//    }
//
//    private boolean canMakeSmores() {
//        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
//    }
//
//
//    @TargetApi(Build.VERSION_CODES.M)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//
//        switch (requestCode) {
//
//            case ALL_PERMISSIONS_RESULT:
//                for (String perms : permissionsToRequest) {
//                    if (!hasPermission(perms)) {
//                        permissionsRejected.add(perms);
//                    }
//                }
//
//                if (permissionsRejected.size() > 0) {
//
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
//                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
//                                            }
//                                        }
//                                    });
//                            return;
//                        }
//                    }
//
//                }
//
//                break;
//        }
//
//    }
//
//    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
//        new AlertDialog.Builder(SecondActivity.this)
//                .setMessage(message)
//                .setPositiveButton("OK", okListener)
//                .setNegativeButton("Cancel", null)
//                .create()
//                .show();
//    }
//
}
