package com.application.helpshake.adapter.volunteer;

import android.app.Activity;
import android.content.Context;
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
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.PublishedHelpRequest;

import java.util.ArrayList;

public class OpenRequestAdapterVolunteer extends ArrayAdapter<PublishedHelpRequest> {

    public interface OpenRequestAdapterListener {
        void OnDetailsClicked(PublishedHelpRequest request);
    }

    OpenRequestAdapterListener mListener;

    private static class ViewHolder {
        ImageView photo;
        TextView nameAndSurname;
        TextView category;
        Button details;
    }

    public OpenRequestAdapterVolunteer(ArrayList<PublishedHelpRequest> data, Context context, Activity activity) {
        super(context, R.layout.list_item_volunteer_open_request, data);
        mListener = (OpenRequestAdapterListener) activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);
        OpenRequestAdapterVolunteer.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new OpenRequestAdapterVolunteer.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_open_request, parent, false);
            viewHolder.photo = convertView.findViewById(R.id.help_seeker_image);
            viewHolder.nameAndSurname =  convertView.findViewById(R.id.list_item_name);
            viewHolder.category = convertView.findViewById(R.id.list_item_category);
            viewHolder.details = convertView.findViewById(R.id.details);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (OpenRequestAdapterVolunteer.ViewHolder) convertView.getTag();
        }

        StringBuilder builder = new StringBuilder();
        for (HelpCategory category : request.getRequest().getHelpRequest().getCategoryList()) {

            switch (category)
            {
                case DogWalking:
                    builder.append("#dogwalking\n");
                    break;
                case Grocery:
                    builder.append("#grocery\n");
                    break;
                case Drugstore:
                    builder.append("#drugstore\n");
                    break;
                default:
                    builder.append("#other\n");
            }
        }

        viewHolder.nameAndSurname.setText(
                String.format("%s %s", request.getRequest().getHelpSeeker().getName(),
                        request.getRequest().getHelpSeeker().getLastName()));
        viewHolder.category.setText(builder.toString());
        viewHolder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnDetailsClicked(request);
            }
        });

        return convertView;
    }
}
