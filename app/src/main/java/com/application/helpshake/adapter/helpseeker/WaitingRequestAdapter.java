package com.application.helpshake.adapter.helpseeker;

import android.annotation.SuppressLint;
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
import com.application.helpshake.model.request.PublishedHelpRequest;

import java.util.ArrayList;

public class WaitingRequestAdapter extends ArrayAdapter<PublishedHelpRequest> {

    public interface OfferListAdapterListener {
        void onHelpAccepted(int position, PublishedHelpRequest request);
        void onHelpDeclined(int position, PublishedHelpRequest request);
    }

    OfferListAdapterListener mListener;

    private static class ViewHolder {
        TextView fullName;
        TextView distance;
        ImageView mapPoint;
        Button acceptButton;
        Button rejectButton;
        TextView infoText;
        TextView title;
    }

    public WaitingRequestAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_waiting_request, data);
        mListener = (OfferListAdapterListener) context;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final PublishedHelpRequest request = getItem(position);

        WaitingRequestAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new WaitingRequestAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_waiting_request, parent, false);
            viewHolder.fullName =  convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.infoText =  convertView.findViewById(R.id.commentText);
            viewHolder.acceptButton = convertView.findViewById(R.id.acceptBtn);
            viewHolder.rejectButton = convertView.findViewById(R.id.rejectBtn);
            viewHolder.title = convertView.findViewById(R.id.requestTitle);

            viewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onHelpAccepted(position, request);
                }
            });

            viewHolder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onHelpDeclined(position, request);
                }
            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (WaitingRequestAdapter.ViewHolder) convertView.getTag();
        }

         viewHolder.fullName.setText(request.getVolunteer().getFullName());
         viewHolder.title.setText("Request: " + request.getRequest().getHelpRequest().getTitle());
         viewHolder.infoText.setText("Your comment: " + request.getRequest().getHelpRequest().getDescription());

        return convertView;
    }
}
