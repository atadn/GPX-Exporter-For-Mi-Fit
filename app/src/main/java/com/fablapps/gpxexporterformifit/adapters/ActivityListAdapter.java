package com.fablapps.gpxexporterformifit.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;


import com.fablapps.gpxexporterformifit.R;
import com.fablapps.gpxexporterformifit.helpers.ToolHelper;
import com.fablapps.gpxexporterformifit.models.DataActivity;

import java.util.List;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ViewHolder> {

    private final List<DataActivity> mData;
    private Context context;

    public ActivityListAdapter(List<DataActivity> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_summaries, parent, false);

        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ActivityListAdapter.ViewHolder holder, final int position) {
        final DataActivity data = mData.get(position);

        String eventType = ToolHelper.getEventTypeString(context, data.getType()) + " " + ToolHelper.getEventDistance(data.getDistance());

        holder.activityTypeAndDistance.setText(eventType);
        holder.activitySavedTime.setText(ToolHelper.getSavedTimeHours(context, data.getTrackId()));
        holder.activityUsedTime.setText(ToolHelper.getActivityUsedTime(data.getTrackId(), data.getEndTime()));
        holder.activityTypeIcon.setImageResource(ToolHelper.getEventTypeImage(data.getType()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView activityTypeAndDistance, activitySavedTime, activityUsedTime;
        private final ImageView activityTypeIcon;

        ViewHolder(View itemView) {
            super(itemView);

            activityTypeAndDistance = itemView.findViewById(R.id.text_activity_type_distance);
            activitySavedTime = itemView.findViewById(R.id.text_activity_saved_time);
            activityUsedTime = itemView.findViewById(R.id.text_activity_used_time);
            activityTypeIcon = itemView.findViewById(R.id.image_activity_type);
        }
    }
}

