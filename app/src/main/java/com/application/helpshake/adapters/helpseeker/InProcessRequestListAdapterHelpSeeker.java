package com.application.helpshake.adapters.helpseeker;

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
import com.application.helpshake.model.VolunteerRequest;

import java.util.ArrayList;

public class InProcessRequestListAdapterHelpSeeker extends ArrayAdapter<VolunteerRequest>  {

    public interface InProcessRequestListAdapterListener {
        void OnMarkFinishedClicked(int position, VolunteerRequest value);
        void OnContact(int position, VolunteerRequest value);
    }

    InProcessRequestListAdapterListener listener;

    private static class ViewHolder {
        Button contactBtn;
        Button finishBtn;
        TextView title;
        TextView volunteerName;
    }

    public InProcessRequestListAdapterHelpSeeker(ArrayList<VolunteerRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_in_progress_request, data);
        listener = (InProcessRequestListAdapterListener) context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final VolunteerRequest request = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_helpseeker_in_progress_request, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);

            viewHolder.finishBtn = convertView.findViewById(R.id.finishButton);
            viewHolder.contactBtn = convertView.findViewById(R.id.contactButton);
            viewHolder.volunteerName = convertView.findViewById(R.id.volunteerName);
            viewHolder.contactBtn.setEnabled(true);
            viewHolder.finishBtn.setEnabled(true);

            viewHolder.contactBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.OnContact(position, request);
                        }
                    }
                });

                viewHolder.finishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.OnMarkFinishedClicked(position, request);
                        }
                    }
                });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(request.getRequest().getTitle());
        viewHolder.volunteerName.setText(request.getVolunteer().getName());
        return convertView;
    }
}
