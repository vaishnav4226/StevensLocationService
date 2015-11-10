package com.example.srivaishnav.beaconsprototype;

/**
 * Created by SriVaishnav on 10/31/15.
 */
public class Beacons {

    public Beacons(String beaconId, String coordinate, boolean trigger) {
        this.beaconId = beaconId;
        this.coordinate = coordinate;
        this.trigger = trigger;
    }

    public String beaconId;
    public String coordinate;
    public boolean trigger;


}
