package com.application.helpshake.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.firestore.GeoPoint;

public class MapService {

    public static void showOnGoogleMap(GeoPoint geoPoint, Context context) {
        Uri gmmIntentUri = Uri.parse("google.navigation:cbll=" + geoPoint.getLatitude() + "," + geoPoint.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }
}
