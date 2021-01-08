package com.application.helpshake.view.volunteer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.NotificationDeclinedHelpOffersAdapter;
import com.application.helpshake.databinding.ActivityVolunteerNotificationBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.notification.NotificationDeclinedRequest;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class VolunteerNotificationActivity extends AppCompatActivity
        implements NotificationDeclinedHelpOffersAdapter.DeclinedOfferListAdapterListener{

    private ActivityVolunteerNotificationBinding mBinding;
    private NotificationDeclinedHelpOffersAdapter mAdapter;

    private FirebaseFirestore mDb;
    private CollectionReference mPublishedRequestsCollection;
    private CollectionReference mNotificationsCollection;

    private BaseUser mUser;
    private ArrayList<PublishedHelpRequest> mRequests = new ArrayList<>();
    private ArrayList<NotificationDeclinedRequest> mNotifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_volunteer_notification);
        mDb = FirebaseFirestore.getInstance();
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");
        mNotificationsCollection = mDb.collection("Notifications");

        mUser = ((UserClient) (getApplicationContext())).getCurrentUser();
        fetchNotificationAboutDeclinedRequests();
    }


    private void fetchNotificationAboutDeclinedRequests() {
        Query query = mNotificationsCollection
                .whereEqualTo("to.uid", mUser.getUid())
                .whereEqualTo("checked", false);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mNotifications.add(ds.toObject(NotificationDeclinedRequest.class));
                }
               initializeListAdapter();
            }
        });
    }

    private void initializeListAdapter() {
        mAdapter = new NotificationDeclinedHelpOffersAdapter(mNotifications, this);
        mBinding.listRequests.setAdapter(mAdapter);
    }

    @Override
    public void onMarkAsRead(int position, NotificationDeclinedRequest notification) {
        mRequests.remove(position);
        mAdapter.notifyDataSetChanged();

        DocumentReference documentToDelete = mNotificationsCollection.document(notification.getUid());
        documentToDelete.delete();
    }

}

