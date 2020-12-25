package com.application.helpshake.view.helpseeker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.helpseeker.WaitingRequestAdapter;
import com.application.helpshake.databinding.ActivityHelpOffersToAcceptBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.BaseUser;
import com.application.helpshake.model.PublishedHelpRequest;
import com.application.helpshake.model.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OfferListHelpSeekerActivity extends AppCompatActivity
        implements WaitingRequestAdapter.OfferListAdapterListener {

    private ActivityHelpOffersToAcceptBinding mBinding;
    private WaitingRequestAdapter mAdapter;

    private FirebaseFirestore mDb;
    private CollectionReference mPublishedRequestsCollection;

    private BaseUser mUser;
    private ArrayList<PublishedHelpRequest> mRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_help_offers_to_accept);
        mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mUser = ((UserClient) (getApplicationContext())).getCurrentUser();
        fetchVolunteerRequests();
    }

    private void fetchVolunteerRequests() {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("request.helpSeeker.uid", mUser.getUid())
                .whereEqualTo("status", Status.WaitingForApproval.toString());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
                initializeListAdapter();
            }
        });
    }

    private void initializeListAdapter() {
        mAdapter = new WaitingRequestAdapter(mRequests, this, OfferListHelpSeekerActivity.this);
        mBinding.list.setAdapter(mAdapter);
    }

    @Override
    public void OnOfferAccepted(int position, final PublishedHelpRequest request) {

        mRequests.remove(position);
        mAdapter.notifyDataSetChanged();

        updateRequestStatus(request.getUid(), Status.InProgress);

        String id = request.getRequest().getUid();
        deleteCorrespondingOpenRequest(id);
        rejectOtherOffers(id);
    }

    private void deleteCorrespondingOpenRequest(String id) {
        DocumentReference documentToDelete = mDb.collection(
                "PublishedHelpRequests").document(id);
        documentToDelete.delete();
    }

    @Override
    public void OnOfferRejected(int position, final PublishedHelpRequest request) {

        mRequests.remove(position);
        mAdapter.notifyDataSetChanged();
        updateRequestStatus(request.getUid(), Status.Rejected);
    }

    private void updateRequestStatus(String id, Status status) {

        final String title = status.equals(Status.InProgress) ? "Request accepted" : "Request rejected";
        final String message = status.equals(Status.InProgress) ? "Great! A volunteer will get informed immediately."
                : "What a pity! We will let the volunteer know about your decision.";

        mDb.collection("PublishedHelpRequests")
                .document(id)
                .update("status", status)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DialogBuilder.showMessageDialog(
                                getSupportFragmentManager(),
                                title,
                                message
                        );
                    }
                });
    }

    private void rejectOtherOffers(String id) {
        Query query = mDb.collection("PublishedHelpRequests")
                .whereEqualTo("request.uid", id)
                .whereEqualTo("status", Status.WaitingForApproval.toString());

        // get all remaining requests, set their status to rejected
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mDb.collection("PublishedHelpRequests")
                            .document(ds.getId())
                            .update("status", Status.Rejected);
                }
            }
        });
    }
}