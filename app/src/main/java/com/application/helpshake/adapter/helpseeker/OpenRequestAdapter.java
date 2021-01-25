package com.application.helpshake.adapter.helpseeker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.enums.HelpCategory;

import java.util.ArrayList;

import lombok.NonNull;

public class OpenRequestAdapter extends ArrayAdapter<PublishedHelpRequest> {


    private ViewHolder viewHolder;

    private static class ViewHolder {
        TextView title;
        Button status;
        TextView comment;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
    }

    public OpenRequestAdapter(ArrayList<PublishedHelpRequest> requests, Context context) {
        super(context, R.layout.list_item_helpseeker_open_request, requests);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @androidx.annotation.NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_open_request, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.status = convertView.findViewById(R.id.status_button);
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);
            viewHolder.comment = convertView.findViewById(R.id.commentTextOpen);

            //initially:
            viewHolder.grocery.setAlpha((float) 0.5);
            viewHolder.dogwalking.setAlpha((float) 0.5);
            viewHolder.drugstore.setAlpha((float) 0.5);
            viewHolder.other.setAlpha((float) 0.5);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int resource;

        switch (request.getStatus()) {
            case Open:
                resource = R.drawable.status_open;
                break;
            case WaitingForApproval:
                resource = R.drawable.status_pending;
                break;
            case InProgress:
                resource = R.drawable.status_in_progress;
                break;
            default:
                resource = R.drawable.status_completed;
        }

        viewHolder.status.setBackgroundResource(resource);

        for (HelpCategory category : request.getRequest().getHelpRequest().getCategoryList()) {
            switch (category) {//ISSUE - when scrolling the list on helpseeker buttons highlighting changes in different ways
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

        viewHolder.title.setText(request.getRequest().getHelpRequest().getTitle());
        viewHolder.comment.setText(request.getRequest().getHelpRequest().getDescription());
        return convertView;
    }

}
