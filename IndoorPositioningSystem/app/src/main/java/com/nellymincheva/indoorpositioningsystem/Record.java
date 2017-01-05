package com.nellymincheva.indoorpositioningsystem;

public class Record{
    public int rssi;
    public String beaconMac;

    public Record(int rssi, String beaconMac){
        this.rssi = rssi;
        this.beaconMac = beaconMac;
    }
}

