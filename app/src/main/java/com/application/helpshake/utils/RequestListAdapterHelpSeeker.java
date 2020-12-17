package com.application.helpshake.utils;

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
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;

import java.util.ArrayList;

public class RequestListAdapterHelpSeeker extends ArrayAdapter<HelpSeekerRequest> {

    private static class ViewHolder {
        TextView category;
        Button status_button;
    }

    public RequestListAdapterHelpSeeker(ArrayList<HelpSeekerRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_request, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        HelpSeekerRequest request = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_request, parent, false);
            viewHolder.category = (TextView) convertView.findViewById(R.id.category);
            viewHolder.status_button = (Button) convertView.findViewById(R.id.status_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        StringBuilder builder = new StringBuilder();

        int resource;

        switch(request.getStatus())
        {
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

        viewHolder.status_button.setBackgroundResource(resource);

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

        viewHolder.category.setText(builder.toString());
        //viewHolder.imageView.setTag(position);
        return convertView;
    }
}
