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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TopicsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        // Gets the Topics of position
        final Topic topic = topics.get(position);

        //holder.itemThumb.setImageResource(thumb);
        holder.topicChoose.setChecked(topic.isSubscribed());
        holder.topicTitle.setText(topic.getTitle());

        holder.topicChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    callbacks.onTopicTurnedOn(topic);
                } else {
                    callbacks.onTopicTurnedOff(topic);
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

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {

        // public ImageView topicThumb;
        public SwitchCompat topicChoose;
        public TextView topicTitle;

        /**
         * Constructor.
         * @param itemView the View to inflate.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            //itemThumb = (ImageView) view.findViewById(R.id.item_thumb);
            topicChoose = (SwitchCompat) itemView.findViewById(R.id.topic_choose);
            topicTitle = (TextView) itemView.findViewById(R.id.topic_title);
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
