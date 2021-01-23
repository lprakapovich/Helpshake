package com.application.helpshake.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.application.helpshake.Constants;
import com.application.helpshake.model.user.ParsedAddress;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;

public class AddressParser {

    @SneakyThrows
    public static ParsedAddress getParsedAddress(Context context,GeoPoint geoPoint) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), Constants.SINGLE_RESULT);
        Address address = addresses.get(0);
        return new ParsedAddress(
                address.getCountryName(),
                address.getAdminArea(),
                address.getLocality(),
                address.getAddressLine(0)
        );
    }
}
