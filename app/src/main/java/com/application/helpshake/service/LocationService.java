package com.application.helpshake.service;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.application.helpshake.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

import lombok.SneakyThrows;

public class LocationService {

    public interface LocationServiceListener {
        void onGpsDisabled();
        void onLocationFetched(GeoPoint geoPoint);
    }

    private LocationServiceListener listener;
    private Context context;
    private Activity activity;

    public LocationService(Activity activity, Context context) {
        this.context = context;
        this.activity = activity;
        this.listener = (LocationServiceListener) context;
    }

    public boolean checkLocationServices() {
        return isGoogleApiServiceEnabled() && isGpsEnabled();
    }

    private boolean isGoogleApiServiceEnabled() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, 1);
            dialog.show();
        } else {
            Toast.makeText(context, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            listener.onGpsDisabled();
            return false;
        }
        return true;
    }

    public void startLocationService() {
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_INITIAL_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.getFusedLocationProviderClient(activity)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @SneakyThrows
                    @Override
                    public void onLocationResult(LocationResult result) {
                        LocationServices.getFusedLocationProviderClient(activity)
                                .removeLocationUpdates(this);

                        if (result != null && result.getLocations().size() > 0) {
                            int lastIndex = result.getLocations().size() - 1;
                            double lat = result.getLocations().get(lastIndex).getLatitude();
                            double lon = result.getLocations().get(lastIndex).getLongitude();
                            GeoPoint geoPoint = new GeoPoint(lat, lon);
                            listener.onLocationFetched(geoPoint);
                        }
                    }
                }, Looper.myLooper());
    }
}
