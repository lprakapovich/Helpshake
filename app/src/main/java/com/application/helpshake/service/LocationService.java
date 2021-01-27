package com.application.helpshake.service;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.application.helpshake.Constants;
import com.application.helpshake.view.volunteer.VolunteerHomeActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

import lombok.SneakyThrows;

import static com.application.helpshake.Constants.REQUEST_CODE_GPS_ENABLED;
import static com.application.helpshake.Constants.REQUEST_CODE_LOCATION_PERMISSION;

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
                            lat =51.747149;
                            double lon = result.getLocations().get(lastIndex).getLongitude();
                            lon =  19.450768;
                            GeoPoint geoPoint = new GeoPoint(lat, lon);
                            listener.onLocationFetched(geoPoint);
                        }
                    }
                }, Looper.myLooper());
    }

    public static void openGpsSettings(Activity activity) {
        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(enableGpsIntent, REQUEST_CODE_GPS_ENABLED);
    }

    public static boolean permissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE_LOCATION_PERMISSION
        );
    }
}
