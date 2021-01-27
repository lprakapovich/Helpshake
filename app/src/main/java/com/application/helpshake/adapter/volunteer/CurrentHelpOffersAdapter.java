package com.application.helpshake.adapter.volunteer;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.service.MapService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CurrentHelpOffersAdapter extends ArrayAdapter<PublishedHelpRequest> {

    public interface CurrentHelpOfferListener {
        void onContact(PublishedHelpRequest request);
    }

    CurrentHelpOfferListener mListener;
    private PublishedHelpRequest request;
    private ViewHolder viewHolder;


    private static class ViewHolder {
        ImageButton callBtn;
        ImageButton mapBtn;
        TextView title;
        TextView helpSeekerName;
        TextView comment;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
        ImageView helpSeekerPic;
    }

    public CurrentHelpOffersAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_volunteer_current_offer, data);
        mListener = (CurrentHelpOfferListener) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        request = getItem(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_volunteer_current_offer, parent, false);

            viewHolder.callBtn = convertView.findViewById(R.id.callButton);
            viewHolder.helpSeekerName = convertView.findViewById(R.id.helpSeekerName);
            viewHolder.mapBtn = convertView.findViewById(R.id.showOnMapBtn);
            viewHolder.title = convertView.findViewById((R.id.title));
            viewHolder.comment = convertView.findViewById((R.id.commentText));
            viewHolder.helpSeekerPic = convertView.findViewById((R.id.helpSeekerPic));
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
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

        viewHolder.helpSeekerName.setText("Help seeker: " + request.getRequest().getHelpSeeker().getFullName());
        viewHolder.title.setText("Title: " + request.getRequest().getHelpRequest().getTitle());
        viewHolder.comment.setText("Comment: " + request.getRequest().getHelpRequest().getDescription());

        setHelpSeekerImage();

        viewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onContact(request);
            }
        });

        viewHolder.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //        TODO: Add map opening
            }
        });

        return convertView;
    }

    public void setHelpSeekerImage() {
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
    }
}
