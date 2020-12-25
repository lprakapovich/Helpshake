package com.application.helpshake.adapter.volunteer;

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
import com.application.helpshake.model.PublishedHelpRequest;

import java.util.ArrayList;

public class CurrentHelpOffersAdapter extends ArrayAdapter<PublishedHelpRequest> {

    public interface CurrentHelpOfferListener {
        void OnContact(PublishedHelpRequest request);
    }

    CurrentHelpOfferListener mListener;

    private static class ViewHolder {
        ImageView photo;
        TextView nameAndSurname;
        Button contact;
    }

    public CurrentHelpOffersAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_volunteer_current_offer, data);
        mListener = (CurrentHelpOfferListener) context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_current_offer, parent, false);
            viewHolder.photo = convertView.findViewById(R.id.helpSeekerPhoto);
            viewHolder.nameAndSurname =  convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.contact = convertView.findViewById(R.id.contact);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.nameAndSurname.setText(request.getRequest().getHelpSeeker().getName());
        viewHolder.contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnContact(request);
            }
        });

        return convertView;
    }
}
