package com.application.helpshake.view.helpseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.request.HelpRequest;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.model.request.UserHelpRequest;
import com.application.helpshake.util.DialogBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
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
    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentBaseUser;

    private ActivityHelpSeekerHomeBinding mBinding;
    private DialogNewHelpRequest mDialog;

    private Status mSelectedStatus;
    private ArrayList<PublishedHelpRequest> mPublishedRequests = new ArrayList<>();
    private ArrayAdapter<PublishedHelpRequest> mCurrentAdapter;

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
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");
        mSelectedStatus = Status.Open;

        getCurrentUser();
        fetchRequests();
    }

    public void getCurrentUser() {
        mCurrentBaseUser = ((UserClient)(getApplicationContext())).getCurrentUser();
    }

    private void fetchRequests() {
        Query query = mSelectedStatus.equals(Status.Open) ? queryOpenRequests() : queryOtherRequests();
        fetchPublishedRequestsWithQuery(query);
    }

    private Query queryOpenRequests() {
        return mPublishedRequestsCollection
                .whereEqualTo("request.helpSeeker.uid", mCurrentBaseUser.getUid())
                .whereEqualTo("status", mSelectedStatus.toString())
                .whereEqualTo("volunteer", null);
    }

    private Query queryOtherRequests() {
        return mPublishedRequestsCollection
                .whereEqualTo("request.helpSeeker.uid", mCurrentBaseUser.getUid())
                .whereEqualTo("status", mSelectedStatus.toString());
    }

    private void fetchPublishedRequestsWithQuery(Query query) {
        mPublishedRequests.clear();
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mPublishedRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
                setAdapter();
            }
        });
    }

    private void setAdapter() {
        switch (mSelectedStatus) {
            case Open:
                mCurrentAdapter = new OpenRequestAdapter(mPublishedRequests, this);
                break;
            case InProgress:
                mCurrentAdapter = new InProgressRequestAdapter(mPublishedRequests, this);
                break;
            case Completed:
                mCurrentAdapter = new CompletedRequestAdapter(mPublishedRequests, this);
                break;
        }

        mBinding.list.setAdapter(mCurrentAdapter);
    }

    private void openNewRequestDialog() {
        mDialog = new DialogNewHelpRequest();
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void onRequestCreated(String title, String comment, List<HelpCategory> categories) {
        mDialog.dismiss();
        createNewRequest(title, comment, categories);
        fetchRequests();
        mCurrentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestCancelled() {
        mDialog.dismiss();
    }

    private void createNewRequest(String title, String description, List<HelpCategory> categories) {

        HelpRequest helpRequest = new HelpRequest(title, description, categories);

        String id = mPublishedRequestsCollection.document().getId();

        PublishedHelpRequest publishedHelpRequest = new PublishedHelpRequest(
                new UserHelpRequest(mCurrentBaseUser, helpRequest, id), null, Status.Open, id);

        mPublishedRequestsCollection.document(id).set(publishedHelpRequest)
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

        fetchRequests();
        return true;
    }

    @Override
    public void onMarkFinished(int position, PublishedHelpRequest request) {

        DialogBuilder.showMessageDialog(
                getSupportFragmentManager(),
                getString(R.string.request_finished),
                getString(R.string.request_finished_msg)
        );

        request.setStatus(Status.Completed);
        updateRequest(request);
    }


    private void updateRequest(PublishedHelpRequest request) {
        mPublishedRequestsCollection.document(request.getUid()).update("status", request.getStatus());
    }

    @Override
    public void onContact(int position, PublishedHelpRequest request) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + request.getVolunteer().getPhoneNumber()));
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

}
