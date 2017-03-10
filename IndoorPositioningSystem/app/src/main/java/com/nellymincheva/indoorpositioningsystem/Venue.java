package com.nellymincheva.indoorpositioningsystem;


import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Venue extends Object implements Serializable{
    public String name;
    public double width, height;
    public List<PositionRecord> calibrationData;
    public String[] beacons;
    public double gridSize;
    public int maxX, maxY;
    public String userId;
    public String venueId;


    public Venue(double width, double height, String name){
        this.name = name;
        this.width = width;
        this.height = height;
        calibrationData = new ArrayList<>();

    }
    public Venue(double width, double height, String name, String userId){
        this.name = name;
        this.width = width;
        this.height = height;
        this.userId = userId;
        calibrationData = new ArrayList<>();

    }
    public Venue(){

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
        double minimalEucledeanDistance = 999999999 ,eucledeanDistance;
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

    public double getGridSize() {
        return gridSize;
    }

    public void setGridSize(double gridSize) {
        this.gridSize = gridSize;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public List<PositionRecord> getCalibrationData() {
        return calibrationData;
    }

    public String getName() {
        return name;
    }

    public String[] getBeacons() {
        return beacons;
    }

    public void setBeacons(String[] beacons) {
        this.beacons = beacons;
    }

    public void setCalibrationData(List<PositionRecord> calibrationData) {
        this.calibrationData = calibrationData;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void setWidth(double width) {
        this.width = width;
    }

}

