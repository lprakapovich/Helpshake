package com.application.helpshake.adapter.volunteer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.notification.NotificationRequestVolunteer;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class NotificationsVolunteerAdapter extends ArrayAdapter<NotificationRequestVolunteer> {

    public interface DeclinedOrAcceptedOfferListAdapterListener {
        void onMarkAsRead(int position, NotificationRequestVolunteer notification);
    }

    DeclinedOrAcceptedOfferListAdapterListener mListener;
    NotificationsVolunteerAdapter.ViewHolder viewHolder;

    private static class ViewHolder {
        ImageView helpSeekerPhoto;
        TextView notificationTitle;
        TextView requestTitle;
        Button markAsReadButton;
    }

    public NotificationsVolunteerAdapter(ArrayList<NotificationRequestVolunteer> data, Context context) {
        super(context, R.layout.list_item_volunteer_declined_help_offers, data);
        mListener = (DeclinedOrAcceptedOfferListAdapterListener) context;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final NotificationRequestVolunteer notification = getItem(position);

        if (convertView == null) {
            viewHolder = new  NotificationsVolunteerAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_declined_help_offers, parent, false);
            viewHolder.notificationTitle =  convertView.findViewById(R.id.notificationTitle);
            viewHolder.requestTitle = convertView.findViewById(R.id.requestTitleById);
            viewHolder.markAsReadButton = convertView.findViewById(R.id.markButton);
            viewHolder.helpSeekerPhoto = convertView.findViewById(R.id.helpseekerPhoto);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = ( NotificationsVolunteerAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.notificationTitle.setText(notification.getTitle());
        viewHolder.requestTitle.setText("Request title: " + notification.getRequestTitle());

        viewHolder.markAsReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMarkAsRead(position, notification);
            }
        });

        setHelpSeekerImage(notification);
        return convertView;
    }

    public void setHelpSeekerImage(NotificationRequestVolunteer notification) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + notification.getTo().getUid() + ".jpeg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri)
                        .fitCenter().into(viewHolder.helpSeekerPhoto);
            }
        });
    }
}


