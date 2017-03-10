package com.nellymincheva.indoorpositioningsystem;

import android.util.ArrayMap;

import java.io.Serializable;
import java.util.Map;

public class PositionRecord implements Serializable{
    public Map<String,Double> records;
    public int x;
    public int y;

    public PositionRecord(int x, int y, Map<String,Double> records){
        this.x = x;
        this.y = y;
        this.records = records;
    }
    public PositionRecord(int x, int y){
        this.x = x;
        this.y = y;
        this.records = new ArrayMap<>();
    }

    public void AddRecord(String beaconAdress, Double rssi){
        this.records.put(beaconAdress,rssi);
    }

    public void AddRecords(Map<String,Double> records){
        this.records.putAll(records);
    }

    public void ClearRecords(){
        this.records.clear();
    }
}
