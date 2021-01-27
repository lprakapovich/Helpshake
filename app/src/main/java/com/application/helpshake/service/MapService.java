package com.application.helpshake.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.firestore.GeoPoint;

import java.util.Locale;

public class MapService {

    public static void showOnGoogleMap(GeoPoint from, GeoPoint to, Context context) {
        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)", from.getLatitude(),
                from.getLongitude(), "You", to.getLatitude(), to.getLongitude(), "HelpSeeker");

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        context.startActivity(intent);
    }
}
