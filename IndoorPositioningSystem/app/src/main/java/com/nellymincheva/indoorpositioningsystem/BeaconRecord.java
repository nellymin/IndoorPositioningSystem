package com.nellymincheva.indoorpositioningsystem;

import java.util.List;

public class BeaconRecord{
    public List<Record> records;
    public String beaconMac;

    public BeaconRecord(String beaconMac, List<Record> records){
        this.beaconMac = beaconMac;
        this.records = records;
    }

    public void AddRecord(Record r){
        records.add(r);
    }

    public void AddRecords(List<Record> r){
        records.addAll(r);
    }

    public void ClearRecords(){
        this.records.clear();
    }
}
