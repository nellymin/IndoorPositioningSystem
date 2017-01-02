package com.nellymincheva.indoorpositioningsystem;

public class Record{
    public int rssi;
    public int i,j;

    public Record(int i, int j, int rssi){
        this.rssi = rssi;
        this.i = i;
        this.j = j;
    }
}

