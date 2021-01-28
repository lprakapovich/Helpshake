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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.request.UserHelpRequest;
import com.application.helpshake.model.enums.HelpCategory;
import com.application.helpshake.util.DistanceEstimator;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class OpenRequestAdapterVolunteer extends ArrayAdapter<PublishedHelpRequest> {

    public interface OpenRequestAdapterListener {
        void onDetails(PublishedHelpRequest request);
    }

    private OpenRequestAdapterListener mListener;
    private HashMap<String, Float> mMappedDistances;

    private static class ViewHolder {
        ImageView photo;
        TextView fullName;
        CheckBox grocery;
        CheckBox dogWalking;
        CheckBox drugstore;
        CheckBox other;
        TextView distance;
        TextView title;
    }

    public OpenRequestAdapterVolunteer(ArrayList<PublishedHelpRequest> data, HashMap<String, Float> mappedDistances, Context context) {
        super(context, R.layout.list_item_volunteer_open_request, data);
        mListener = (OpenRequestAdapterListener) context;
        mMappedDistances = mappedDistances;
    }

    @SuppressLint("SetTextI18n")
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
            viewHolder.fullName = convertView.findViewById(R.id.list_item_name);
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogWalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);
            viewHolder.distance = convertView.findViewById(R.id.distance);
            viewHolder.title = convertView.findViewById(R.id.requestTitle);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (OpenRequestAdapterVolunteer.ViewHolder) convertView.getTag();
        }
        viewHolder.photo.setImageResource(R.drawable.empty_profile);
        viewHolder.grocery.setAlpha((float) 0.25);
        viewHolder.dogWalking.setAlpha((float) 0.25);
        viewHolder.drugstore.setAlpha((float) 0.25);
        viewHolder.other.setAlpha((float) 0.25);

        assert request != null;
        UserHelpRequest userHelpRequest = request.getRequest();

        for (HelpCategory category : request.getRequest().getHelpRequest().getCategoryList()) {
            switch (category) {
                case DogWalking:
                    viewHolder.dogWalking.setAlpha((float) 1.0);
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

        viewHolder.title.setText("Title: " + userHelpRequest.getHelpRequest().getTitle());
        viewHolder.fullName.setText("Help seeker: " + System.lineSeparator() + userHelpRequest.getHelpSeeker().getFullName());
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + userHelpRequest.getHelpSeeker().getUid() + ".jpeg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri)
                        .fitCenter().into(viewHolder.photo);
            }
        });

        Float distance = mMappedDistances.get(request.getUid());
        assert distance != null;
        Pair<Integer, String> parsedDistance = DistanceEstimator.parseDistance(distance);
        viewHolder.distance.setText(parsedDistance.first + parsedDistance.second + " from you");
        return convertView;
    }
}
