package com.application.helpshake.view.volunteer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.CurrentHelpOffersAdapter;
import com.application.helpshake.adapter.volunteer.WaitingHelpOffersAdapter;
import com.application.helpshake.databinding.ActivityCurrentHelpOffersBinding;
import com.application.helpshake.databinding.ActivityVolunteerWaitingHelpOffersBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class WaitingHelpOffersActivity extends AppCompatActivity
        implements WaitingHelpOffersAdapter.WaitingHelpOfferListener {

    ActivityVolunteerWaitingHelpOffersBinding mBinding;
    WaitingHelpOffersAdapter mAdapter;

    FirebaseFirestore mDb;
    CollectionReference mPublishedRequestsCollection;
    BaseUser mCurrentUser;

    ArrayList<PublishedHelpRequest> mOffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_waiting_help_offers);

        mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

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
        mAdapter = new WaitingHelpOffersAdapter(mOffers, this);
        mBinding.list.setAdapter(mAdapter);
    }
}