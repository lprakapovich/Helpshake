package com.application.helpshake.util;

import android.location.Location;
import android.util.Pair;

import com.google.firebase.firestore.GeoPoint;

public class DistanceEstimator {
    public static float distanceBetween(GeoPoint from, GeoPoint to) {
        float[] results = new float[1];
        Location.distanceBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), results);
        return results[0];
    }

    public static Pair<Integer, String> parseDistance(Float distance) {
        float parsed = distance.intValue() > 1000 ? distance / 1000 : distance;
        String unit = distance.intValue() > 1000 ? " kilometer(s)" : " meter(s)";
       return new Pair<>(Math.round(parsed), unit);
    }
}
