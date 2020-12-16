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
import com.application.helpshake.model.Status;

import java.util.ArrayList;

public class RequestListAdapter extends ArrayAdapter<HelpSeekerRequest> {

    finishButtonListener finishListener;
    contactButtonListener contactListener;

    public interface finishButtonListener {
        public void onFinishButtonClickListener(int position, HelpSeekerRequest value);
    }

    public interface contactButtonListener {
        public void onContactButtonClickListener(int position, HelpSeekerRequest value);
    }

    public void setFinishButtonListener(finishButtonListener listener) {
        this.finishListener = listener;
    }

    public void setContactButtonListener(contactButtonListener listener) {
        this.contactListener = listener;
    }

    private static class ViewHolder {
        TextView category;
        TextView status;
        Button contactBtn;
        Button finishBtn;
    }

    public RequestListAdapter(ArrayList<HelpSeekerRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_request, data);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final HelpSeekerRequest request = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_request, parent, false);
            viewHolder.category = (TextView) convertView.findViewById(R.id.category);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            viewHolder.contactBtn = (Button) convertView.findViewById(R.id.contactButton);
            viewHolder.finishBtn = (Button) convertView.findViewById(R.id.finishButton);

            if (request.getStatus() == Status.InProgress) {
                viewHolder.contactBtn.setVisibility(View.VISIBLE);
                viewHolder.finishBtn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.contactBtn.setVisibility(View.INVISIBLE);
                viewHolder.finishBtn.setVisibility(View.INVISIBLE);
            }
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
            viewHolder = (ViewHolder) convertView.getTag();
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

        viewHolder.status.setText(request.getStatus().toString());
        viewHolder.category.setText(builder.toString());
        //viewHolder.imageView.setTag(position);
        return convertView;
    }
}
