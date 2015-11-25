package com.marcelorocha.MQTTSample.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.model.Topic;

import java.util.List;

/**
 * The Adapter for Topics RecyclerView.
 */
public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

    private static final String SUBSCRIBED = "SUBSCRIBED";

    /**
     * Represents an instance of {@link Callbacks}.
     */
    private Callbacks callbacks;

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
        return new ViewHolder(v);
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

        holder.topicSubscribed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    callbacks.onSubscribeTopic(topic.getTitle());
                }
            }
        });
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
     * Set an {@link Callbacks} implementation on Adapter.
     * @param callbacksImpl An {@link Callbacks} instance.
     */
    public void setCallbacksImpl(final Callbacks callbacksImpl) {
        this.callbacks = callbacksImpl;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {

        // public ImageView topicThumb;
        public SwitchCompat topicSubscribed;
        public TextView topicTitle;
        public TextView topicMessage;

        /**
         * Constructor.
         * @param itemView the View to inflate.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            //itemThumb = (ImageView) view.findViewById(R.id.item_thumb);
            topicSubscribed = (SwitchCompat) itemView.findViewById(R.id.topic_subscribed);
            topicTitle = (TextView) itemView.findViewById(R.id.topic_title);
            topicMessage = (TextView) itemView.findViewById(R.id.topic_message);
        }
    }

    /**
     * The interface to listen the Topics list item actions.
     */
    public interface Callbacks {

        /**
         * Called when user requests a topic subscribing.
         * @param topic the topic to subscribe.
         */
        void onSubscribeTopic(String topic);

    }

}
