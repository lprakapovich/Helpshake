package com.application.helpshake.view.volunteer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.CompletedHelpOffersAdapter;
import com.application.helpshake.adapter.volunteer.WaitingHelpOffersAdapter;
import com.application.helpshake.databinding.ActivityVolunteerCompletedOffersBinding;
import com.application.helpshake.databinding.ActivityVolunteerWaitingHelpOffersBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.service.GeoFireService;
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
import java.util.Objects;

public class CompletedOffersActivity extends AppCompatActivity
        implements CompletedHelpOffersAdapter.CompletedHelpOfferListener {

    private ActivityVolunteerCompletedOffersBinding mBinding;

    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentUser;

    private ArrayList<PublishedHelpRequest> mOffers;
    CompletedHelpOffersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_completed_offers);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

        mOffers = new ArrayList<>();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Completed requests");
        fetchHelpOffers();
    }

    private void fetchHelpOffers() {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("volunteer.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.Completed.toString());

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
        mAdapter = new CompletedHelpOffersAdapter(mOffers, this);
        mBinding.list.setAdapter(mAdapter);
    }


    @Override
    public void onOfferClosed(int position, PublishedHelpRequest request) {
        mPublishedRequestsCollection.document(request.getUid()).delete();
        mOffers.remove(position);
        mAdapter.notifyDataSetChanged();
    }
}
