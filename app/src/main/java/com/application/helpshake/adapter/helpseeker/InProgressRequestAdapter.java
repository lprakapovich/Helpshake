package com.application.helpshake.adapter.helpseeker;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.util.ArrayList;

public class InProgressRequestAdapter extends ArrayAdapter<PublishedHelpRequest>  {

    public interface InProcessRequestListAdapterListener {
        void onMarkFinished(int position, PublishedHelpRequest value);
        void onContact(int position, PublishedHelpRequest value);
    }

    InProcessRequestListAdapterListener listener;


    private static class ViewHolder {
        ImageButton callBtn;
        ImageButton finishBtn;
        TextView title;
        TextView volunteerName;
        TextView comment;
        CheckBox grocery;
        CheckBox dogwalking;
        CheckBox drugstore;
        CheckBox other;
    }

    public InProgressRequestAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_in_progress_request, data);
        listener = (InProcessRequestListAdapterListener) context;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final PublishedHelpRequest request = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_in_progress_request, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);

            viewHolder.finishBtn = convertView.findViewById(R.id.markAsFinishedButton);
            viewHolder.callBtn = convertView.findViewById(R.id.callButton);
            viewHolder.volunteerName = convertView.findViewById(R.id.voluneerName);
            viewHolder.callBtn.setEnabled(true);
            viewHolder.finishBtn.setEnabled(true);
            viewHolder.grocery = convertView.findViewById(R.id.grocery);
            viewHolder.dogwalking = convertView.findViewById(R.id.dogwalking);
            viewHolder.drugstore = convertView.findViewById(R.id.drugstore);
            viewHolder.other = convertView.findViewById(R.id.other);
            viewHolder.comment = convertView.findViewById(R.id.commentText);

            viewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onContact(position, request);
                        }
                    }
                });

                viewHolder.finishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onMarkFinished(position, request);
                        }
                    }
                });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //initially:
        viewHolder.grocery.setAlpha((float) 0.5);
        viewHolder.dogwalking.setAlpha((float) 0.5);
        viewHolder.drugstore.setAlpha((float) 0.5);
        viewHolder.other.setAlpha((float) 0.5);

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

        viewHolder.title.setText("Title: " + request.getRequest().getHelpRequest().getTitle());
        viewHolder.volunteerName.setText("Voluneer: " + request.getVolunteer().getFullName());
        viewHolder.comment.setText("Your comment: " + request.getRequest().getHelpRequest().getDescription());
        return convertView;
    }
}
