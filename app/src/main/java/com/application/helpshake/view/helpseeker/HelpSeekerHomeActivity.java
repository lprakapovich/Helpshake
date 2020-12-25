package com.application.helpshake.view.helpseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.helpseeker.CompletedRequestAdapter;
import com.application.helpshake.adapter.helpseeker.InProgressRequestAdapter;
import com.application.helpshake.adapter.helpseeker.OpenRequestAdapter;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.dialog.DialogNewHelpRequest;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.BaseUser;
import com.application.helpshake.model.HelpRequest;
import com.application.helpshake.model.PublishedHelpRequest;
import com.application.helpshake.model.UserClient;
import com.application.helpshake.model.UserHelpRequest;
import com.application.helpshake.util.DialogBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HelpSeekerHomeActivity extends AppCompatActivity
        implements DialogNewHelpRequest.NewRequestListener,
        InProgressRequestAdapter.InProcessRequestListAdapterListener {

    private FirebaseFirestore mDb;
    private BaseUser mCurrentBaseUser;

    private ActivityHelpSeekerHomeBinding mBinding;
    private DialogNewHelpRequest mDialog;

    private Status mSelectedStatus;
    private ArrayList<PublishedHelpRequest> mPublishedHelpRequests = new ArrayList<>();

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
                        startActivity(new Intent(
                                HelpSeekerHomeActivity.this,
                                OfferListHelpSeekerActivity.class
                        ));
                    }
                }
        );

        mBinding.profileViewButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(
                                HelpSeekerHomeActivity.this,
                                HelpSeekerProfilePage.class
                        ));
                    }
                }
        );

        mDb = FirebaseFirestore.getInstance();
        mSelectedStatus = Status.Open;

        getCurrentUser();
        configureQuery();
    }

    public void getCurrentUser() {
        mCurrentBaseUser = ((UserClient)(getApplicationContext())).getCurrentUser();
    }

    private void configureQuery() {
        Query query = mSelectedStatus.equals(Status.Open) ? queryOpenRequests() : queryOtherRequests();
        fetchPublishedRequests(query);
    }

    private Query queryOpenRequests() {
        return mDb.collection("PublishedHelpRequests")
                .whereEqualTo("request.helpSeeker.uid", mCurrentBaseUser.getUid())
                .whereEqualTo("status", mSelectedStatus.toString())
                .whereEqualTo("volunteer", null);
    }

    private Query queryOtherRequests() {
        return mDb.collection("PublishedHelpRequests")
                .whereEqualTo("request.helpSeeker.uid", mCurrentBaseUser.getUid())
                .whereEqualTo("status", mSelectedStatus.toString());
    }

    private void fetchPublishedRequests(Query query) {
        mPublishedHelpRequests.clear();

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mPublishedHelpRequests.add(ds.toObject(PublishedHelpRequest.class));
                }

                setAdapter();
            }
        });
    }


    private void setAdapter() {
        switch (mSelectedStatus) {
            case Open:
                OpenRequestAdapter mOpenRequestAdapter = new OpenRequestAdapter(mPublishedHelpRequests, this);
                mBinding.list.setAdapter(mOpenRequestAdapter);
                break;
            case InProgress:
                InProgressRequestAdapter mInProcessAdapter = new InProgressRequestAdapter(mPublishedHelpRequests, this);
                mBinding.list.setAdapter(mInProcessAdapter);
                break;
            case Completed:
                CompletedRequestAdapter mCompletedAdapter = new CompletedRequestAdapter(mPublishedHelpRequests, this);
                mBinding.list.setAdapter(mCompletedAdapter);
                break;
        }
    }

    private void openNewRequestDialog() {
        mDialog = new DialogNewHelpRequest();
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void OnRequestCreated(String title, String comment, List<HelpCategory> categories) {
        mDialog.dismiss();
        createNewRequest(title, comment, categories);
        configureQuery();
    }

    @Override
    public void OnRequestCancelled() {
        mDialog.dismiss();
    }

    private void createNewRequest(String title, String description, List<HelpCategory> categories) {
        HelpRequest request = new HelpRequest(
                title,
                description,
                categories
        );

        String id = mDb.collection("PublishedHelpRequests")
                .document().getId();

        PublishedHelpRequest publishedHelpRequest = new PublishedHelpRequest(
                new UserHelpRequest(mCurrentBaseUser, request, id),
                null,
                Status.Open,
                id
        );

        mDb.collection("PublishedHelpRequests")
                .document(id)
                .set(publishedHelpRequest)
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_request_filtering, menu);
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

        configureQuery();
        return true;
    }

    @Override
    public void OnMarkFinished(int position, PublishedHelpRequest request) {

        DialogBuilder.showMessageDialog(
                getSupportFragmentManager(),
                getString(R.string.request_finished),
                getString(R.string.request_finished_msg)
        );

        request.setStatus(Status.Completed);
        updateRequest(request);
    }


    private void updateRequest(PublishedHelpRequest request) {
        mDb.collection("PublishedHelpRequests").document(request.getUid()).
                update("status", request.getStatus()
        );
    }

    @Override
    public void OnContact(int position, PublishedHelpRequest request) {
        startPhoneActivity(Intent.ACTION_DIAL, "tel:" + request.getVolunteer().getPhoneNumber());
    }

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
}
