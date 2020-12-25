package com.application.helpshake.adapter.helpseeker;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.application.helpshake.R;
import com.application.helpshake.model.PublishedHelpRequest;

import java.util.ArrayList;

public class InProgressRequestAdapter extends ArrayAdapter<PublishedHelpRequest>  {

    public interface InProcessRequestListAdapterListener {
        void OnMarkFinished(int position, PublishedHelpRequest value);
        void OnContact(int position, PublishedHelpRequest value);
    }

    InProcessRequestListAdapterListener listener;

    /**
     * Need to add categories in a form of icons
     */
    private static class ViewHolder {
        Button contactBtn;
        Button finishBtn;
        TextView title;
        TextView volunteerName;
    }

    public InProgressRequestAdapter(ArrayList<PublishedHelpRequest> data, Context context) {
        super(context, R.layout.list_item_helpseeker_in_progress_request, data);
        listener = (InProcessRequestListAdapterListener) context;
    }

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
                            listener.OnMarkFinished(position, request);
                        }
                    }
                });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(request.getRequest().getHelpRequest().getTitle());
        viewHolder.volunteerName.setText(request.getVolunteer().getName());
        return convertView;
    }
}
