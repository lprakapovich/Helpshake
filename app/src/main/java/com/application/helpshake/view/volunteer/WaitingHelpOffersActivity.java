package com.application.helpshake.view.volunteer;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.WaitingHelpOffersAdapter;
import com.application.helpshake.adapter.volunteer.WaitingHelpOffersAdapter.WaitingHelpOfferListener;
import com.application.helpshake.databinding.ActivityVolunteerWaitingHelpOffersBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.service.GeoFireService;
import com.application.helpshake.service.GeoFireService.GeoFireListener;
import com.application.helpshake.service.MapService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WaitingHelpOffersActivity extends AppCompatActivity
        implements WaitingHelpOfferListener, GeoFireListener {

    private ActivityVolunteerWaitingHelpOffersBinding mBinding;

    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentUser;

    private ArrayList<PublishedHelpRequest> mOffers;
    private GeoFireService mGeoFireService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_waiting_help_offers);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

        mOffers = new ArrayList<>();
        mGeoFireService = new GeoFireService(this);
        getSupportActionBar().setTitle("Currently waiting");
        fetchHelpOffers();
    }

    private void fetchHelpOffers() {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("volunteer.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.WaitingForApproval.toString());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mOffers.add(ds.toObject(PublishedHelpRequest.class));
                }
                initializeAdapter();
            }
        });
    }

    private void initializeAdapter() {
        WaitingHelpOffersAdapter mAdapter = new WaitingHelpOffersAdapter(mOffers, this);
        mBinding.list.setAdapter(mAdapter);
    }

    @Override
    public void onMapClicked(PublishedHelpRequest request) {
        GeoPoint from = ((UserClient)(getApplicationContext())).getCurrentLocation();
        GeoPoint to = new GeoPoint(
                request.getRequest().getHelpSeeker().getAddress().getLatitude(),
                request.getRequest().getHelpSeeker().getAddress().getLongitude()
        );
        MapService.showOnGoogleMap(from, to, this);
    }

    @Override
    public void onKeysReceived(HashMap<String, GeoPoint> keyGeoPoints) {
        // nothing
    }

    @Override
    public void onLocationReceived(GeoPoint to) {
        GeoPoint from = ((UserClient)(getApplicationContext())).getCurrentLocation();
        MapService.showOnGoogleMap(from, to, this);
    }
}