package com.application.helpshake.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
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
import java.util.List;

public class HelpSeekerHomeActivity extends AppCompatActivity
        implements DialogHelpRequest.RequestSubmittedListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;

    DialogHelpRequest mDialog;
    RequestListAdapter mAdapter;

    ArrayList<HelpSeekerRequest> mHelpRequests;
    User user;

    ActivityHelpSeekerHomeBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_home);

        mBinding.newRequestButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openRequestDialog();
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
        fetchHelpSeekerRequests();
    }


    private void fetchHelpSeekerRequests() {

        Query query = mRequestsCollection.whereEqualTo(
                getString(R.string.collection_doc_uid),
                mUser.getUid());
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
                mUser.getUid(),
                user.getName(),
                user.getSurname(),
                categories,
                Status.Open,
                comment
        );

        String collection = getString(R.string.collectionHelpSeekerRequests);

        mDb.collection(collection).document().set(request)
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
