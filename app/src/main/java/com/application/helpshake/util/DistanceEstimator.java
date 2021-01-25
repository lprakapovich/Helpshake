package com.application.helpshake.util;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

public class DistanceEstimator {
    public static float distanceBetween(GeoPoint from, GeoPoint to) {
        float[] results = new float[1];
        Location.distanceBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), results);
        return results[0];
    }
}
