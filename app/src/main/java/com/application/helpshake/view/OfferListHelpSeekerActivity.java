package com.application.helpshake.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpOffersToAcceptBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.application.helpshake.model.VolunteerRequest;
import com.application.helpshake.utils.OfferListAdapterHelpSeeker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OfferListHelpSeekerActivity extends AppCompatActivity
        implements OfferListAdapterHelpSeeker.OfferListAdapterListener {

    private ActivityHelpOffersToAcceptBinding mBinding;
    private FirebaseUser mUser;
    private CollectionReference mVolunteerRequestsCollection;
    FirebaseFirestore mDb;
    HelpSeekerRequest helpRequest;

    private ArrayList<VolunteerRequest> mVolunteerRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_offers_to_accept);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mUser = mAuth.getCurrentUser();
        mVolunteerRequests = new ArrayList<>();
        mVolunteerRequestsCollection = mDb.collection("volunteerRequest");
        fetchVolunteerRequests();
    }

    private void fetchVolunteerRequests() {
        Query query = mVolunteerRequestsCollection
                .whereEqualTo("request.helpSeekerUid", mUser.getUid())
                .whereEqualTo("request.status", Status.WaitingForApproval);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    mVolunteerRequests.add(snapshot.toObject(VolunteerRequest.class));
                }
                initializeListAdapter();
            }
        });
    }

    private void initializeListAdapter() {
        OfferListAdapterHelpSeeker mAdapter = new OfferListAdapterHelpSeeker(
                mVolunteerRequests, this, OfferListHelpSeekerActivity.this);
        mBinding.list.setAdapter(mAdapter);
    }

    @Override
    public void onOfferAccepted(final VolunteerRequest request) {
        Toast.makeText(this, request.getVolunteer().getName(), Toast.LENGTH_LONG).show();

        mVolunteerRequests.remove(request);

        Query query = mDb.collection("helpSeekerRequests")
                .whereEqualTo("requestId", request.getRequest().getRequestId());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {

                    helpRequest = snapshot.toObject(HelpSeekerRequest.class);
                    helpRequest.setStatus(Status.InProgress);

                    mDb.collection("helpSeekerRequests").document(snapshot.getId()).set(helpRequest);

                    deleteOtherRequestsWhenAccepted(request);
                }
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Request accepted",
                        "Thank you!"
                );
            }
        });


    }

    public void deleteOtherRequestsWhenAccepted(VolunteerRequest request) {
        Query query = mDb.collection("volunteerRequest")
                .whereEqualTo("request.helpSeekerUid", mUser.getUid())
                .whereEqualTo("request.requestUid", request.getRequest().getRequestId())
                .whereNotEqualTo("request.volunteer.uid", request.getVolunteer().getUid());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                        snapshot.getReference().delete();
                }
            }
        });
    }

    @Override
    public void onOfferRejected(VolunteerRequest request) {
        Toast.makeText(this, request.getVolunteer().getName(), Toast.LENGTH_LONG).show();

        Query query = mDb.collection("helpSeekerRequests")
                .whereEqualTo("requestId", request.getRequest().getRequestId());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {

                    helpRequest = snapshot.toObject(HelpSeekerRequest.class);
                    helpRequest.setStatus(Status.Open);

                    mDb.collection("helpSeekerRequests").document(snapshot.getId()).set(helpRequest);
                }
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Request rejected",
                        "Thank you!"
                );
            }
        });
    }
}