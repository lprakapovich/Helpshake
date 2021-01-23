package com.application.helpshake.view.helpseeker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityEditHelpseekerProfileBinding;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;

import static com.application.helpshake.Constants.GALLERY_REQUEST_CODE;
import static com.application.helpshake.Constants.REQUEST_CODE_GPS_ENABLED;
import static com.application.helpshake.Constants.REQUEST_CODE_LOCATION_PERMISSION;

public class EditProfileHelpSeekerActivity extends AppCompatActivity {

    private ActivityEditHelpseekerProfileBinding mBinding;
    private CollectionReference mUsersCollection;
    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentUser;
    private String phoneNum;

    private ArrayList<PublishedHelpRequest> mPublishedRequests;
    private Uri imageData;

    private boolean mLocationPermissionGranted;
    private boolean mLocationPermissionRejected;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_helpseeker_profile);
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();
        mPublishedRequests = new ArrayList<>();

        mLocationPermissionGranted = false;
        mLocationPermissionRejected = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //setImageProfile();
        setPhoneNumber();
        setBindings();

    }


    private void setBindings() {
        mBinding.nameHelpSeeker.setText(mCurrentUser.getFullName());

        mBinding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                saveInformationToDatabase();
            }
        });

        mBinding.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
                }
            }
        });

        mBinding.fetchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            EditProfileHelpSeekerActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    getCurrentLocation();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Location should be enabled",
                        "Denied. Please enable in manually in settings"
                );
            }
        }
    }


    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(EditProfileHelpSeekerActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult result) {
                        LocationServices.getFusedLocationProviderClient(EditProfileHelpSeekerActivity.this)
                                .removeLocationUpdates(this);

                        if (result != null && result.getLocations().size() > 0) {
                            int lastIndex = result.getLocations().size() - 1;

                            double lat = result.getLocations().get(lastIndex).getLatitude();
                            double lon = result.getLocations().get(lastIndex).getLongitude();

                            mBinding.currentLocation.setText(lat + ", " + lon);
                            Log.d("LOCATION FETCHED", lat + ", " + lon );
                        }
                    }
                }, Looper.myLooper());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageData = data.getData();
            mBinding.changeButton.setImageURI(imageData);
        }
    }

    // save to Firebase storage
    private void handleUpload(Uri uri) {

        String uid = mCurrentUser.getUid();
        String path = "profileImages/" + uid + ".jpeg";
        final StorageReference reference = FirebaseStorage.getInstance().getReference(path);

        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        // Picasso.get().load(task.getResult()).into(mBinding.changeImage);
                    }
                });
            }
        });
    }

    private void readUserInput() {
        phoneNum = mBinding.phoneInput.getText().toString();
    }

    private void saveInformationToDatabase() {
        mUsersCollection.document(mCurrentUser.getUid()).update(
                "phoneNumber", phoneNum)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DialogBuilder.showMessageDialog(
                                getSupportFragmentManager(),
                                "Information updated",
                                "Thanks for providing information"
                        );
                    }
                });

        mCurrentUser.setPhoneNumber(phoneNum);
        findRequestsToUpdatePhoneNum();
        handleUpload(imageData);
    }

    private void findRequestsToUpdatePhoneNum() {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mPublishedRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
                updatePhoneNumber();
            }
        });
    }

    private void updatePhoneNumber() {
        for (PublishedHelpRequest request : mPublishedRequests) {
            mPublishedRequestsCollection.document(request.getUid()).update("request.helpSeeker.phoneNumber", phoneNum);
        }
    }

    public void setPhoneNumber() {
        mBinding.phoneInput.setText(mCurrentUser.getPhoneNumber());
    }

    public void setImageProfile() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + mCurrentUser.getUid() + ".jpeg");
        imageData = Uri.parse(ref.getDownloadUrl().toString());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri)
                        .fitCenter().into(mBinding.changeButton);
                //Picasso.get().load(uri).into(mBinding.changeButton);
            }
        });
    }


//    private boolean checkMapServices() {
//        return isServiceEnabled() && isMapsEnabled();
//    }
//
//    private boolean isMapsEnabled() {
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            buildAlertMessageNoGps();
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isServiceEnabled() {
//        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(EditProfileHelpSeekerActivity.this);
//        if (available == ConnectionResult.SUCCESS) {
//            Log.d("LOCATION", "isServicesOK: Google Play Services is working");
//            return true;
//        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
//            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
//            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(EditProfileHelpSeekerActivity.this, available, 1);
//            dialog.show();
//        } else {
//            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
//        }
//        return false;
//    }
//
//    private void buildAlertMessageNoGps() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
//                .setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivityForResult(enableGpsIntent, REQUEST_CODE_GPS_ENABLED);
//                    }
//                });
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (checkMapServices()) {
//            if (mLocationPermissionGranted) {
//                // everything is ok
//            } else {
//                getLocationPermission();
//            }
//        }
//    }
//
//    private void getLocationPermission() {
//        if (permissionIsGranted()) {
//            mLocationPermissionGranted = true;
//            getLastKnownLocation();
//            Toast.makeText(getApplicationContext(), "Location is ok", Toast.LENGTH_LONG).show();
//        } else if (mLocationPermissionRejected) {
//            Toast.makeText(getApplicationContext(), "Please enable permissions manually", Toast.LENGTH_LONG).show();
//        } else {
//            requestPermissionExplicitly();
//        }
//    }
//
//    private void requestPermissionExplicitly() {
//        ActivityCompat.requestPermissions(this,
//                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                REQUEST_CODE_LOCATION_PERMISSION);
//    }
//
//    private boolean permissionIsGranted() {
//        return ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
//            // If request is cancelled, the result arrays are empty.
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mLocationPermissionGranted = true;
//            } else {
//                mLocationPermissionRejected = true;
//            }
//        }
//    }
//
//    private void getLastKnownLocation() {
//
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(), "ERROR WITH LOCATION", Toast.LENGTH_LONG).show();
//            return;
//        } else {
//            Toast.makeText(getApplicationContext(), "its ok WITH LOCATION", Toast.LENGTH_LONG).show();
//        }
//
//        mLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//            @SneakyThrows
//            @Override
//            public void onComplete(@NonNull Task<android.location.Location> task) {
//                if (task.isSuccessful()) {
//                    if (task.getResult() != null) {
//                        Log.d("FETCHED", task.getResult().toString());
//                        Location location = task.getResult();
//                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
//                        Log.d("LAST KNOWN LOCATION", geoPoint.getLatitude() + ", " + geoPoint.getLongitude());
//
//                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
//                        List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
//
//                        String address = addresses.get(0).getAddressLine(0);
//                        String city = addresses.get(0).getLocality();
//                        String state = addresses.get(0).getAdminArea();
//                        String country = addresses.get(0).getCountryName();
//
//                        Log.d("ADDRESS", address + ", " + city + "," + state + "," + country);
//
//                    } else {
//                        Toast.makeText(getApplicationContext(), "TASK GET REQULT IS NULl", Toast.LENGTH_LONG).show();
//
//                    }
//                }
//            }
//        });
//    }
}