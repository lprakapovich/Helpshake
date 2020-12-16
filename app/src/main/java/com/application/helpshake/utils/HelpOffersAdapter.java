package com.application.helpshake.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.RequestEnrollment;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HelpOffersAdapter extends ArrayAdapter<HelpSeekerRequest> {

    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;
    CollectionReference mEnrollmentsCollection;
    FirebaseUser mUser;
    RequestEnrollment enrollment;

    User user;


    private static class ViewHolder {
        ImageView volunteerPhoto;
        TextView nameAndSurname;
        TextView distance;
        ImageView mapPoint;
        Button acceptButton;
        Button rejectButton;
        TextView infoText;
    }

    public HelpOffersAdapter(ArrayList<HelpSeekerRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_help_offers, data);

        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection("helpSeekerRequests");
        mUsersCollection = mDb.collection("users");
        mEnrollmentsCollection = mDb.collection("requests_enrollments");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final HelpSeekerRequest request = getItem(position);

        HelpOffersAdapter.ViewHolder viewHolder;

        final int pos = position;
        if (convertView == null) {
            viewHolder = new HelpOffersAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_help_offers, parent, false);
            viewHolder.volunteerPhoto = (ImageView) convertView.findViewById(R.id.volunteerPhoto);
            viewHolder.nameAndSurname = (TextView) convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.distanceText);
            viewHolder.infoText = (TextView) convertView.findViewById(R.id.informationText);
            viewHolder.mapPoint = (ImageView) convertView.findViewById(R.id.mapPoint);
            viewHolder.acceptButton = (Button) convertView.findViewById(R.id.acceptBtn);
            viewHolder.rejectButton = (Button) convertView.findViewById(R.id.rejectBtn);

            viewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDb.collection("helpSeekerRequests")
                            .document(request.getRequestId()).update("status", Status.InProgress);
                    remove(request); // remove the item
                    notifyDataSetChanged();

                }
            });

            viewHolder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDb.collection("helpSeekerRequests")
                            .document(request.getRequestId()).update("status", Status.Open);

                    mDb.collection("requests_enrollments").document(request.getRequestId()).update("volunteerId", "");
                    remove(request); // remove the item
                    notifyDataSetChanged();
                }
            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (HelpOffersAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.nameAndSurname.setText(getVolunteerName(request.getRequestId()));

        //viewHolder.imageView.setTag(position);
        return convertView;
    }

    public String getVolunteerName(String requestId) {
      //  queryEnrollment(requestId);
       return "";
    }

    public void queryEnrollment(String requestId) {

        Query query = mEnrollmentsCollection.whereEqualTo("requestId", requestId);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    enrollment = snapshot.toObject(RequestEnrollment.class);
                }
            }
        });
    }

    public void queryVolunteer() {
        Query query = mUsersCollection.whereEqualTo("uid",
                enrollment.getVolunteerId());
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
