package com.application.helpshake.adapter.helpseeker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class WaitingRequestAdapter extends ArrayAdapter<PublishedHelpRequest> {

    public interface OfferListAdapterListener {
        void onHelpAccepted(int position, PublishedHelpRequest request);

        void onHelpDeclined(int position, PublishedHelpRequest request);
    }

    OfferListAdapterListener mListener;
    WaitingRequestAdapter.ViewHolder viewHolder;

    private static class ViewHolder {
        TextView fullName;
        TextView distance;
        ImageView mapPoint;
        Button acceptButton;
        Button rejectButton;
        TextView infoText;
        TextView title;
        ImageView volunteerPhoto;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
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

        if (convertView == null) {
            viewHolder = new WaitingRequestAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_waiting_request, parent, false);
            viewHolder.fullName = convertView.findViewById(R.id.nameAndSurnameText);
            viewHolder.infoText = convertView.findViewById(R.id.commentText);
            viewHolder.acceptButton = convertView.findViewById(R.id.acceptBtn);
            viewHolder.rejectButton = convertView.findViewById(R.id.rejectBtn);
            viewHolder.title = convertView.findViewById(R.id.requestTitle);
            viewHolder.volunteerPhoto = convertView.findViewById(R.id.helpSeekerPic);
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (WaitingRequestAdapter.ViewHolder) convertView.getTag();
        }

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

        viewHolder.fullName.setText("Volunteer: " + System.lineSeparator() + request.getVolunteer().getFullName());
        viewHolder.title.setText("Title: " + request.getRequest().getHelpRequest().getTitle());
        viewHolder.infoText.setText("Your comment: " + request.getRequest().getHelpRequest().getDescription());

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

        return convertView;
    }

    public void setVolunteerImage(PublishedHelpRequest request) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + request.getVolunteer().getUid() + ".jpeg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri)
                        .fitCenter().into(viewHolder.volunteerPhoto);
            }
        });
    }

}
