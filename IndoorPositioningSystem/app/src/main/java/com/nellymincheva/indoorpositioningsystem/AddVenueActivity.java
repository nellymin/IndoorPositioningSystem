package com.nellymincheva.indoorpositioningsystem;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class AddVenueActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private Venue newVenue;
    Messenger mCalibrationService = null;
    boolean mCalibrationServiceBound;
    public boolean notReady = false;

    public int calibrationX, calibrationY;


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
        Button calibrateBtn = (Button) findViewById(R.id.calibrateButton);
        calibrateBtn.setOnClickListener(this);


        registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        HashMap<String, Double> nearbyBeacons = (HashMap<String, Double>) intent.getSerializableExtra(CalibrationService.EXTRA_NEARBY_MAP);
                        for (Map.Entry<String, Double> nearbyBeacon : nearbyBeacons.entrySet()) {
                            String beaconAddress = nearbyBeacon.getKey();
                            Double distance = nearbyBeacon.getValue();
                            Log.wtf(TAG, beaconAddress + " " + distance);
                        }
                    }
                }, new IntentFilter(CalibrationService.ACTION_NEARBY_BEACONS_BROADCAST)
        );

        registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        HashMap<String, Double> positionRecord = (HashMap<String, Double>) intent.getSerializableExtra(CalibrationService.EXTRA_CALIBRATION_MAP);
                        Map<String, Double> records = new HashMap<String, Double>();
                        for (Map.Entry<String, Double> positionRec : positionRecord.entrySet()) {
                            String beaconAddress = positionRec.getKey();
                            Double rssi = positionRec.getValue();
                            records.put(beaconAddress, rssi);
                            Log.wtf("zdr", beaconAddress + " " + rssi);
                        }
                        PositionRecord pr = new PositionRecord(calibrationX, calibrationY, records);
                        newVenue.AddCalibrationData(pr);
                        Log.wtf("zdr", calibrationX+" x");
                        Log.wtf("zdr", calibrationY+" y");
                        Log.wtf("zdr", newVenue.maxX+" max x");
                        Log.wtf("zdr", newVenue.maxY+" max y");
                        GridView gv = (GridView) findViewById(R.id.grid_view2);
                        gv.changeCell(calibrationX,calibrationY);
                        if(calibrationX + 1 >= newVenue.maxX){
                            calibrationX = 0;
                            if(calibrationY + 1 < newVenue.maxY){
                                calibrationY++;
                                calibratePosition();
                            }
                            else{
                                finishCalibration();
                            }

                        }
                        else{
                            calibrationX++;
                            calibratePosition();
                        }

                    }
                }, new IntentFilter(CalibrationService.ACTION_CALIBRATE_POSITION_BROADCAST)
        );


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service
        bindService(new Intent(this, CalibrationService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null)
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Cannot create venue");
                    builder.setMessage("Since location access has not been granted, Bekon will not be able to detect beacons, therefore create a new venue");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void AddVenueWithSizing(double width, double height, String name) {

        if (mUser == null)
            return;
        newVenue = new Venue(width, height, name);
        LinearLayout sizing = (LinearLayout) findViewById(R.id.sizing);
        sizing.setVisibility(View.INVISIBLE);
        LinearLayout grid = (LinearLayout) findViewById(R.id.grid);
        grid.setVisibility(View.VISIBLE);
        newVenue.SetGridSize(1);
        Map<String, Integer> rec = new HashMap<>();
        newVenue.userId = mUserId;
        //mDatabase.child("venues").push().setValue(newVenue);

    }

    public void setGrid(){
        TextView gridSizeText = (TextView) findViewById(R.id.grid_size);
        double gridSize = Double.parseDouble(gridSizeText.getText().toString());
        newVenue.SetGridSize(gridSize);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setNumColumns(newVenue.maxX);
        gridView.setNumRows(newVenue.maxY);

        LinearLayout grid = (LinearLayout) findViewById(R.id.grid);
        grid.setVisibility(View.INVISIBLE);
        LinearLayout calibrate = (LinearLayout) findViewById(R.id.calibrate);
        calibrate.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addVenueWithDimentions:
                TextView name = (TextView) findViewById(R.id.venue_name);
                TextView widthText = (TextView) findViewById(R.id.room_width);
                TextView heightText = (TextView) findViewById(R.id.room_height);
                if (widthText.getText().toString().matches("") || heightText.getText().toString().matches("") || name.getText().toString().matches("")) {
                    Toast.makeText(this, "Please enter values", Toast.LENGTH_LONG).show();
                    break;
                }
                double width = Double.parseDouble(widthText.getText().toString());
                double height = Double.parseDouble(heightText.getText().toString());
                if (width <= 0 || height <= 0) {
                    Toast.makeText(this, "Width and height must have positive values", Toast.LENGTH_LONG).show();
                    break;
                }
                AddVenueWithSizing(width, height, name.getText().toString());
                break;
            case R.id.previewButton:
                TextView gridSizeText = (TextView) findViewById(R.id.grid_size);
                if(gridSizeText==null)
                    break;
                double gridSize = Double.parseDouble(gridSizeText.getText().toString());
                if (gridSize <= 0 || gridSize > newVenue.width || gridSize > newVenue.height) {
                    Toast.makeText(this, "Grid size must have positive value, smaller than width and height", Toast.LENGTH_LONG).show();
                    break;
                }
                newVenue.SetGridSize(gridSize);
                GridView gridView = (GridView) findViewById(R.id.grid_view);
                gridView.setNumColumns(newVenue.maxX);
                gridView.setNumRows(newVenue.maxY);

                break;
            case R.id.setGridSize:
                gridSizeText = (TextView) findViewById(R.id.grid_size);
                if(gridSizeText.length()==0){
                    Toast.makeText(this, "Fill in grid size", Toast.LENGTH_LONG).show();
                    break;
                }
                gridSize = Double.parseDouble(gridSizeText.getText().toString());
                if (gridSize <= 0 || gridSize > newVenue.width || gridSize > newVenue.height) {
                    Toast.makeText(this, "Grid size must have positive value, smaller than width and height", Toast.LENGTH_LONG).show();
                    break;
                }
                newVenue.SetGridSize(gridSize);
                GridView gridView2 = (GridView) findViewById(R.id.grid_view2);
                gridView2.setNumColumns(newVenue.maxX);
                gridView2.setNumRows(newVenue.maxY);
                setGrid();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Android M Permission check
                    if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Bekon needs location access");
                        builder.setMessage("Please grant location access so Bekon can create new venue");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        PERMISSION_REQUEST_COARSE_LOCATION);
                            }

                        });
                        builder.show();
                    }
                }
                break;

            case R.id.calibrateButton:
                Button cal = (Button) findViewById(R.id.calibrateButton);
                cal.setEnabled(false);
                newVenue.beacons = new ArrayList<>();
                newVenue.beacons.add("DD:12:B2:90:39:48");
                newVenue.beacons.add("C9:35:A9:B1:84:9D");
                newVenue.beacons.add("E0:62:12:B9:F3:BE");
                newVenue.beacons.add("EE:86:9C:E0:19:F9");
                calibrationX = 0;
                calibrationY = 0;

                calibratePosition();
                break;
            default:
                break;

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
    public int n = 0;
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

    public void finishCalibration(){

        mDatabase.child("venues").push().setValue(newVenue);

    }

    private void ShowAddVenue() {

    }

}
