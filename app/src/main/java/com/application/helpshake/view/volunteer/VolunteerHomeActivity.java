package com.application.helpshake.view.volunteer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.OpenRequestAdapterVolunteer;
import com.application.helpshake.databinding.ActivityVolunteerHomeBinding;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.dialog.DialogRequestDetails;
import com.application.helpshake.view.others.SettingsPopUp;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class VolunteerHomeActivity extends AppCompatActivity
        implements DialogRequestDetails.RequestSubmittedListener,
        OpenRequestAdapterVolunteer.OpenRequestAdapterListener {

    FirebaseAuth mAuth;
    FirebaseUser mFirebaseUser;
    FirebaseFirestore mDb;
    CollectionReference mPublishedRequestsCollection;

    ActivityVolunteerHomeBinding mBinding;
    DialogRequestDetails mDialog;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    ArrayList<String> activeCategories;

    BaseUser mCurrentUser;
    ArrayList<PublishedHelpRequest> mPublishedOpenRequests = new ArrayList<>();
    ArrayList<PublishedHelpRequest> mPublishedWaitingRequests = new ArrayList<>();
    PublishedHelpRequest mPublishedRequest;
    OpenRequestAdapterVolunteer mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_home);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        setBindings();
        getCurrentUser();
        initHomeView();
    }

    private void setBindings() {
        mBinding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        VolunteerProfilePage.class
                ));
            }
        });

        mBinding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        SettingsPopUp.class
                ));
            }
        });

        mBinding.notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        VolunteerNotificationActivity.class
                ));
            }
        });

        mBinding.myRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        CurrentHelpOffersActivity.class
                ));
            }
        });
    }

    private void getCurrentUser() {
        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();
    }

    private void initHomeView() {
        try {
            setActiveCategories();
            findWaitingRequestsForUser();
            fetchHelpSeekerRequests(activeCategories);
        } catch (NullPointerException e) {
            setSharedPreferences();
            setActiveCategories();
            findWaitingRequestsForUser();
            fetchHelpSeekerRequests(activeCategories);
        }
    }

    private void fetchHelpSeekerRequests(ArrayList<String> categories) {

        Query query = mPublishedRequestsCollection
                .whereEqualTo("status", Status.Open.toString())
                .whereEqualTo("volunteer", null)
                .whereArrayContainsAny("request.helpRequest.categoryList", categories);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mPublishedOpenRequests.add(ds.toObject(PublishedHelpRequest.class));
                }

                deleteRequestsIfHelpOfferWasSend();

                initializeListAdapter();
            }
        });
    }

    private void findWaitingRequestsForUser() {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("volunteer.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.WaitingForApproval.toString());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mPublishedWaitingRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
            }
        });
    }

    private void deleteRequestsIfHelpOfferWasSend() {
        ArrayList<PublishedHelpRequest> requestsToDelete = new ArrayList<>();

        for (int i = 0; i < mPublishedOpenRequests.size(); i++) {
            for (int j = 0; j < mPublishedWaitingRequests.size(); j++) {

                if (mPublishedOpenRequests.get(i).getRequest().getUid().equals(
                        mPublishedWaitingRequests.get(j).getRequest().getUid())) {
                    requestsToDelete.add(mPublishedOpenRequests.get(i));
                }
            }
        }
        for(PublishedHelpRequest r : requestsToDelete) {
            mPublishedOpenRequests.remove(r);
        }
    }

    private void initializeListAdapter() {
        mAdapter = new OpenRequestAdapterVolunteer(mPublishedOpenRequests, this);
        mBinding.listRequests.setAdapter(mAdapter);
        mBinding.listRequests.setItemsCanFocus(false);

//        mBinding.listRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                request = mHelpRequests.get(position);
//                openRequestDialog(mHelpRequests.get(position));
//            }
//        });
    }


    @Override
    public void onHelpOffered() {

        mDialog.dismiss();

        String id = mPublishedRequestsCollection.document().getId();

        mPublishedRequest.setStatus(Status.WaitingForApproval);
        mPublishedRequest.setVolunteer(mCurrentUser);
        mPublishedRequest.setUid(id);

        mPublishedRequestsCollection.document(id).set(mPublishedRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showDialog(getString(R.string.help_offered),
                                getString(R.string.help_offered_msg));
                    }
                });

        mPublishedOpenRequests.remove(mPublishedRequest);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogClosed() {
        mDialog.dismiss();
    }

    private void showDialog(String title, String message) {
        DialogBuilder.showMessageDialog(getSupportFragmentManager(), title, message);
    }

    @Override
    public void onDetails(PublishedHelpRequest request) {
        mPublishedRequest = request;
        mDialog = new DialogRequestDetails(request);
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }


    private void setSharedPreferences() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();
        editor.putString("cDog", "DogWalking");
        editor.putString("cDrug", "Drugstore");
        editor.putString("cOther", "Other");
        editor.putString("cGrocery", "Grocery");
        editor.apply();
    }


    private void setActiveCategories() {
        activeCategories = new ArrayList<String>(Arrays.asList(sharedPref.getString("cDog", "Do"),
                sharedPref.getString("cDrug", "Dr"), sharedPref.getString("cGrocery", "Gr"),
                sharedPref.getString("cOther", "Ot")));
        for (String s : activeCategories) {
            System.out.println(s);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            mAdapter.clear();
            setActiveCategories();
            findWaitingRequestsForUser();
            fetchHelpSeekerRequests(activeCategories);
        } catch (NullPointerException e) {
            System.out.println("Don't know how to handle it :( But it works :)");
        }
    }
}