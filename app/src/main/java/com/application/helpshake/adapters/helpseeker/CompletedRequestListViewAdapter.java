package com.application.helpshake.adapters.helpseeker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.model.VolunteerRequest;

import java.util.ArrayList;

public class CompletedRequestListViewAdapter extends ArrayAdapter<VolunteerRequest> {

    private static class ViewHolder {
        TextView title;
        TextView volunteerName;
    }

    public CompletedRequestListViewAdapter(@NonNull ArrayList<VolunteerRequest> data, Context context){
        super(context, R.layout.list_item_helpseeker_completed_request, data);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final VolunteerRequest request = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_in_progress_request, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.volunteerName = convertView.findViewById(R.id.volunteerName);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(request.getRequest().getTitle());
        viewHolder.volunteerName.setText(request.getVolunteer().getName());
        return convertView;
    }


}
