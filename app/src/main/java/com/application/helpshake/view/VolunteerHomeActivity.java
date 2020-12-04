package com.application.helpshake.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityVolunteerHomeBinding;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.ui.DialogHelpRequest;
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

public class VolunteerHomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;

    RequestListAdapter mAdapter;
    ArrayList<HelpSeekerRequest> mHelpRequests;

    ActivityVolunteerHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_home);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));

        mHelpRequests = new ArrayList<>();

        fetchHelpSeekerRequests();
    }

    private void fetchHelpSeekerRequests() {
        Query query = mRequestsCollection;
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

        mAdapter = new RequestListAdapter(mHelpRequests, this);
        mBinding.listRequests.setAdapter(mAdapter);

        mBinding.listRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HelpSeekerRequest request = mHelpRequests.get(position);
            }
        });
    }
}