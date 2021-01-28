package com.application.helpshake.view.helpseeker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.helpseeker.CompletedRequestAdapter;
import com.application.helpshake.adapter.helpseeker.InProgressRequestAdapter;
import com.application.helpshake.adapter.helpseeker.InProgressRequestAdapter.InProcessRequestListAdapterListener;
import com.application.helpshake.adapter.helpseeker.OpenRequestAdapter;
import com.application.helpshake.adapter.helpseeker.OpenRequestAdapter.OpenRequestListAdapterListener;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.dialog.DialogNewHelpRequest;
import com.application.helpshake.dialog.DialogNewHelpRequest.NewRequestListener;
import com.application.helpshake.dialog.DialogSingleResult;
import com.application.helpshake.dialog.DialogSingleResult.DialogResultListener;
import com.application.helpshake.dialog.DialogVolunteerFeedback;
import com.application.helpshake.dialog.DialogVolunteerFeedback.VolunteerFeedbackListener;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.CompletedRequest;
import com.application.helpshake.model.request.HelpRequest;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.request.UserHelpRequest;
import com.application.helpshake.model.user.Address;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.ParsedAddress;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.service.GeoFireService;
import com.application.helpshake.service.GeoFireService.GeoFireListener;
import com.application.helpshake.service.LocationService;
import com.application.helpshake.service.LocationService.LocationServiceListener;
import com.application.helpshake.util.AddressParser;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.auth.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.application.helpshake.Constants.REQUEST_CODE_LOCATION_PERMISSION;

public class HelpSeekerHomeActivity extends AppCompatActivity
        implements NewRequestListener,
        InProcessRequestListAdapterListener,
        GeoFireListener,
        LocationServiceListener,
        DialogResultListener,
        OpenRequestListAdapterListener,
        VolunteerFeedbackListener {

    private FirebaseFirestore mDb;
    private CollectionReference mPublishedRequestsCollection;
    private CollectionReference mCompletedRequestsCollection;
    private BaseUser mCurrentBaseUser;

    private ActivityHelpSeekerHomeBinding mBinding;
    private DialogNewHelpRequest mDialog;

    private Status mSelectedStatus;
    private ArrayList<PublishedHelpRequest> mPublishedRequests = new ArrayList<>();
    private ArrayAdapter<PublishedHelpRequest> mCurrentAdapter;

    private GeoFireService mGeoFireService;

    private LocationService mLocationService;

    private PublishedHelpRequest mSelectedRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_help_seeker_home);

        getCurrentUser();

        mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");
        mCompletedRequestsCollection = mDb.collection("CompletedRequests");

        mGeoFireService = new GeoFireService(this);
        mLocationService = new LocationService(HelpSeekerHomeActivity.this, this);

        mSelectedStatus = Status.Open;

        setFilteringButtons();
        setBindings();
        fetchRequests();
        handleFloatingButtonVisibility();
    }

    private void handleFloatingButtonVisibility() {
        mBinding.list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }
            int previousFirstVisibleItem = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (previousFirstVisibleItem == firstVisibleItem) {
                    return;
                }
                if (firstVisibleItem > previousFirstVisibleItem) {
                    mBinding.floatingAddRequestButton.hide();
                } else {
                    mBinding.floatingAddRequestButton.show();
                }
                previousFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLocationService.checkLocationServices()) {
            startLocationService();
        }
    }

    private void setBindings() {
        mBinding.floatingAddRequestButton.setEnabled(false);
        mBinding.floatingAddRequestButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkDataRequiredForHelpRequest();
                    }
                }
        );
    }

    private void checkDataRequiredForHelpRequest() {
        if (!isPhoneNumberProvided()) {
            DialogBuilder.showMessageDialog(
                    getSupportFragmentManager(),
                    getString(R.string.missing_phone_number),
                    getString(R.string.missing_phone_number_message));
        } else if (!isAddressProvided()) {
            DialogBuilder.showMessageDialog(
                    getSupportFragmentManager(),
                    "Missing address",
                    "Please, go to your profile and update your current location"
            );
        } else {
            openNewRequestDialog();
        }
    }

    private void setFilteringButtons() {
        RadioGroup filterButtonsGroup = findViewById(R.id.tabButtons);
        filterButtonsGroup.check(R.id.openButton);
        filterButtonsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = group.findViewById(checkedId);
                switch (rb.getId()) {
                    case R.id.openButton:
                        mSelectedStatus = Status.Open;
                        break;
                    case R.id.inProgressButton:
                        mSelectedStatus = Status.InProgress;
                        break;
                    case R.id.completedButton:
                        mSelectedStatus = Status.Completed;
                        break;
                }
                fetchRequests();
            }
        });
    }

    private boolean isPhoneNumberProvided() {
        return !StringUtils.isBlank(mCurrentBaseUser.getPhoneNumber());
    }

    private boolean isAddressProvided() {
        return mCurrentBaseUser.getAddress() != null;
    }

    public void openPhoneNumInfo() {
        DialogBuilder.showMessageDialog(
                getSupportFragmentManager(),
                getString(R.string.missing_phone_number),
                getString(R.string.missing_phone_number_message)
        );
    }

    public void getCurrentUser() {
        mCurrentBaseUser = ((UserClient) (getApplicationContext())).getCurrentUser();
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

        mGeoFireService.addGeoStore(id,
                mCurrentBaseUser.getAddress().getLatitude(),
                mCurrentBaseUser.getAddress().getLongitude());
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
            case R.id.notifications:
                startActivity(new Intent(
                        HelpSeekerHomeActivity.this,
                        OfferListHelpSeekerActivity.class
                ));

                fetchRequests();

                break;
            case R.id.profile:
                startActivity(new Intent(
                        HelpSeekerHomeActivity.this,
                        HelpSeekerProfilePage.class
                ));
                break;
            case R.id.logOut:
                startActivity(new Intent(HelpSeekerHomeActivity.this,
                        LoginActivity.class
                ));
                break;

        }

        return true;
    }

    @Override
    public void onMarkFinished(int position, PublishedHelpRequest request) {
        mSelectedRequest = request;
        DialogVolunteerFeedback dialog = new DialogVolunteerFeedback(this);
        Log.d("mark as finished", request.getUid());
        dialog.show(getSupportFragmentManager(), "TAG");

//        DialogBuilder.showMessageDialog(
//                getSupportFragmentManager(),
//                getString(R.string.request_finished),
//                getString(R.string.request_finished_msg)
//        );
//
//        request.setStatus(Status.Completed);
//        updateRequest(request);
    }

    @Override
    public void onRequestDelete(int position, PublishedHelpRequest request) {
        mDb.collection("PublishedHelpRequests").document(request.getUid()).delete();
        fetchRequests();
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

    @Override
    public void onKeysReceived(HashMap<String, GeoPoint> keyGeoPoints) {
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
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Location should be enabled",
                        "Location access is denied. Please enable in manually in the phone settings."
                );
            }
        }
    }

    @Override
    public void onGpsDisabled() {
        DialogSingleResult mDialogResult = new DialogSingleResult(
                "No GPS detected",
                "This application requires GPS to work properly, do you want to enable it?",
                HelpSeekerHomeActivity.this);
        mDialogResult.show(getSupportFragmentManager(), "tag");
    }

    @Override
    public void onLocationFetched(GeoPoint geoPoint) {
        ((UserClient) (getApplicationContext())).getCurrentUser().setAddress(new Address(geoPoint.getLatitude(), geoPoint.getLongitude()));
        ParsedAddress address = AddressParser.getParsedAddress(getApplicationContext(), geoPoint);
        Toast.makeText(getApplicationContext(), address.getAddress(), Toast.LENGTH_LONG).show();
        mBinding.floatingAddRequestButton.setEnabled(true);
    }

    @Override
    public void onResult() {
        LocationService.openGpsSettings(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onFeedbackSubmitted(float rating) {
        mSelectedRequest.setStatus(Status.Completed);
        Log.d("on feedback submitted", mSelectedRequest.getUid());
        mPublishedRequestsCollection.document(mSelectedRequest.getUid()).delete();
        CompletedRequest completedRequest = new CompletedRequest(mSelectedRequest, rating);
        mCompletedRequestsCollection.document(mSelectedRequest.getUid()).set(completedRequest);
    }
}
