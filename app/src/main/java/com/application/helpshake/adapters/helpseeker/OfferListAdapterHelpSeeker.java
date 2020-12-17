package com.application.helpshake.adapters.helpseeker;

import android.app.Activity;
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
import com.application.helpshake.model.VolunteerRequest;

import java.util.ArrayList;

public class OfferListAdapterHelpSeeker extends ArrayAdapter<VolunteerRequest> {

    public interface OfferListAdapterListener {
        void onOfferAccepted(int position, VolunteerRequest request);
        void onOfferRejected(int position, VolunteerRequest request);
    }

    OfferListAdapterListener mListener;

    private static class ViewHolder {
        TextView nameAndSurname;
        TextView distance;
        ImageView mapPoint;
        Button acceptButton;
        Button rejectButton;
        TextView infoText;
        TextView title;
    }

    public OfferListAdapterHelpSeeker(ArrayList<VolunteerRequest> data, Context context, Activity listener) {
        super(context, R.layout.list_item_helpseeker_help_offers, data);
        mListener = (OfferListAdapterListener) listener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final VolunteerRequest request = getItem(position);

        OfferListAdapterHelpSeeker.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new OfferListAdapterHelpSeeker.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_help_offers, parent, false);
            viewHolder.nameAndSurname = (TextView) convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.distanceText);
            viewHolder.infoText = (TextView) convertView.findViewById(R.id.informationText);
            viewHolder.mapPoint = (ImageView) convertView.findViewById(R.id.mapPoint);
            viewHolder.acceptButton = (Button) convertView.findViewById(R.id.acceptBtn);
            viewHolder.rejectButton = (Button) convertView.findViewById(R.id.rejectBtn);
            viewHolder.title = (TextView) convertView.findViewById(R.id.requestTitle);

            viewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onOfferAccepted(position, request);
                }
            });

            viewHolder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onOfferRejected(position, request);
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OfferListAdapterHelpSeeker.ViewHolder) convertView.getTag();
        }

         viewHolder.nameAndSurname.setText(request.getVolunteer().getName());
         viewHolder.title.setText(request.getRequest().getTitle());

        return convertView;
    }
}
