package com.marcelorocha.MQTTSample.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.data.NotificationStorage;
import com.marcelorocha.MQTTSample.model.Notification;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * The Adapter for Notifications RecyclerView.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private DateFormat dateFormat;
    private DateFormat timeFormat;

    /**
     * The Notifications on Adapter.
     */
    private List<Notification> notifications;

    /**
     * Class Constructor.
     */
    public NotificationsAdapter(Context context) {
        // Get Date and Time format of System preferences.
        this.dateFormat = android.text.format.DateFormat.getDateFormat(context);
        this.timeFormat = android.text.format.DateFormat.getTimeFormat(context);
    }

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

        String notificationDateTime = notification.getDateTime();

        try {
            Date date = NotificationStorage.getDateFormat().parse(notificationDateTime);
            notificationDateTime = dateFormat.format(date) + " " + timeFormat.format(date);
        } catch (ParseException e) {
            Log.e(NotificationsAdapter.class.getSimpleName(),
                    "Can't format notification dateTime: " + notificationDateTime);
        }

        //holder.notificationThumb.setImageResource(thumb);
        holder.notificationDateTime.setText(notificationDateTime);
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
        public TextView notificationDateTime;

        /**
         * Constructor.
         * @param itemView the View to inflate.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            //notificationThumb = (ImageView) itemView.findViewById(R.id.notification_thumb);
            notificationDateTime = (TextView) itemView.findViewById(R.id.notification_datetime);
            notificationMessage = (TextView) itemView.findViewById(R.id.notification_message);
        }
    }

}
