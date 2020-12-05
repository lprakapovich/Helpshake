package com.application.helpshake.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityVolunteerHomeBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.ui.DialogRequestDetails;
import com.application.helpshake.utils.RequestListAdapterVolunteer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class VolunteerHomeActivity extends AppCompatActivity
        implements DialogRequestDetails.RequestSubmittedListener {

    HelpSeekerRequest request;
    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;

    RequestListAdapterVolunteer mAdapter;
    ArrayList<HelpSeekerRequest> mHelpRequests;

    ActivityVolunteerHomeBinding mBinding;
    DialogRequestDetails mDialog;

    int[] images = {R.drawable.camera, R.drawable.ic_baseline_location_on_24};

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
        // 1) fetching requests which contains declared help category
        // (even if there are also categories unwanted by the volunteer)
        Query query = mRequestsCollection.whereArrayContainsAny("helpCategories",
               Arrays.asList("DogWalking", "Grocery"));

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

        mAdapter = new RequestListAdapterVolunteer(mHelpRequests, this, images);
        mBinding.listRequests.setAdapter(mAdapter);
        mBinding.listRequests.setItemsCanFocus(false);

        mBinding.listRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                request = mHelpRequests.get(position);
                openRequestDialog(mHelpRequests.get(position));
            }
        });
    }



    private void openRequestDialog(HelpSeekerRequest helpRequest) {
        mDialog = new DialogRequestDetails(helpRequest);
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void onRequestStatusChanged(Status status) {
        mDialog.dismiss();

        String collection = getString(R.string.collectionHelpSeekerRequests);
        request.setStatus(Status.WaitingForApproval);
        mDb.collection(collection).document(request.getRequestId()).update("status", Status.WaitingForApproval)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DialogBuilder.showMessageDialog(
                                getSupportFragmentManager(),
                                getString(R.string.help_offered),
                                getString(R.string.help_offered_msg)
                        );
                    }
                });
    }

    @Override
    public void OnRequestCancelled() {
        mDialog.dismiss();
    }


}