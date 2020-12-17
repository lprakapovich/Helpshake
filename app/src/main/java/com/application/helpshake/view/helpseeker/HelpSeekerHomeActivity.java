package com.application.helpshake.view.helpseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapters.helpseeker.CompletedRequestListViewAdapter;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.application.helpshake.model.VolunteerRequest;
import com.application.helpshake.ui.DialogHelpRequest;
import com.application.helpshake.adapters.helpseeker.InProcessRequestListAdapterHelpSeeker;
import com.application.helpshake.adapters.helpseeker.RequestListAdapterHelpSeeker;
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


public class HelpSeekerHomeActivity extends AppCompatActivity
        implements DialogHelpRequest.RequestSubmittedListener,
        InProcessRequestListAdapterHelpSeeker.InProcessRequestListAdapterListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;
    CollectionReference mVolunteerRequestsCollection;

    DialogHelpRequest mDialog;
    RequestListAdapterHelpSeeker mAdapter;
    InProcessRequestListAdapterHelpSeeker mInProcessAdapter;
    CompletedRequestListViewAdapter mCompletedAdapter;

    ArrayList<HelpSeekerRequest> mHelpRequests;
    ArrayList<VolunteerRequest> mVolunteerRequests;

    User user;

    ActivityHelpSeekerHomeBinding mBinding;

    Status mSelectedStatus;

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
                        openNewRequestDialog();
                    }
                }
        );

        mBinding.offeredHelpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HelpSeekerHomeActivity.this, OfferListHelpSeekerActivity.class
                        ));
                    }
                }
        );

        mBinding.profileViewButton.setOnClickListener(
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
        mVolunteerRequestsCollection = mDb.collection("volunteerRequest");

        mHelpRequests = new ArrayList<>();
        mVolunteerRequests = new ArrayList<>();

        mSelectedStatus = Status.Open;

        queryUser();
        fetchRequests();
    }


    private void fetchRequests() {
        if (mSelectedStatus.equals(Status.InProgress) || mSelectedStatus.equals(Status.Completed)) {
            fetchFromVolunteerRequestCollection();
        } else {
            fetchFromHelpSeekerRequestsCollection();
        }
    }

    private void fetchFromHelpSeekerRequestsCollection() {
        mHelpRequests.clear();
        Query query = mRequestsCollection
                .whereEqualTo(getString(R.string.collection_doc_uid), mUser.getUid())
                .whereEqualTo(getString(R.string.collectionNodeStatus), mSelectedStatus);

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

    private void fetchFromVolunteerRequestCollection() {
        mVolunteerRequests.clear();
        Query query = mVolunteerRequestsCollection
                .whereEqualTo("request.helpSeekerUid", mUser.getUid())
                .whereEqualTo("request.status", mSelectedStatus);

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


//    private void fetchVolunteerRequests() {
//
//        // if it is an completed, fetch from the
//        Query query = mVolunteerRequestsCollection.whereEqualTo(
//                "request.helpSeekerUid", mUser.getUid())
//                .whereEqualTo("request.status", Status.InProgress);
//
//        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot snapshots) {
//                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
//                    mVolunteerRequestsCollection.add(
//                            snapshot.toObject(VolunteerRequest.class));
//                }
//            }
//        });
//    }


    private void initializeListAdapter() {
        switch(mSelectedStatus) {
            case InProgress:
                mInProcessAdapter = new InProcessRequestListAdapterHelpSeeker(mVolunteerRequests, this);
                mBinding.list.setAdapter(mInProcessAdapter);
                break;

            case Completed:
                mCompletedAdapter = new CompletedRequestListViewAdapter(mVolunteerRequests, this);
                mBinding.list.setAdapter(mCompletedAdapter);
                break;

            default:
                mAdapter = new RequestListAdapterHelpSeeker(mHelpRequests, this);
                mBinding.list.setAdapter(mAdapter);
                break;
        }
    }

    private void openNewRequestDialog() {
        mDialog = new DialogHelpRequest();
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void onRequestSubmitted(String title, String comment, List<HelpCategory> categories) {
        mDialog.dismiss();
        createNewRequest(title, comment, categories);
        fetchRequests();
    }

    @Override
    public void OnRequestCancelled() {
        mDialog.dismiss();
    }

    private void createNewRequest(String title, String comment, List<HelpCategory> categories) {
        HelpSeekerRequest request = new HelpSeekerRequest(
                UUID.randomUUID().toString(),
                mUser.getUid(),
                user.getName(),
                user.getSurname(),
                categories,
                Status.Open,
                title,
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
                mSelectedStatus = Status.Open;
                break;
            case R.id.waiting_for_approval_request:
                getSupportActionBar().setTitle(getString(R.string.pending_requests));
                mSelectedStatus = Status.WaitingForApproval;
                break;
            case R.id.in_progress_request:
                getSupportActionBar().setTitle(getString(R.string.in_progress_requests));
                mSelectedStatus = Status.InProgress;
                break;
            case R.id.completed_request:
                getSupportActionBar().setTitle(getString(R.string.completed_requests));
                mSelectedStatus = Status.Completed;
                break;
        }

        fetchRequests();
        return true;
    }

//    @Override
//    public void onFinishButtonClickListener(int position, HelpSeekerRequest value) {
//        mHelpRequests.remove(position);
//        mAdapter.notifyDataSetChanged();
//
//        mDb.collection("helpSeekerRequests").document(value.getRequestId()).update("status", Status.Completed);
//        mDb.collection("helpSeekerRequests").document(value.getRequestId()).delete();
//
//        mAdapter.clear();
//        fetchRequests();
//    }

//    @Override
//    public void onContactButtonClickListener(int position, HelpSeekerRequest value) {
//        queryToGetVolunteerPhone(value);
//    }

    public void startPhoneActivity(String action, String uri) {
        Uri location = Uri.parse(uri);
        Intent intent = new Intent (action, location);
        checkImplicitIntent(intent);
    }

    public void checkImplicitIntent(Intent intent) {
       if(intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void OnMarkFinishedClicked(int position, VolunteerRequest request) {
        mVolunteerRequests.remove(position);
        mInProcessAdapter.notifyDataSetChanged();

        DialogBuilder.showMessageDialog(
                getSupportFragmentManager(),
                getString(R.string.request_finished),
                getString(R.string.request_finished_msg)
        );

        request.getRequest().setStatus(Status.Completed);

        Query q = mDb.collection("volunteerRequest")
                .whereEqualTo("request.requestId", request.getRequest().getRequestId())
                .whereEqualTo("volunteer.uid", request.getVolunteer().getUid())
                .limit(1);

        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                VolunteerRequest request = snapshots.getDocuments().get(0).toObject(VolunteerRequest.class);
                request.getRequest().setStatus(Status.Completed);
                String id = snapshots.getDocuments().get(0).getId();
                updateRequest(id, request);
            }
        });
    }


    private void updateRequest(String documentId, VolunteerRequest request) {
        mDb.collection("volunteerRequest").document(documentId).update(
                "request.status", request.getRequest().getStatus()
        );
    }

    @Override
    public void OnContact(int position, VolunteerRequest request) {
        startPhoneActivity(Intent.ACTION_DIAL, "tel:" + request.getVolunteer().getPhoneNum());
    }
}
