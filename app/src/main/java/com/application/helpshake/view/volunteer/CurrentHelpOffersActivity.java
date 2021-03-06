package com.application.helpshake.view.volunteer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.CurrentHelpOffersAdapter;
import com.application.helpshake.databinding.ActivityCurrentHelpOffersBinding;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.view.helpseeker.HelpSeekerHomeActivity;
import com.application.helpshake.view.helpseeker.HelpSeekerProfilePage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CurrentHelpOffersActivity extends AppCompatActivity implements CurrentHelpOffersAdapter.CurrentHelpOfferListener {

    ActivityCurrentHelpOffersBinding mBinding;
    CurrentHelpOffersAdapter mAdapter;

    FirebaseFirestore mDb;
    CollectionReference mPublishedRequestsCollection;
    BaseUser mCurrentUser;

    ArrayList<PublishedHelpRequest> mOffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_current_help_offers);

        mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

        bindButtons();
        fetchHelpOffers();
    }

    private void bindButtons() {
        mBinding.home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CurrentHelpOffersActivity.this, HelpSeekerHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void fetchHelpOffers() {

        Query query = mPublishedRequestsCollection
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
    public void onContact(PublishedHelpRequest request) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + request.getRequest().getHelpSeeker().getPhoneNumber()));
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

}