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
import com.google.common.base.Optional;

import org.w3c.dom.Text;
import com.application.helpshake.model.Status;

import java.util.ArrayList;

public class RequestListAdapterHelpSeeker extends ArrayAdapter<HelpSeekerRequest> {

    finishButtonListener finishListener;
    contactButtonListener contactListener;

    public interface finishButtonListener {
        void onFinishButtonClickListener(int position, HelpSeekerRequest value);
    }

    public interface contactButtonListener {
        void onContactButtonClickListener(int position, HelpSeekerRequest value);
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
        TextView title;
        Button status_button;
    }

    public RequestListAdapterHelpSeeker(ArrayList<HelpSeekerRequest> data, Context context) {
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
            viewHolder.status_button = (Button) convertView.findViewById(R.id.status_button);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.category = (TextView) convertView.findViewById(R.id.category);
            viewHolder.status_button = (Button) convertView.findViewById(R.id.status_button);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.finishBtn = (Button) convertView.findViewById(R.id.finishButton);

            if (request.getStatus() == Status.InProgress) {
                viewHolder.contactBtn.setEnabled(true);
                viewHolder.finishBtn.setEnabled(true);
            } else {
                viewHolder.contactBtn.setEnabled(false);
                viewHolder.finishBtn.setEnabled(false);
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

        int resource;

        switch(request.getStatus())
        {
            case Open:
                resource = R.drawable.status_open;
                break;
            case WaitingForApproval:
                resource = R.drawable.status_pending;
                break;
            case InProgress:
                resource = R.drawable.status_in_progress;
                break;
            default:
                resource = R.drawable.status_completed;
        }

        viewHolder.status_button.setBackgroundResource(resource);

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

        viewHolder.category.setText(builder.toString());
        viewHolder.title.setText(request.getTitle());
        return convertView;
    }
}
