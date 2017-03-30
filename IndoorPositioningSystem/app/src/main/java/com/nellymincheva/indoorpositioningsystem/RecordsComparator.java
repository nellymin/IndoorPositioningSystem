package com.nellymincheva.indoorpositioningsystem;

import java.util.Comparator;

public class RecordsComparator implements Comparator<PositionRecord> {
    private static String mac;
    private static int signal;

    public RecordsComparator(String mac, int signal) {
        this.mac = mac;
        this.signal = signal;
    }

    public RecordsComparator compareBy(String mac, int signal) {
        this.mac = mac;
        this.signal = signal;
        return this;
    }

    @Override
    public int compare(PositionRecord o1, PositionRecord o2) {
        double o1Accuracy = Math.abs(o1.records.get(mac)-signal);
        double o2Accuracy = Math.abs(o2.records.get(mac)-signal);

        if(o1Accuracy > o2Accuracy) {
            return 1;
        }
        else if(o1Accuracy < o2Accuracy) {
            return -1;
        }
        else {
            return 0;
        }
    }
}