package com.application.helpshake.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityVolunteerHomeBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.application.helpshake.model.VolunteerRequest;
import com.application.helpshake.ui.DialogRequestDetails;
import com.application.helpshake.adapters.volunteer.RequestListAdapterVolunteer;
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
        implements DialogRequestDetails.RequestSubmittedListener {

    HelpSeekerRequest request;
    FirebaseAuth mAuth;
    FirebaseUser mFirebaseUser;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;

    RequestListAdapterVolunteer mAdapter;
    ArrayList<HelpSeekerRequest> mHelpRequests;
    ActivityVolunteerHomeBinding mBinding;
    DialogRequestDetails mDialog;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    ArrayList<String> activeCategories;

    User mUser;

    int[] images = {R.drawable.camera, R.drawable.ic_baseline_location_on_24};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_home);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));
        mHelpRequests = new ArrayList<>();

        fetchCurrentUser();

        mBinding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        VolunteerProfilePage.class
                ));
            }
        });

        mBinding.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        SettingsPopUp.class
                ));
            }
        });

        mBinding.notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        AcceptedHelpOffersActivity.class
                ));
            }
        });
    }

    private void fetchCurrentUser() {
        Query query = mDb.collection(getString(R.string.collectionUsers)).whereEqualTo("uid", mFirebaseUser.getUid()).limit(1);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                             @Override
                                             public void onSuccess(QuerySnapshot snapshots) {
                                                 mUser = snapshots.getDocuments().get(0).toObject(User.class);
                                                 initHomeView();
                                             }
                                         }
        );
    }

    private void initHomeView() {
        try {
            setActiveCategories();
            fetchHelpSeekerRequests(activeCategories);
        } catch (NullPointerException e) {
            setSharedPreferences();
            setActiveCategories();
            fetchHelpSeekerRequests(activeCategories);
        }
    }

    @Override
    protected void onResume() { //called when activity is resumed
        super.onResume();
        try {
            mAdapter.clear();
            setActiveCategories();
            fetchHelpSeekerRequests(activeCategories);
        } catch (NullPointerException e) {
            System.out.println("Don't know how to handle it :( But it works :)");
        }
    }

    private void fetchHelpSeekerRequests(ArrayList<String> categories) {
        // 1) fetching requests which contains declared help category
        // (even if there are also categories unwanted by the volunteer)

        Query query = mRequestsCollection.whereArrayContainsAny("helpCategories",
                categories).whereEqualTo("status", Status.Open);

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

    private void setSharedPreferences() {
        //initial setup, all categories selected
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

    private void openRequestDialog(HelpSeekerRequest helpRequest) {
        mDialog = new DialogRequestDetails(helpRequest);
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void OnHelpOffered() {
        mDialog.dismiss();
        request.setStatus(Status.WaitingForApproval);

        mDb.collection(getString(R.string.collectionVolunteerRequest))
                .document()
                .set(new VolunteerRequest(mUser, request))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showDialog(getString(R.string.help_offered),
                                getString(R.string.help_offered_msg));
                    }
                });

        mDb.collection(getString(R.string.collectionHelpSeekerRequests))
                .document(request.getRequestId())
                .update("status", Status.WaitingForApproval)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mHelpRequests.clear();
                        mAdapter.notifyDataSetChanged();
                        fetchHelpSeekerRequests(activeCategories);
                    }
                });
    }


    @Override
    public void OnRequestCancelled() {
        mDialog.dismiss();
    }

    private void showDialog(String title, String message) {
        DialogBuilder.showMessageDialog(getSupportFragmentManager(), title, message);
    }
}