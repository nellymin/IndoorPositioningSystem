package com.nellymincheva.indoorpositioningsystem;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Map;

public class PositioningService extends Service implements BeaconConsumer {

    public static Venue venue;
    private BeaconManager beaconManager;
    Map<String,Integer> positionRecords;


    public static final String
            ACTION_USER_POSITION_BROADCAST = PositioningService.class.getName() + "LocationBroadcast",
            EXTRA_USER_POSITION_X = "user_position_x",
            EXTRA_USER_POSITION_Y = "user_position_y";


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
        venue = (Venue) new Gson().fromJson(intent.getStringExtra("venue"), Venue.class);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        SetupBeaconManager(beaconManager);
        positionRecords = new ArrayMap<>();
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

                if (beacons.size() > 0 && venue != null) {
                    for(Beacon beacon: beacons){
                        Log.wtf("zdr", " "+beacon.getBluetoothAddress());
                        for(String b : venue.beacons){
                            if(b.equals(beacon.getBluetoothAddress()))
                                positionRecords.put(beacon.getBluetoothAddress(),beacon.getRssi());
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


        Pair<Integer,Integer> position = venue.findPosition(positionRecords);

            double userPositionY = position.first;
            double userPositionX = position.second;
        Log.wtf("zdrr", " TUKA " + userPositionX);
        Log.wtf("zdrr", " I TAM " + userPositionY);
        Log.wtf("zdrr", " SILATA " + positionRecords.get("EE:86:9C:E0:19:F9"));
            Intent intent = new Intent(ACTION_USER_POSITION_BROADCAST);
            intent.putExtra(EXTRA_USER_POSITION_X, userPositionX);
            intent.putExtra(EXTRA_USER_POSITION_Y, userPositionY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
