package com.application.helpshake.adapters.volunteer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.model.VolunteerRequest;

import java.util.ArrayList;

public class AcceptedRequestListAdapter extends ArrayAdapter<VolunteerRequest> {

    AcceptedRequestListAdapter.finishButtonListener finishListener;
    AcceptedRequestListAdapter.contactButtonListener contactListener;

    public AcceptedRequestListAdapter(ArrayList<VolunteerRequest> data, Context context) {
        super(context, R.layout.list_item_volunteer_accepted_offers, data);
    }

    public interface finishButtonListener {
        void onFinishButtonClickListener(int position, VolunteerRequest value);
    }

    public interface contactButtonListener {
        void onContactButtonClickListener(int position,  VolunteerRequest value);
    }

    public void setFinishButtonListener(AcceptedRequestListAdapter.finishButtonListener listener) {
        this.finishListener = listener;
    }

    public void setContactButtonListener(AcceptedRequestListAdapter.contactButtonListener listener) {
        this.contactListener = listener;
    }

    private static class ViewHolder {
        TextView category;
        TextView nameAndSurnameHelpSeeker;
        Button contactBtn;
        Button finishBtn;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final VolunteerRequest request = getItem(position);
        final AcceptedRequestListAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new AcceptedRequestListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_request, parent, false);
            viewHolder.category = (TextView) convertView.findViewById(R.id.categories);
            viewHolder.nameAndSurnameHelpSeeker = (TextView) convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.contactBtn = (Button) convertView.findViewById(R.id.contactButton);
            viewHolder.finishBtn = (Button) convertView.findViewById(R.id.finishButton);

            viewHolder.contactBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contactListener != null) {
                        contactListener.onContactButtonClickListener(position, request);
                    }
                }
            });

            viewHolder.finishBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finishListener != null) {
                        finishListener.onFinishButtonClickListener(position, request);
                    }
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AcceptedRequestListAdapter.ViewHolder) convertView.getTag();
        }



        //viewHolder.imageView.setTag(position);
        return convertView;
    }
}
