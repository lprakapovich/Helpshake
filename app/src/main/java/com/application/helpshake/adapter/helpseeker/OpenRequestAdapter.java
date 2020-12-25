package com.application.helpshake.adapter.helpseeker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.application.helpshake.R;
import com.application.helpshake.model.PublishedHelpRequest;
import com.application.helpshake.model.enums.HelpCategory;

import java.util.ArrayList;

import lombok.NonNull;

public class OpenRequestAdapter extends ArrayAdapter<PublishedHelpRequest> {

    /**
     * Need to add categories in a form of icons
     */
    private static class ViewHolder {
        TextView title;
        TextView category;
        Button status;
    }

    public OpenRequestAdapter(ArrayList<PublishedHelpRequest> requests, Context context) {
        super(context, R.layout.list_item_helpseeker_open_request, requests);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @androidx.annotation.NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_open_request, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.category = convertView.findViewById(R.id.categories);
            viewHolder.status = convertView.findViewById(R.id.status_button);
            convertView.setTag(viewHolder);
        }
        else {
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

        viewHolder.status.setBackgroundResource(resource);

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

            viewHolder.category.setText(builder.toString());
        }

        viewHolder.title.setText(request.getRequest().getHelpRequest().getTitle());
        return convertView;
    }
}
