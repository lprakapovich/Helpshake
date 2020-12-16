package com.application.helpshake.view;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpOffersToAcceptBinding;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.application.helpshake.ui.DialogHelpRequest;
import com.application.helpshake.utils.HelpOffersAdapter;
import com.application.helpshake.utils.RequestListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HelpOfferActivity extends AppCompatActivity {

    private ActivityHelpOffersToAcceptBinding mBinding;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;
    CollectionReference mRequestsEnrollmentsCollection;

    HelpOffersAdapter mAdapter;

    ArrayList<HelpSeekerRequest> mHelpRequests;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_offers_to_accept);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));
        mUsersCollection = mDb.collection(getString(R.string.collectionUsers));
        mRequestsEnrollmentsCollection = mDb.collection("requests_enrollments");
        mHelpRequests = new ArrayList<>();

        fetchHelpOffers();
    }

    private void fetchHelpOffers() {

        Query query = mRequestsCollection.whereEqualTo(
                "helpSeekerUid",
                mUser.getUid()).whereEqualTo("status", Status.WaitingForApproval);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    mHelpRequests.add(
                            snapshot.toObject(HelpSeekerRequest.class));
                }

                initializeListAdapter();
            }
        });
    }

    private void initializeListAdapter() {

        mAdapter = new HelpOffersAdapter(mHelpRequests, this);
        mBinding.list.setAdapter(mAdapter);

        mBinding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }


    public void queryUser() {
        Query query = mUsersCollection.whereEqualTo("uid",
                mUser.getUid());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    user = snapshot.toObject(User.class);
                }
            }
        });
    }
}