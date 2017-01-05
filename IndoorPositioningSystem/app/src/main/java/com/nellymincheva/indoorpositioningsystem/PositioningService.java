package com.nellymincheva.indoorpositioningsystem;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class PositioningService extends Service implements BeaconConsumer {

    private BeaconManager beaconManager;
    private Venue venue;

    private String beacon1Mac, beacon2Mac, beacon3Mac, beacon4Mac;
    private double beacon1X, beacon1Y, beacon2X, beacon2Y, beacon3X, beacon3Y, beacon4X, beacon4Y;


    public static final String
            ACTION_USER_POSITION_BROADCAST = PositioningService.class.getName() + "LocationBroadcast",
            EXTRA_USER_POSITION_X = "extra_user_x",
            EXTRA_USER_POSITION_Y = "extra_user_y",
            EXTRA_SIGNAL_1 = "extra_signal_1",
            EXTRA_SIGNAL_2 = "extra_signal_2",
            EXTRA_SIGNAL_3 = "extra_signal_3",
            EXTRA_SIGNAL_4 = "extra_signal_4";


    private double signalBeacon1 = -1;
    private double signalBeacon2 = -1;
    private double signalBeacon3 = -1;
    private double signalBeacon4 = -1;

    SharedPreferences sharedPreferences;
    private int scanPeriod;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Started positioning", Toast.LENGTH_SHORT).show();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        SetupBeaconManager(beaconManager);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        beacon1Mac = sharedPreferences.getString("beacon1Mac","");
        beacon1X = Float.parseFloat(sharedPreferences.getString("beacon1X", "0"));
        beacon1Y = Float.parseFloat(sharedPreferences.getString("beacon1Y", "0"));
        beacon2Mac = sharedPreferences.getString("beacon2Mac","");
        beacon2X = Float.parseFloat(sharedPreferences.getString("beacon2X", "0"));
        beacon2Y = Float.parseFloat(sharedPreferences.getString("beacon2Y", "0"));
        beacon3Mac = sharedPreferences.getString("beacon3Mac","");
        beacon3X = Float.parseFloat(sharedPreferences.getString("beacon3X", "0"));
        beacon3Y = Float.parseFloat(sharedPreferences.getString("beacon3Y", "0"));
        beacon4Mac = sharedPreferences.getString("beacon4Mac","");
        beacon4X = Float.parseFloat(sharedPreferences.getString("beacon4X", "0"));
        beacon4Y = Float.parseFloat(sharedPreferences.getString("beacon4Y", "0"));

        beaconManager.bind(this);

        return mMessenger.getBinder();
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
            public int seconds = 0;

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                seconds++;
                int count = 0;

                if (beacons.size() > 0) {
                    for(Beacon beacon: beacons){
                        //Log.wtf(TAG, beacon.getBluetoothAddress() + " " + beacon.getRssi());
                        //myBeaconsManager.AddMyBeacon(beacon,beacon.getDistance());
                        count ++;
                        if(beacon.getBluetoothAddress().equals(beacon1Mac)){
                            signalBeacon1 = beacon.getRssi();
                            //Log.wtf(TAG, "1 is " + (beacon.getDistance()*100)/100 + " away");
                            //Log.wtf(TAG, "" + signalBeacon1);

                        }
                        else if(beacon.getBluetoothAddress().equals(beacon2Mac)){
                            signalBeacon2 = beacon.getRssi();
                            //Log.wtf(TAG, "2 is " + (beacon.getDistance()*100)/100 + " away");
                        }
                        else if(beacon.getBluetoothAddress().equals(beacon3Mac)){
                            signalBeacon3 = beacon.getRssi();
                            //Log.wtf(TAG, "3 is " + (beacon.getDistance()*100)/100 + " away");
                        }
                        else if(beacon.getBluetoothAddress().equals(beacon4Mac)){
                            signalBeacon4 = beacon.getRssi();
                            //Log.wtf(TAG, "3 is " + (beacon.getDistance()*100)/100 + " away");
                        }
                    }
                    sendBroadcastMessage();
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    @Override
    public void onDestroy() {
        beaconManager.unbind(this);
    }

    private void sendBroadcastMessage() {
        if (signalBeacon1!=-1 && signalBeacon2!=-1 && signalBeacon3!=-1 && signalBeacon4!=-1) {
            double userPositionY = 0;
            double userPositionX = 0;

            Intent intent = new Intent(ACTION_USER_POSITION_BROADCAST);
            intent.putExtra(EXTRA_SIGNAL_1, signalBeacon1);
            intent.putExtra(EXTRA_SIGNAL_2, signalBeacon2);
            intent.putExtra(EXTRA_SIGNAL_3, signalBeacon3);
            intent.putExtra(EXTRA_SIGNAL_4, signalBeacon4);


            intent.putExtra(EXTRA_USER_POSITION_X, userPositionX);
            intent.putExtra(EXTRA_USER_POSITION_Y, userPositionY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
