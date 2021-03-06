package com.application.helpshake.view.volunteer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.adapter.volunteer.NotificationsVolunteerAdapter;
import com.application.helpshake.databinding.ActivityVolunteerNotificationBinding;
import com.application.helpshake.model.notification.NotificationDeclinedRequest;
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
        implements NotificationsVolunteerAdapter.DeclinedOfferListAdapterListener{

    private ActivityVolunteerNotificationBinding mBinding;
    private NotificationsVolunteerAdapter mAdapter;

    private FirebaseFirestore mDb;
    private CollectionReference mPublishedRequestsCollection;
    private CollectionReference mNotificationsCollection;

    private BaseUser mUser;
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
                .whereEqualTo("checked", false)
                .whereEqualTo("title", "Help offer was rejected");

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
        mAdapter = new NotificationsVolunteerAdapter(mNotifications,this);
        mBinding.listRequests.setAdapter(mAdapter);
    }

    @Override
    public void onMarkAsRead(int position, final NotificationDeclinedRequest notification) {
        mNotifications.remove(position);
        mAdapter.notifyDataSetChanged();

        deleteMarkedAsReadNotification(notification.getUid());
        deleteRelatedDeclinedRequests(notification.getDeclinedRequestId());
    }

    public void deleteMarkedAsReadNotification(String uid) {
        DocumentReference documentToDelete = mNotificationsCollection.document(uid);
        documentToDelete.delete();
    }

    public void deleteRelatedDeclinedRequests(String requestId) {
        DocumentReference documentToDelete = mPublishedRequestsCollection.document(requestId);
        documentToDelete.delete();
    }
}

