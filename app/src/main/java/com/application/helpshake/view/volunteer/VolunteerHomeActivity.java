package com.application.helpshake.view.volunteer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.Constants;
import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.OpenRequestAdapterVolunteer;
import com.application.helpshake.adapter.volunteer.OpenRequestAdapterVolunteer.OpenRequestAdapterListener;
import com.application.helpshake.databinding.ActivityVolunteerHomeBinding;
import com.application.helpshake.dialog.DialogRequestDetails;
import com.application.helpshake.dialog.DialogRequestDetails.RequestSubmittedListener;
import com.application.helpshake.dialog.DialogSingleResult;
import com.application.helpshake.dialog.DialogSingleResult.DialogResultListener;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.Address;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.ParsedAddress;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.service.GeoFireService;
import com.application.helpshake.service.GeoFireService.GeoFireListener;
import com.application.helpshake.service.LocationService;
import com.application.helpshake.service.LocationService.LocationServiceListener;
import com.application.helpshake.service.MapService;
import com.application.helpshake.util.AddressParser;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.auth.LoginActivity;
import com.application.helpshake.view.others.SettingsPopUp;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.application.helpshake.Constants.REQUEST_CODE_LOCATION_PERMISSION;

public class VolunteerHomeActivity extends AppCompatActivity implements RequestSubmittedListener,
        OpenRequestAdapterListener,
        LocationServiceListener,
        GeoFireListener,
        DialogResultListener {

    private CollectionReference mPublishedRequestsCollection;

    private ActivityVolunteerHomeBinding mBinding;
    private DialogRequestDetails mDialog;

    private SharedPreferences sharedPref;

    private ArrayList<String> activeCategories;
    private List<String> mFetchedIds;

    private BaseUser mCurrentUser;
    private ArrayList<PublishedHelpRequest> mPublishedOpenRequests = new ArrayList<>();
    private ArrayList<PublishedHelpRequest> mPublishedWaitingRequests = new ArrayList<>();
    private PublishedHelpRequest mPublishedRequest;
    private OpenRequestAdapterVolunteer mAdapter;

    private GeoFireService mGeoFireService;
    private LocationService mLocationService;
    private boolean mLocationAccessDenied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_home);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mGeoFireService = new GeoFireService(this);
        mLocationService = new LocationService(VolunteerHomeActivity.this, this);
        mLocationAccessDenied = false;

        setBindings();
        getCurrentUser();
        initHomeView();

        handleFloatingButtonVisibility();
    }

    private void handleFloatingButtonVisibility() {

        mBinding.listRequests.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            int previousFirstVisibleItem = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //firstVisibleItem - first item in the list is 0, then 1, etc.

                if (previousFirstVisibleItem == firstVisibleItem) {
                    return;
                }
                if (firstVisibleItem > previousFirstVisibleItem) {
                    mBinding.floatingSetPreferencesButton.hide();
                } else {
                    mBinding.floatingSetPreferencesButton.show();
                }

                previousFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_helpseeker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myRequests:
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        CurrentHelpOffersActivity.class
                ));
                break;
            case R.id.notifications:
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        VolunteerNotificationActivity.class
                ));
                break;
            case R.id.profile:
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        VolunteerProfilePage.class
                ));
                break;
            case R.id.logOut:
                startActivity(new Intent(VolunteerHomeActivity.this, LoginActivity.class
                ));
                break;
        }
        return true;
    }

    private void setBindings() {
        mBinding.floatingSetPreferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(
                        VolunteerHomeActivity.this,
                        SettingsPopUp.class
                ));
            }
        });
    }

    private void getCurrentUser() {
        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();
    }

    private void initHomeView() {
        try {
            setActiveCategories();
            findWaitingRequestsForUser();
        } catch (NullPointerException e) {
            setSharedPreferences();
            setActiveCategories();
            findWaitingRequestsForUser();
        }
    }

    private void fetchHelpSeekerRequests(ArrayList<String> categories) {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("status", Status.Open.toString())
                .whereEqualTo("volunteer", null)
                .whereArrayContainsAny("request.helpRequest.categoryList", categories);

        mPublishedOpenRequests.clear();

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    if (mFetchedIds.contains(ds.getId())) {
                        mPublishedOpenRequests.add(ds.toObject(PublishedHelpRequest.class));
                    }
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
        for (PublishedHelpRequest r : requestsToDelete) {
            mPublishedOpenRequests.remove(r);
        }
    }

    private void initializeListAdapter() {
        mAdapter = new OpenRequestAdapterVolunteer(mPublishedOpenRequests, this);
        mBinding.listRequests.setAdapter(mAdapter);

        mBinding.listRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPublishedRequest = mPublishedOpenRequests.get(position);
                mDialog = new DialogRequestDetails(mPublishedRequest);
                mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
            }
        });
    }

    @Override
    public void onHelpOffered() {
        if (!StringUtils.isBlank(mCurrentUser.getPhoneNumber())) {
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
        } else {
            DialogBuilder.showMessageDialog(
                    getSupportFragmentManager(),
                    getString(R.string.missing_phone),
                    getString(R.string.missing_phone_message)
            );
        }
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
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cDog", "DogWalking");
        editor.putString("cDrug", "Drugstore");
        editor.putString("cOther", "Other");
        editor.putString("cGrocery", "Grocery");
        editor.apply();
    }

    private void setActiveCategories() {
        activeCategories = new ArrayList<>(Arrays.asList(sharedPref.getString("cDog", "Do"),
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
            System.out.println("Nevermind");
        }

        if (mLocationService.checkLocationServices() && !mLocationAccessDenied) {
            startLocationService();
        }
    }

    private void startLocationService() {
        if (permissionNotGranted()) {
            LocationService.requestPermissions(this);
        } else {
            mLocationService.startLocationService();
        }
    }

    private boolean permissionNotGranted() {
        return !LocationService.permissionGranted(this);
    }

    ;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationService.startLocationService();
            } else {
                mLocationAccessDenied = true;
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Location access is denied",
                        "We can't adjust the searching engine without your location. Please, enable in manually in the phone settings."
                );
            }
        }
    }

    @Override
    public void onGpsDisabled() {
        DialogSingleResult mDialogResult = new DialogSingleResult(
                "No GPS detected",
                "This application requires GPS to work properly, do you want to enable it?",
                VolunteerHomeActivity.this);
        mDialogResult.show(getSupportFragmentManager(), "tag");
    }

    @Override
    public void onLocationFetched(GeoPoint geoPoint) {
        ((UserClient) (getApplicationContext())).getCurrentUser().setAddress(new Address(geoPoint.getLatitude(), geoPoint.getLongitude()));
        ParsedAddress address = AddressParser.getParsedAddress(getApplicationContext(), geoPoint);
        Toast.makeText(getApplicationContext(), address.getAddress(), Toast.LENGTH_LONG).show();
        mGeoFireService.getGeoFireStoreKeysWithinRange(new Address(geoPoint.getLatitude(), geoPoint.getLongitude()), Constants.DEFAULT_SEARCH_RADIUS);
    }

    @Override
    public void onKeysReceived(HashMap<String, GeoPoint> keys) {
        mFetchedIds = Lists.newArrayList(keys.keySet());
        fetchHelpSeekerRequests(activeCategories);

//        for (String key: mFetchedIds) {
//            Log.d("KEY", key);
//        }
//        GeoPoint me = new GeoPoint(mCurrentUser.getAddress().getLatitude(), mCurrentUser.getAddress().getLongitude());
//        for (GeoPoint geoPoint : keys.values()) {
//            Log.d("DISTANCE BETWEEN", DistanceEstimator.distanceBetween(me, geoPoint) + ".");
//        }
    }

    public void onShowOnMapClicked(PublishedHelpRequest request) {
        GeoPoint geoPoint = mGeoFireService.getAssociatedGeoPoint(request.getUid());
        MapService.showOnGoogleMap(geoPoint, this);
    }

    @Override
    public void onResult() {
        LocationService.openGpsSettings(this);
    }
}