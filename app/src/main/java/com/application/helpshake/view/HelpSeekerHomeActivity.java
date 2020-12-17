package com.application.helpshake.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.RequestEnrollment;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.application.helpshake.ui.DialogHelpRequest;
import com.application.helpshake.utils.RequestListAdapterHelpSeeker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HelpSeekerHomeActivity extends AppCompatActivity implements
        DialogHelpRequest.RequestSubmittedListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;

    DialogHelpRequest mDialog;
    RequestListAdapterHelpSeeker mAdapter;

    ArrayList<HelpSeekerRequest> mHelpRequests;
    User user;

    ActivityHelpSeekerHomeBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.open_requests));

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_home);


        mBinding.newRequestButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openRequestDialog();
                    }
                });

        mBinding.offeredHelpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HelpSeekerHomeActivity.this, OfferListHelpSeekerActivity.class
                        ));
                    }
                }
        );

        mBinding.profileButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(
                                HelpSeekerHomeActivity.this, HelpSeekerProfilePage.class
                        ));
                    }
                }
        );

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));
        mUsersCollection = mDb.collection(getString(R.string.collectionUsers));

        mHelpRequests = new ArrayList<>();
        queryUser();
        fetchHelpSeekerRequests(Status.Open);
    }


    private void fetchHelpSeekerRequests(Status status) {
        mHelpRequests.clear();

        Query query = mRequestsCollection
                .whereEqualTo(getString(R.string.collection_doc_uid), mUser.getUid())
                .whereEqualTo(getString(R.string.collectionNodeStatus), status.toString());

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
        mAdapter = new RequestListAdapterHelpSeeker(mHelpRequests, this);
        mBinding.list.setAdapter(mAdapter);
        mBinding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HelpSeekerRequest request = mHelpRequests.get(position);

            }
        });
    }

    private void openRequestDialog() {
        mDialog = new DialogHelpRequest();
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void onRequestSubmitted(String comment, List<HelpCategory> categories) {
        mDialog.dismiss();
        createNewRequest(comment, categories);
    }

    @Override
    public void OnRequestCancelled() {
        mDialog.dismiss();
    }

    private void createNewRequest(String comment, List<HelpCategory> categories) {
        HelpSeekerRequest request = new HelpSeekerRequest(
                UUID.randomUUID().toString(),
                mUser.getUid(),
                user.getName(),
                user.getSurname(),
                categories,
                Status.Open,
                comment
        );

        String collection = getString(R.string.collectionHelpSeekerRequests);

        mDb.collection(collection).document(request.getRequestId()).set(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        getString(R.string.request_published),
                        getString(R.string.request_published_msg)
                );
            }
        });

//        RequestEnrollment requestEnrollment = new RequestEnrollment(request.getRequestId());
//        mDb.collection("requests_enrollments").document(requestEnrollment.getRequestId()).set(requestEnrollment);
    }

    public void queryUser() {
        Query query = mUsersCollection.whereEqualTo("uid", mUser.getUid());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                   user = snapshot.toObject(User.class);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filtering, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.open_request:
                getSupportActionBar().setTitle(getString(R.string.open_requests));
                fetchHelpSeekerRequests(Status.Open);
                break;
            case R.id.waiting_for_approval_request:
                getSupportActionBar().setTitle(getString(R.string.pending_requests));
                fetchHelpSeekerRequests(Status.WaitingForApproval);
                break;
            case R.id.in_progress_request:
                getSupportActionBar().setTitle(getString(R.string.in_progress_requests));
                fetchHelpSeekerRequests(Status.InProgress);
                break;
            case R.id.completed_request:
                getSupportActionBar().setTitle(getString(R.string.completed_requests));
                fetchHelpSeekerRequests(Status.Completed);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
