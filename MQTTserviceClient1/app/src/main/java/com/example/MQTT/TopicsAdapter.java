package com.example.MQTT;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * The Adapter for Topics RecyclerView.
 */
public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

    private static final String SUBSCRIBED = "SUBSCRIBED";

    /**
     * Represents an instance of {@link TopicsListItemListener}.
     */
    private TopicsListItemListener listener;

    /**
     * The Topics on Adapter.
     */
    private List<Topic> topics;

    @Override
    public TopicsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        ViewHolder vh = new ViewHolder(v, listener);
        return vh;
    }

    @Override
    public void onBindViewHolder(TopicsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        // Gets the Topics of position
        final Topic topic = topics.get(position);

        //holder.itemThumb.setImageResource(thumb);
        holder.topicSubscribed.setChecked(topic.isSubscribed());
        holder.topicTitle.setText(topic.getTitle());
        holder.topicMessage.setText(topic.isSubscribed() ? SUBSCRIBED : "");
    }

    @Override
    public int getItemCount() {
        return topics == null ? 0 : topics.size();
    }

    /**
     * Load Topics in List.
     * @param topics the topics to load.
     */
    public void loadTopics(final List<Topic> topics) {
        this.topics = topics;
        notifyDataSetChanged();
    }

    /**
     * Set a listener instance on Adapter.
     * @param listener A {@link TopicsListItemListener} instance.
     */
    public void setTopicListItemListener(final TopicsListItemListener listener) {
        this.listener = listener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder /*implements
            View.OnClickListener, View.OnLongClickListener*/ {

        // public ImageView topicThumb;
        public SwitchCompat topicSubscribed;
        public TextView topicTitle;
        public TextView topicMessage;

        /**
         * The listener for Topics List item actions.
         */
        private TopicsListItemListener itemListener;

        /**
         * Constructor.
         * @param itemView the View to inflate.
         * @param itemListener The listener for Topics List item actions.
         */
        public ViewHolder(View itemView, TopicsListItemListener itemListener) {
            super(itemView);
            //itemThumb = (ImageView) view.findViewById(R.id.item_thumb);
            topicSubscribed = (SwitchCompat) itemView.findViewById(R.id.topic_subscribed);
            topicTitle = (TextView) itemView.findViewById(R.id.topic_title);
            topicMessage = (TextView) itemView.findViewById(R.id.topic_message);

            this.itemListener = itemListener;
        }

    }

    /**
     * The interface to listen the Topics list item actions.
     */
    public interface TopicsListItemListener {

        /**
         * Called when user requests a topic subscribing.
         * @param topic the topic to subscribe.
         */
        void onRequestTopicSubscribe(String topic);

    }

}
