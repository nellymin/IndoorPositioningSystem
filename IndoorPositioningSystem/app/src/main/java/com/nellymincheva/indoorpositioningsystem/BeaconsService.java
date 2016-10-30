package com.nellymincheva.indoorpositioningsystem;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

public class BeaconsService extends Service implements BeaconConsumer {

    private BeaconManager beaconManager;

    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 1;
    static final int MSG_CHANGE_MYBEACONS = 2;
    static final int MSG_GIVE_MYBEACONS_INFORMATION = 3;
    private double distanceToBeacon1 = -1;
    private double distanceToBeacon2 = -1;
    private double distanceToBeacon3 = -1;

    SharedPreferences sharedPreferences;

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

                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
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
        sharedPreferences = getApplicationContext().getSharedPreferences("test", MODE_PRIVATE);
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

        bm.setBackgroundBetweenScanPeriod(12000);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            public int seconds = 0;

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                seconds++;
                int count = 0;
                String beacon1Mac = sharedPreferences.getString("beacon1Mac","");
                String beacon2Mac = sharedPreferences.getString("beacon2Mac","");
                String beacon3Mac = sharedPreferences.getString("beacon3Mac","");

                if (beacons.size() > 0) {
                    for(Beacon beacon: beacons){
                        //myBeaconsManager.AddMyBeacon(beacon,beacon.getDistance());
                        count ++;
                        if(beacon.getBluetoothAddress().equals(beacon1Mac)){
                            distanceToBeacon1 = (beacon.getDistance()*100)/100;
                        }
                        if(beacon.getBluetoothAddress().equals(beacon2Mac)){
                            distanceToBeacon2 = (beacon.getDistance()*100)/100;
                        }
                        if(beacon.getBluetoothAddress().equals(beacon3Mac)){
                            distanceToBeacon3 = (beacon.getDistance()*100)/100;
                        }
                        Log.wtf(TAG, count +"BEACON #" + beacon.getBluetoothAddress() + " is " + (beacon.getDistance()*100)/100 + " meters away");
                        Toast.makeText(getApplicationContext(),beacon.getBluetoothAddress() + " is " + (beacon.getDistance()*100)/100 + " meters away" , Toast.LENGTH_LONG).show();
                    }

                    Log.wtf(TAG, "-----------------");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

}
