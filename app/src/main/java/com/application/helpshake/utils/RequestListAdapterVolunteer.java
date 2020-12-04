package com.application.helpshake.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;

import java.util.ArrayList;

public class RequestListAdapterVolunteer extends ArrayAdapter<HelpSeekerRequest> {

    int[] photos;

    private static class ViewHolder {
        ImageView photo;
        TextView nameAndSurname;
        TextView category;
        TextView distance;
        ImageView mapPoint;
    }

    public RequestListAdapterVolunteer(ArrayList<HelpSeekerRequest> data, Context context, int[] photos) {
        super(context, R.layout.list_item_volunteer_request, data);
        this.photos = photos;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        HelpSeekerRequest request = getItem(position);
        RequestListAdapterVolunteer.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new RequestListAdapterVolunteer.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_request, parent, false);
            viewHolder.photo = (ImageView) convertView.findViewById(R.id.help_seeker_image);
            viewHolder.nameAndSurname = (TextView) convertView.findViewById(R.id.list_item_name);
            viewHolder.category = (TextView) convertView.findViewById(R.id.list_item_category);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.list_item_distance);
            viewHolder.mapPoint = (ImageView) convertView.findViewById(R.id.location);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RequestListAdapterVolunteer.ViewHolder) convertView.getTag();
        }

        StringBuilder builder = new StringBuilder();
        for (HelpCategory category : request.getHelpCategories()) {

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

        viewHolder.nameAndSurname.setText(request.getHelpSeekerName() + " " + request.getHelpSeekerSurname());
        viewHolder.category.setText(builder.toString());
        viewHolder.photo.setImageResource(photos[0]);
        viewHolder.mapPoint.setImageResource(photos[1]);
        return convertView;
    }
}

