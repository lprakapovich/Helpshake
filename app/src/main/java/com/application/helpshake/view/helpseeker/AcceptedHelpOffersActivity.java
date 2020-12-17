package com.application.helpshake.view.helpseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpOffersToAcceptBinding;
import com.application.helpshake.databinding.ActivityListAcceptedOffersBinding;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.application.helpshake.model.VolunteerRequest;
import com.application.helpshake.ui.DialogRequestDetails;
import com.application.helpshake.adapters.volunteer.AcceptedRequestListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AcceptedHelpOffersActivity extends AppCompatActivity {

    HelpSeekerRequest request;
    FirebaseAuth mAuth;
    FirebaseUser mFirebaseUser;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;
    CollectionReference mVolunteerRequestsCollection;

    ArrayList<HelpSeekerRequest> mHelpRequests;
    ActivityHelpOffersToAcceptBinding mBinding;
    User user;
    User volunteer;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    ArrayList<String> activeCategories;
    ArrayList<VolunteerRequest> volunteerRequests = new ArrayList<>();
    AcceptedRequestListAdapter mAdapter;

    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_list_accepted_offers);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));
        mHelpRequests = new ArrayList<>();

        queryUser();
        fetchVolunteerRequests();
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

    private void fetchVolunteerRequests() {
        Query query = mVolunteerRequestsCollection
                .whereEqualTo("request.status", Status.InProgress)
                .whereEqualTo("request.volunteer.uid", mUser.getUid());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    mVolunteerRequestsCollection.add(
                            snapshot.toObject(VolunteerRequest.class));
                }
            }
        });
        initializeListAdapter();
    }

    private void initializeListAdapter() {
       /* mAdapter = new AcceptedRequestListAdapter(volunteerRequests, this);
        mBinding.list_requests.setAdapter(mAdapter);
        mAdapter.setContactButtonListener(this);
        mAdapter.setFinishButtonListener(this);

        mBinding.list_requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });*/
    }

}