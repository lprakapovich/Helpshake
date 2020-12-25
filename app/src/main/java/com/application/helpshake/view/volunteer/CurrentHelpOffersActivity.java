package com.application.helpshake.view.volunteer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.Toast;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.CurrentHelpOffersAdapter;
import com.application.helpshake.databinding.ActivityCurrentHelpOffersBinding;
import com.application.helpshake.model.BaseUser;
import com.application.helpshake.model.PublishedHelpRequest;
import com.application.helpshake.model.UserClient;
import com.application.helpshake.model.enums.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CurrentHelpOffersActivity extends AppCompatActivity implements CurrentHelpOffersAdapter.CurrentHelpOfferListener {

    ActivityCurrentHelpOffersBinding mBinding;
    CurrentHelpOffersAdapter mAdapter;

    FirebaseFirestore mDb;
    BaseUser mCurrentUser;

    ArrayList<PublishedHelpRequest> mOffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_current_help_offers);

        mDb = FirebaseFirestore.getInstance();

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

        fetchHelpOffers();
    }

    private void fetchHelpOffers() {

        Query query = mDb.collection("PublishedHelpRequests")
                .whereEqualTo("volunteer.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.InProgress.toString());

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
        mAdapter = new CurrentHelpOffersAdapter(mOffers, this);
        mBinding.list.setAdapter(mAdapter);
    }

    @Override
    public void OnContact(PublishedHelpRequest request) {
        Toast.makeText(this, request.getRequest().getHelpSeeker().getName(), Toast.LENGTH_LONG).show();
    }
}