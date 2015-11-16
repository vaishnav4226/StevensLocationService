package com.example.srivaishnav.beaconsprototype;

/**
 * Created by SriVaishnav on 10/31/15.
 */
public class BeaconData {

    public BeaconData(String beaconId, int coordinateX, int coordinateY, boolean trigger) {
        this.beaconId = beaconId;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.trigger = trigger;
    }

    public String beaconId;
    public int coordinateX;
    public int coordinateY;
    public boolean trigger;


}
