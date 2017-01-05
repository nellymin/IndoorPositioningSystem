package com.nellymincheva.indoorpositioningsystem;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class AddVenueActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    private Venue newVenue;
    Messenger mCalibrationService = null;
    boolean mCalibrationServiceBound;


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mCalibrationService = new Messenger(service);
            mCalibrationServiceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mCalibrationServiceBound = false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_venue);
        Button addVenueBtn = (Button) findViewById(R.id.addVenueWithDimentions);
        addVenueBtn.setOnClickListener(this);
        Button setGridBtn = (Button) findViewById(R.id.setGridSize);
        setGridBtn.setOnClickListener(this);
        Button previewBtn = (Button) findViewById(R.id.previewButton);
        previewBtn.setOnClickListener(this);



        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String opa = intent.getStringExtra("action");

                        Log.wtf(TAG, opa);
                        switch (intent.getStringExtra("action")){
                            case "NearbyBeacons":

                                HashMap<String, Double> nearbyBeacons = (HashMap<String, Double>)intent.getSerializableExtra(CalibrationService.EXTRA_NEARBY_MAP);
                                for(Map.Entry<String, Double> nearbyBeacon : nearbyBeacons.entrySet()) {
                                    String beaconAdress = nearbyBeacon.getKey();
                                    Double distance = nearbyBeacon.getValue();
                                    Log.wtf(TAG, beaconAdress + " " + distance);
                                }
                                return;
                            case "CalibratePosition":

                                Log.wtf(TAG,"shit33");
                                HashMap<String, Double> positionRecord = (HashMap<String, Double>)intent.getSerializableExtra(CalibrationService.EXTRA_CALIBRATION_MAP);
                                for(Map.Entry<String, Double> positionRec : positionRecord.entrySet()) {

                                    Log.wtf(TAG,"shit4");
                                    String beaconAdress = positionRec.getKey();
                                    Double distance = positionRec.getValue();
                                    Log.wtf(TAG, beaconAdress + " " + distance);
                                }
                                return;
                        }
                    }
                }, new IntentFilter(CalibrationService.ACTION_NEARBY_BEACONS_BROADCAST)
        );



    }

    @Override
    protected void onStart(){
        super.onStart();

        // Bind to the service
        bindService(new Intent(this, CalibrationService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth != null)
            mUser = mAuth.getCurrentUser();

        mUserId = mUser.getUid();
        ShowAddVenue();



    }


    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mCalibrationServiceBound) {
            unbindService(mConnection);
            mCalibrationServiceBound = false;
        }
    }


    private void AddVenueWithSizing(double width, double height, String name){

        if(mUser == null)
            return;
        newVenue = new Venue(width, height, name);
        newVenue.SetGridSize(1);
        Map<String, Integer> rec = new HashMap<>();
        rec.put("", -12);
        PositionRecord pr = new PositionRecord(0, 0, null);
        newVenue.AddCalibrationData(pr);
        pr = new PositionRecord(0, 1, null);
        newVenue.AddCalibrationData(pr);
        mDatabase.child("venues").child(mUserId).push().setValue(newVenue);
        calibratePosition();
        //getNearbyBeacons();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addVenueWithDimentions:
                TextView name = (TextView) findViewById(R.id.venue_name);
                TextView widthText = (TextView) findViewById(R.id.room_width);
                TextView heightText = (TextView) findViewById(R.id.room_height);
                if(widthText.getText().toString().matches("") || heightText.getText().toString().matches("") || name.getText().toString().matches("")){
                    Toast.makeText(this, "Please enter values", Toast.LENGTH_LONG).show();
                    return;
                }
                double width = Double.parseDouble(widthText.getText().toString());
                double height = Double.parseDouble(heightText.getText().toString());
                if(width<= 0 || height<=0){
                    Toast.makeText(this, "Width and height must have positive values", Toast.LENGTH_LONG).show();
                    return;
                }
                AddVenueWithSizing(width, height, name.getText().toString());
                HideAddVenue();
                ShowSetGrid();
                return;
            case R.id.previewButton:
                TextView gridSizeText = (TextView) findViewById(R.id.grid_size);
                double gridSize = Double.parseDouble(gridSizeText.getText().toString());
                if(gridSize<=0 || gridSize>newVenue.width || gridSize>newVenue.height){
                    Toast.makeText(this, "Grid size must have positive value, smaller than width and height", Toast.LENGTH_LONG).show();
                    return;
                }
                newVenue.SetGridSize(gridSize);
                GridView grid = (GridView) findViewById(R.id.grid_view);
                grid.setNumColumns(newVenue.maxX);
                grid.setNumRows(newVenue.maxY);
                return;
            case R.id.setGridSize:
                gridSizeText = (TextView) findViewById(R.id.grid_size);
                gridSize = Double.parseDouble(gridSizeText.getText().toString());
                if(gridSize<=0 || gridSize>newVenue.width || gridSize>newVenue.height){
                    Toast.makeText(this, "Grid size must have positive value, smaller than width and height", Toast.LENGTH_LONG).show();
                    return;
                }
                newVenue.SetGridSize(gridSize);
                return;
        }
    }
    public void getNearbyBeacons() {
        if (!mCalibrationServiceBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, CalibrationService.MSG_SCAN_NEARBY, 0, 0);
        try {
            mCalibrationService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void calibratePosition() {
        if (!mCalibrationServiceBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, CalibrationService.MSG_CALIBRATE_POSITION, 0, 0);
        try {
            mCalibrationService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void ShowAddVenue() {

    }
    private void HideAddVenue() {
    }
    private void ShowSetGrid() {
    }
    private void HideSetGrid() {
    }
    private void ShowSetMap() {
    }
    private void HideSetMap() {
    }
    private void ShowAddBeacons() {
    }
    private void HideAddBeacons() {
    }
    private void ShowCalibrate() {
    }
    private void HideCalibrate() {
    }
}
