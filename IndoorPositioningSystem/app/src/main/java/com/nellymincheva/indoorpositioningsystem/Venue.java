package com.nellymincheva.indoorpositioningsystem;


import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Venue {
    public String name;
    public double width, height;
    public List<PositionRecord> calibrationData;
    public String[] beacons;
    public double gridSize;
    public int maxX, maxY;


    public Venue(double width, double height, String name){
        this.name = name;
        this.width = width;
        this.height = height;
        calibrationData = new ArrayList<>();

    }

    public void SetGridSize(double gridSize){
        this.gridSize = gridSize;
        this.maxX = (int)Math.round(this.width/gridSize);
        this.maxY = (int)Math.round(this.height/gridSize);
        if(calibrationData != null)
            this.calibrationData.clear();

    }

    public void AddCalibrationData(PositionRecord record){
        if(gridSize <= 0)
            return;
        if(record.x > this.maxX || record.y > this.maxY) {
            return;
        }
        calibrationData.add(record);
    }

    public void SetDimentions(int width, int height){
        this.width = width;
        this.height = height;

        if(gridSize>0){
            this.maxX = (int)Math.ceil(this.width/gridSize);
            this.maxY = (int)Math.ceil(this.height/gridSize);
            if(calibrationData != null)
                this.calibrationData.clear();
        }
    }

    public Pair<Integer,Integer> findPosition (Map<String,Integer> records){
        int positionX = -1, positionY = -1;
        double minimalEucledeanDistance = -1 ,eucledeanDistance;
        for(int x = 0; x < maxX; x++){
            for(int y = 0; y < maxY; y++){
                for(PositionRecord pr : this.calibrationData) {
                    if(pr != null && pr.x == x && pr.y == y) {
                        boolean hasRecords = false;
                        eucledeanDistance = 0;
                        for(Map.Entry<String, Integer> entry : records.entrySet()){
                            if(pr.records.containsKey(entry.getKey())){
                                hasRecords = true;
                                eucledeanDistance += (pr.records.get(entry.getKey()) - entry.getValue()) * (pr.records.get(entry.getKey()) - entry.getValue());
                            }
                        }
                        eucledeanDistance = Math.sqrt(eucledeanDistance);
                        if(eucledeanDistance < minimalEucledeanDistance && hasRecords){
                            minimalEucledeanDistance = eucledeanDistance;
                            positionX = x;
                            positionY = y;
                        }
                    }
                }
            }
        }
        Pair<Integer, Integer> position = new Pair<>(positionX, positionY);
        return position;
    }

    public void SetBeacons(String[] beacons){
        this.beacons = beacons;
    }

    public List<PositionRecord> GetCalibrationData(){
        return this.calibrationData;
    }

}

