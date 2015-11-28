package com.marcelorocha.MQTTSample.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.model.Notification;

import java.util.List;

/**
 * The Adapter for Notifications RecyclerView.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    /**
     * The Notifications on Adapter.
     */
    private List<Notification> notifications;

    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notifcation_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        // Gets the Topics of position
        final Notification notification = notifications.get(position);

        //holder.notificationThumb.setImageResource(thumb);
        holder.notificationMessage.setText(notification.getMessage());
    }

    @Override
    public int getItemCount() {
        return notifications == null ? 0 : notifications.size();
    }

    /**
     * Load Notifications in List.
     * @param notifications the notifications to load.
     */
    public void loadNotifications(final List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {

        // public ImageView notificationThumb;
        public TextView notificationMessage;

        /**
         * Constructor.
         * @param itemView the View to inflate.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            //notificationThumb = (ImageView) itemView.findViewById(R.id.notification_thumb);
            notificationMessage = (TextView) itemView.findViewById(R.id.notification_message);
        }
    }

}
