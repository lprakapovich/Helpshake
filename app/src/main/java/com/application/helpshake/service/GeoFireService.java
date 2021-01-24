package com.application.helpshake.service;

import android.content.Context;

import com.application.helpshake.model.user.Address;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeoFireService {

    public interface GeoFireListener {
        void onKeysReceived(HashMap<String, GeoPoint> keyGeoPoints);
    }

    private GeoFireListener mListener;
    private GeoFirestore mGeoFireStore;
    private List<String> mGeoFireKeys;
    private HashMap<String, GeoPoint> mKeyGeoPoints;

    public GeoFireService(Context context) {
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        CollectionReference mGeoFireStoreReference = mDb.collection("GeoFireStores");
        mGeoFireStore = new GeoFirestore(mGeoFireStoreReference);
        mKeyGeoPoints = new HashMap<>();
        //mGeoFireKeys = new ArrayList<>();
        mListener = (GeoFireListener) context;
    }

    public void getGeoFireStoreKeysWithinRange(Address address, float radius) {
        mGeoFireStore.queryAtLocation(new GeoPoint(address.getLatitude(), address.getLongitude()), radius)
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoPoint geoPoint) {
                        //mGeoFireKeys.add(key);
                        mKeyGeoPoints.put(key, geoPoint);
                    }
                    @Override
                    public void onKeyExited(String key) {
                    }

                    @Override
                    public void onKeyMoved(String key, GeoPoint geoPoint) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        mListener.onKeysReceived(mKeyGeoPoints);
                    }

                    @Override
                    public void onGeoQueryError(Exception e) {

                    }
                });
    }

    public void addGeoStore(String id, double latitude, double longitude) {
        mGeoFireStore.setLocation(id, new GeoPoint(latitude, longitude));
    }
}
