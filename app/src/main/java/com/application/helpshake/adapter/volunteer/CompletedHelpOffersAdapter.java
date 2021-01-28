package com.application.helpshake.adapter.volunteer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class CompletedHelpOffersAdapter extends ArrayAdapter<PublishedHelpRequest> {

    public interface CompletedHelpOfferListener {
        void onOfferClosed(int position, PublishedHelpRequest value);
    }

    CompletedHelpOffersAdapter.CompletedHelpOfferListener mListener;

    private static class ViewHolder {
        ImageView helpSeekerPic;
        TextView helpSeekerName;
        TextView title;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
        ImageView close;
    }

    public CompletedHelpOffersAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_volunteer_waiting_offer, data);
        mListener = (CompletedHelpOffersAdapter.CompletedHelpOfferListener) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    	   final PublishedHelpRequest request = getItem(position);
    	   CompletedHelpOffersAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new CompletedHelpOffersAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_completed_offers, parent, false);

            viewHolder.helpSeekerPic = convertView.findViewById((R.id.help_seeker_image));
            viewHolder.helpSeekerName = convertView.findViewById(R.id.list_item_name);
            viewHolder.title = convertView.findViewById((R.id.requestTitle));
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);
            viewHolder.close = convertView.findViewById(R.id.closeId);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (CompletedHelpOffersAdapter.ViewHolder) convertView.getTag();
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

        viewHolder.helpSeekerName.setText(request.getRequest().getHelpSeeker().getFullName());
        viewHolder.title.setText("Title: " + request.getRequest().getHelpRequest().getTitle());

        viewHolder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onOfferClosed(position, request);
                }
            }
        });
        setHelpSeekerImage(request);

        return convertView;
    }

    public void setHelpSeekerImage(PublishedHelpRequest request) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + request.getRequest().getHelpSeeker().getUid() + ".jpeg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri)
                        .fitCenter().into(viewHolder.helpSeekerPic);
            }
        });
    }
}
