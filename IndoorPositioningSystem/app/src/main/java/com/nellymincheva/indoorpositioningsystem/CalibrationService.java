package com.nellymincheva.indoorpositioningsystem;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CalibrationService extends Service implements BeaconConsumer {

    private BeaconManager beaconManager;

    boolean scanNearbyBeacons = false;
    Map<String, Double> nearbyBeacons = new HashMap<>();
    long nearbyStarted;

    boolean calibratingPosition = false;
    Map<String,Integer> positionRecords;
    Map<String,Integer> addressRepeated;
    long calibrationStarted;

    private int calibrationPeriod = 5000, scanNearbyPeriod = 5000;

    static final int MSG_SCAN_NEARBY = 1;
    static final int MSG_CALIBRATE_POSITION = 2;

    public static final String
            ACTION_NEARBY_BEACONS_BROADCAST = CalibrationService.class.getName() + "BeaconsBroadcast",
            EXTRA_NEARBY_MAP = "extra_nearby_map" ,
    ACTION_CALIBRATE_POSITION_BROADCAST = CalibrationService.class.getName() + "CalibratePositionBroadcast",
    EXTRA_CALIBRATION_MAP = "extra_calibration_map";


    private int scanPeriod;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCAN_NEARBY:
                    nearbyBeacons = new HashMap<>();
                    scanNearbyBeacons = true;
                    nearbyStarted = System.currentTimeMillis( );
                case MSG_CALIBRATE_POSITION:
                    positionRecords = new HashMap<>();
                    addressRepeated = new HashMap<>();
                    calibratingPosition = true;
                    calibrationStarted = System.currentTimeMillis();

                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Started positioning", Toast.LENGTH_SHORT).show();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        SetupBeaconManager(beaconManager);
        beaconManager.bind(this);

        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        beaconManager.unbind(this);
    }

    private void SetupBeaconManager(BeaconManager bm) {

        bm.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        bm.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        bm.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        bm.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));
        bm.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        scanPeriod = 10;
        bm.setBackgroundBetweenScanPeriod(scanPeriod);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons.size() > 0) {
                    long timeNow = System.currentTimeMillis( );
                    if(calibratingPosition){
                        if(timeNow - calibrationStarted > 5000) {
                            sendBroadcastMessage("CalibratePosition");
                            calibratingPosition = false;
                        }
                        else
                        for(Beacon beacon: beacons){
                            String address = beacon.getBluetoothAddress();

                            Log.wtf(TAG,"shit2");
                            if(positionRecords.containsKey(address)){
                                int repeated = addressRepeated.get(address);
                                positionRecords.put(address, (repeated * positionRecords.get(address) + beacon.getRssi())/ (repeated+1));
                                addressRepeated.put(address, repeated+1);
                            }
                            else{
                                positionRecords.put(address, beacon.getRssi());
                                addressRepeated.put(address, 1);
                            }
                        }
                    }
                    else if(scanNearbyBeacons){
                        if(timeNow - nearbyStarted > 5000) {
                            sendBroadcastMessage("NearbyBeacons");
                            scanNearbyBeacons = false;
                        }
                        else
                        for(Beacon beacon: beacons){

                            String address = beacon.getBluetoothAddress();
                            if(nearbyBeacons.containsKey(address)){
                                double distance = Math.min(beacon.getDistance(),nearbyBeacons.get(address));
                                nearbyBeacons.put(address,distance);
                            }
                            else {
                                nearbyBeacons.put(address, beacon.getDistance());

                            }
                        }
                    }

                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("rangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private void sendBroadcastMessage(String action) {
        Intent intent = new Intent();
        switch(action){
            case "NearbyBeacons":
                intent = new Intent(ACTION_NEARBY_BEACONS_BROADCAST);
                intent.putExtra(EXTRA_NEARBY_MAP, (Serializable) nearbyBeacons);
                intent.putExtra("action", "NearbyBeacons");
                return;
            case "CalibratePosition":
                Log.wtf(TAG,"shit");
                intent = new Intent(ACTION_CALIBRATE_POSITION_BROADCAST);
                intent.putExtra(EXTRA_CALIBRATION_MAP, (Serializable) positionRecords);
                intent.putExtra("action", "CalibratePosition");
                return;
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
