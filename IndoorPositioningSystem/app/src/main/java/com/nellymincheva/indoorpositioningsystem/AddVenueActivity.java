package com.nellymincheva.indoorpositioningsystem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AddVenueActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_venue);
        Button btn = (Button) findViewById(R.id.addVenueWithDimentions);
        btn.setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserId = mUser.getUid();

    }

    private void AddVenueWithSizing(double width, double height){
        if(mUser.isAnonymous())
            return;
        Venue newVenue = new Venue(width, height);
        newVenue.SetGridSize(1);
        List<Record> lr = new ArrayList<>();
        lr.add(new Record(0, 0, -23));
        lr.add(new Record(0, 0, -63));
        lr.add(new Record(0, 1, -28));
        BeaconRecord br = new BeaconRecord("wtf", lr);
        newVenue.AddCalibrationData(0,0,br);
        mDatabase.child("venues").child(mUserId).setValue(newVenue);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addVenueWithDimentions:
                AddVenueWithSizing(1, 2);
                return;
        }
    }
}
