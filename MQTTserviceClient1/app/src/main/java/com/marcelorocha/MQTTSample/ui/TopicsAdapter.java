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

    /**
     * The Topics on Adapter.
     */
    private List<Topic> topics;

    /**
     * Represents an instance of {@link Callbacks}.
     */
    private Callbacks callbacks;

    /**
     * Constructor.
     * @param callbacksImpl An {@link Callbacks} instance.
     */
    public TopicsAdapter(final Callbacks callbacksImpl) {
        this.callbacks = callbacksImpl;
    }

    @Override
    public TopicsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        return new ViewHolder(v, callbacks);
    }

    @Override
    public void onBindViewHolder(TopicsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.setItem(topics.get(position));
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

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder implements
            CompoundButton.OnCheckedChangeListener {

        // private ImageView topicThumb;
        private SwitchCompat topicChoose;
        private TextView topicTitle;

        private Topic topic;
        private Callbacks callbacks;

        /**
         * Constructor.
         * @param itemView the View to inflate.
         */
        public ViewHolder(View itemView, Callbacks callbacks) {
            super(itemView);
            //topicThumb = (ImageView) itemView.findViewById(R.id.topic_thumb);
            topicTitle = (TextView) itemView.findViewById(R.id.topic_title);
            topicChoose = (SwitchCompat) itemView.findViewById(R.id.topic_choose);
            topicChoose.setOnCheckedChangeListener(this);
            this.callbacks = callbacks;
        }

        public void setItem(Topic topic) {
            this.topic = topic;

            topicTitle.setText(topic.getTitle());
            topicChoose.setChecked(topic.isSubscribed());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                callbacks.onTopicTurnedOn(topic);
            } else {
                callbacks.onTopicTurnedOff(topic);
            }
        }

    }

    /**
     * The interface to listen the Topics list item actions.
     */
    public interface Callbacks {

        /**
         * Called when user turned on a Topic.
         * @param topic the topic turned on.
         */
        void onTopicTurnedOn(Topic topic);

        /**
         * Called when user turned off a Topic.
         * @param topic the topic turned off.
         */
        void onTopicTurnedOff(Topic topic);

    }

}
