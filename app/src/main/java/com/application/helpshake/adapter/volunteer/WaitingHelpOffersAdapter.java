package com.application.helpshake.adapter.volunteer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.util.DistanceEstimator;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class WaitingHelpOffersAdapter extends ArrayAdapter<PublishedHelpRequest> {

    public interface WaitingHelpOfferListener {
        void onMapClicked(PublishedHelpRequest request);
    }

    WaitingHelpOfferListener mListener;

    private static class ViewHolder {
        ImageView helpSeekerPic;
        TextView helpSeekerName;
        ImageView mapBtn;
        TextView distance;
        TextView title;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
    }

    public WaitingHelpOffersAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_volunteer_waiting_offer, data);
        mListener = (WaitingHelpOffersAdapter.WaitingHelpOfferListener) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);

        WaitingHelpOffersAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new WaitingHelpOffersAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_waiting_offer, parent, false);

            viewHolder.helpSeekerPic = convertView.findViewById((R.id.help_seeker_image));
            viewHolder.helpSeekerName = convertView.findViewById(R.id.list_item_name);
            viewHolder.mapBtn = convertView.findViewById(R.id.map);
            viewHolder.distance = convertView.findViewById(R.id.distance);
            viewHolder.title = convertView.findViewById((R.id.requestTitle));
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (WaitingHelpOffersAdapter.ViewHolder) convertView.getTag();
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

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + request.getRequest().getHelpSeeker().getUid() + ".jpeg");
        Uri imageData = Uri.parse(ref.getDownloadUrl().toString());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri)
                        .fitCenter().into(viewHolder.helpSeekerPic);
            }
        });

        viewHolder.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMapClicked(request);
            }
        });

        return convertView;
    }
}
