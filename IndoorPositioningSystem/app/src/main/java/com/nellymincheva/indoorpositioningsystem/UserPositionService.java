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
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import static org.altbeacon.beacon.service.scanner.ScanFilterUtils.TAG;

public class UserPositionService extends Service implements BeaconConsumer {

    private BeaconManager beaconManager;

    private String beacon1Mac, beacon2Mac, beacon3Mac;
    private double beacon1X, beacon1Y, beacon2X, beacon2Y, beacon3X, beacon3Y;

    static final int MSG_SAY_HELLO = 1;
    static final int MSG_CHANGE_MYBEACONS = 2;
    static final int MSG_GIVE_MYBEACONS_INFORMATION = 3;

    public static final String
            ACTION_USER_POSITION_BROADCAST = UserPositionService.class.getName() + "LocationBroadcast",
            EXTRA_USER_POSITION_X = "extra_user_x",
            EXTRA_USER_POSITION_Y = "extra_user_y",
            EXTRA_DISTANCE_1 = "extra_distance_1",
            EXTRA_SIGNAL_1 = "extra_signal_1",
            EXTRA_DISTANCE_2 = "extra_distance_2",
            EXTRA_SIGNAL_2 = "extra_signal_2",
            EXTRA_DISTANCE_3 = "extra_distance_3",
            EXTRA_SIGNAL_3 = "extra_signal_3";


    private double distanceToBeacon1 = -1;
    private double distanceToBeacon2 = -1;
    private double distanceToBeacon3 = -1;
    private double signalBeacon1 = -1;
    private double signalBeacon2 = -1;
    private double signalBeacon3 = -1;

    SharedPreferences sharedPreferences;
    private int scanPeriod;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHANGE_MYBEACONS:

                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SAY_HELLO:

                    Toast.makeText(getApplicationContext(), Math.round(distanceToBeacon1 * 100.0) / 100.0+"; "+Math.round(distanceToBeacon2 * 100.0) / 100.0+"; "+Math.round(distanceToBeacon3 * 100.0) / 100.0 , Toast.LENGTH_SHORT).show();
                    break;
                case MSG_GIVE_MYBEACONS_INFORMATION:
                    Toast.makeText(getApplicationContext(), "opa",Toast.LENGTH_LONG).show();
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
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();

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

        beaconManager.bind(this);

        return mMessenger.getBinder();
    }

    private void SetupBeaconManager(BeaconManager bm) {

        //bm.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        //RunningAverageRssiFilter.setSampleExpirationMilliseconds(10000l);
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

        scanPeriod = 50;
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
                        //myBeaconsManager.AddMyBeacon(beacon,beacon.getDistance());
                        count ++;
                        if(beacon.getBluetoothAddress().equals(beacon1Mac)){
                            distanceToBeacon1 = (beacon.getDistance()*100)/100;
                            signalBeacon1 = beacon.getRssi();
                            Log.wtf(TAG, "1 is " + (beacon.getDistance()*100)/100 + " away");

                        }
                        else if(beacon.getBluetoothAddress().equals(beacon2Mac)){
                            distanceToBeacon2 = (beacon.getDistance()*100)/100;
                            signalBeacon2 = beacon.getRssi();
                            Log.wtf(TAG, "2 is " + (beacon.getDistance()*100)/100 + " away");
                        }
                        else if(beacon.getBluetoothAddress().equals(beacon3Mac)){
                            distanceToBeacon3 = (beacon.getDistance()*100)/100;
                            signalBeacon3 = beacon.getRssi();
                            Log.wtf(TAG, "3 is " + (beacon.getDistance()*100)/100 + " away");
                        }
                        else{
                            Log.wtf(TAG, "4 is " + (beacon.getDistance()*100)/100 + " away");
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

    private void sendBroadcastMessage() {
        if (distanceToBeacon1!=-1 && distanceToBeacon2!=-1 && distanceToBeacon3!=-1) {

            double S = (square(beacon3X) - square(beacon2X) + square(beacon3Y) - square(beacon2Y) + square(distanceToBeacon2) - square(distanceToBeacon3)) / 2.0;
            double T = (square(beacon1X) - square(beacon2X) + square(beacon1Y) - square(beacon2Y) + square(distanceToBeacon2) - square(distanceToBeacon1)) / 2.0;
            double userPositionY = ((T * (beacon2X - beacon3X)) - (S * (beacon2X-beacon1X))) / (((beacon1Y - beacon2Y) * (beacon2X - beacon3X)) - ((beacon3Y - beacon2Y) * (beacon2X-beacon1X)));
            double userPositionX = ((userPositionY * (beacon1Y - beacon2Y)) - T) / (beacon2X-beacon1X);


            Intent intent = new Intent(ACTION_USER_POSITION_BROADCAST);
            intent.putExtra(EXTRA_DISTANCE_1, distanceToBeacon1);
            intent.putExtra(EXTRA_SIGNAL_1, signalBeacon1);
            intent.putExtra(EXTRA_DISTANCE_2, distanceToBeacon2);
            intent.putExtra(EXTRA_SIGNAL_2, signalBeacon2);
            intent.putExtra(EXTRA_DISTANCE_3, distanceToBeacon3);
            intent.putExtra(EXTRA_SIGNAL_3, signalBeacon3);
            intent.putExtra(EXTRA_USER_POSITION_X, userPositionX);
            intent.putExtra(EXTRA_USER_POSITION_Y, userPositionY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    double square(double n){
        return n*n;
    }
}
