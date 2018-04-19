package me.iologic.apps.dtn;

import android.location.Location;
import android.util.Log;

/**
 * Created by vinee on 10-02-2018.
 */

public class LightningMcQueen {

    private double curTime = 0;
    private double oldLat = 0.0;
    private double oldLon = 0.0;

    public double getSpeed(Location location) {
        double newTime = System.currentTimeMillis();
        double newLat = location.getLatitude();
        double newLon = location.getLongitude();
       // if (location.hasSpeed()) {
         //   Log.i(Constants.TAG, "I am in if condition of SPEED");
         //   float speed = location.getSpeed();
         //   return speed;
        //} else {
            double distance = calculationBydistance(newLat, newLon, oldLat, oldLon);
            double timeDifference = newTime - curTime;
            double speed = distance / (timeDifference / 1000.0);
            // Log.i(Constants.TAG, "Speed: " + speed);
            curTime = newTime;
            oldLat = newLat;
            oldLon = newLon;

            return speed;

    //    }
    }

    private double calculationBydistance(double lat1, double lon1, double lat2, double lon2) {
        double radius = Constants.Earth.RADIUS;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radius * c;
    }
}
