package com.nellymincheva.indoorpositioningsystem;

import java.util.Map;

public class PositionRecord{
    public Map<String,Integer> records;
    public int x, y;

    public PositionRecord(int x, int y, Map<String,Integer> records){
        this.x = x;
        this.y = y;
        this.records = records;
    }

    public void AddRecord(String beaconAdress, int rssi){
        this.records.put(beaconAdress,rssi);
    }

    public void AddRecords(Map<String,Integer> records){
        this.records.putAll(records);
    }

    public void ClearRecords(){
        this.records.clear();
    }
}
