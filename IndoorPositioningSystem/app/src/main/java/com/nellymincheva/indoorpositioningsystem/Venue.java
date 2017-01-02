package com.nellymincheva.indoorpositioningsystem;


import java.util.ArrayList;
import java.util.List;

public class Venue {
    public double width, height;
    public List<BeaconRecord> calibrationData;
    public String[] beacons;
    public double gridSize;
    public int maxX, maxY;


    public Venue(double width, double height){
        this.width = width;
        this.height = height;
        calibrationData = new ArrayList<>();

    }

    public void SetGridSize(double gridSize){
        this.gridSize = gridSize;
        this.maxX = (int)Math.ceil(this.width/gridSize);
        this.maxY = (int)Math.ceil(this.height/gridSize);
        if(calibrationData != null)
            this.calibrationData.clear();

    }

    public void AddCalibrationData(int i, int j, BeaconRecord br){
        if(gridSize <= 0)
            return;
        if(i > this.maxX || j > this.maxY) {
            return;
        }

        calibrationData.add(br);
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

    public void SetBeacons(String[] beacons){
        this.beacons = beacons;
    }

    public List<BeaconRecord> GetCalibrationData(){
        return this.calibrationData;
    }

}

