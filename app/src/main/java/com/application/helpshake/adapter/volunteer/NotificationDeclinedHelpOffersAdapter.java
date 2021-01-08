package com.application.helpshake.adapter.volunteer;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.notification.NotificationDeclinedRequest;

import java.util.ArrayList;

public class NotificationDeclinedHelpOffersAdapter extends ArrayAdapter<NotificationDeclinedRequest> {

    public interface DeclinedOfferListAdapterListener {
        void onMarkAsRead(int position, NotificationDeclinedRequest notification);
    }

    NotificationDeclinedHelpOffersAdapter.DeclinedOfferListAdapterListener mListener;

    private static class ViewHolder {
        TextView notificationTitle;
        TextView notificationMessage;
        TextView fullName;
        TextView requestTitle;
        Button markAsReadButton;
    }

    public NotificationDeclinedHelpOffersAdapter(ArrayList<NotificationDeclinedRequest> data, Context context) {
        super(context, R.layout.list_item_volunteer_declined_help_offers, data);
        mListener = (NotificationDeclinedHelpOffersAdapter.DeclinedOfferListAdapterListener) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final NotificationDeclinedRequest notification = getItem(position);

        NotificationDeclinedHelpOffersAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new NotificationDeclinedHelpOffersAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_declined_help_offers, parent, false);
            viewHolder.notificationTitle =  convertView.findViewById(R.id.notificationTitle);
            viewHolder.notificationMessage = convertView.findViewById(R.id.notificationMessage);
            viewHolder.fullName =  convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.requestTitle = convertView.findViewById(R.id.requestTitle);
            viewHolder.markAsReadButton = convertView.findViewById(R.id.markButton);

            viewHolder.markAsReadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMarkAsRead(position, notification);
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (NotificationDeclinedHelpOffersAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.notificationTitle.setText(notification.getTitle());
        viewHolder.notificationMessage.setText(notification.getMessage());
        viewHolder.fullName.setText(notification.getFrom().getFullName());
        //viewHolder.requestTitle.setText(notification.getDeclinedRequestId());
        return convertView;
    }
}

