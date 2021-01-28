package com.application.helpshake.adapter.helpseeker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.request.PublishedHelpRequest;

import java.util.ArrayList;

public class CompletedRequestAdapter extends ArrayAdapter<PublishedHelpRequest> {

    private static class ViewHolder {
        TextView title;
        TextView volunteerName;
        TextView completionDate;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
        RatingBar ratingBar;
    }

    public CompletedRequestAdapter(@NonNull ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_completed_request, data);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_completed_request, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.volunteerName = convertView.findViewById(R.id.helpSeekerName);
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);
            viewHolder.ratingBar = convertView.findViewById(R.id.rating_bar);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //initially:
        viewHolder.grocery.setAlpha((float) 0.25);
        viewHolder.dogwalking.setAlpha((float) 0.25);
        viewHolder.drugstore.setAlpha((float) 0.25);
        viewHolder.other.setAlpha((float) 0.25);

        for (HelpCategory category : request.getRequest().getHelpRequest().getCategoryList()) {
            switch (category) {
                case DogWalking:
                    viewHolder.dogwalking.setAlpha((float) 1.0);
                    break;
                case Grocery:
                    viewHolder.grocery.setAlpha((float) 1.0);
                    break;
                case Drugstore:
                    viewHolder.drugstore.setAlpha((float) 1.0);
                    break;
                case Other:
                    viewHolder.other.setAlpha((float) 1.0);
                    break;
            }
        }

        viewHolder.title.setText("Title: " + request.getRequest().getHelpRequest().getTitle());
        viewHolder.volunteerName.setText("Volunteer: " + request.getVolunteer().getFullName());
        viewHolder.ratingBar.setRating(request.getRatings());
        return convertView;
    }
}
