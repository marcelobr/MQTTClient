package com.marcelorocha.MQTTSample.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class PublishDialog implements TopicsAdapter.Callbacks {

    private Context context;
    private View dialogView;
    private EditText edtMessage;
    private AlertDialog alertDialog;

    /**
     * Represents the list of chosen Topics to send the message.
     */
    private List<Topic> chosenTopics = new ArrayList<>();

    /**
     * Represents an instance of {@link Callbacks}.
     */
    private Callbacks callbacks;

    /**
     * Constructor.
     * @param context The context of parent view.
     * @param topics The list of Topics to show on dialog.
     * @param callbacksImpl The {@link Callbacks} implementation.
     */
    public PublishDialog(Context context, List<Topic> topics, Callbacks callbacksImpl) {
        this.context = context;
        this.callbacks = callbacksImpl;

        initView(topics);

        alertDialog = new AlertDialog.Builder(context)
            .setTitle(R.string.action_publish)
            .setView(dialogView)
            .setPositiveButton(R.string.action_send, onActionSend)
            .setNegativeButton(R.string.action_cancel, onActionCancel)
            .create();
    }

    /**
     * The callback for action send click.
     */
    private DialogInterface.OnClickListener onActionSend = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            String message = edtMessage.getText().toString();

            if (message.isEmpty()) {
                Toast.makeText(context, R.string.no_topic_selected, Toast.LENGTH_LONG).show();
                return; // Exits from this method.
            }

            if (chosenTopics.isEmpty()) {
                Toast.makeText(context, R.string.no_topic_selected, Toast.LENGTH_LONG).show();
                return; // Exits from this method.
            }

            callbacks.onSendMessage(message, chosenTopics);
        }
    };

    /**
     * The callback for action cancel click.
     */
    private DialogInterface.OnClickListener onActionCancel = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            alertDialog.dismiss();
        }
    };

    /**
     * Setup and initialize the dialog view.
     * @param topics The list of Topics to show on dialog.
     */
    private void initView(List<Topic> topics) {
        dialogView = View.inflate(context, R.layout.publish_dialog, null);

        edtMessage = (EditText) dialogView.findViewById(R.id.message);

        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.topics_list);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set the Adapter
        TopicsAdapter adapter = new TopicsAdapter(this);
        adapter.loadTopics(topics);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onTopicTurnedOn(final Topic topic) {
        chosenTopics.add(topic);
    }

    @Override
    public void onTopicTurnedOff(final Topic topic) {
        chosenTopics.remove(topic);
    }

    /**
     * Show the PublishDialog.
     */
    public void show() {
        alertDialog.show();
    }

    /**
     * The interface to listen the {@link PublishDialog} actions.
     */
    public interface Callbacks {

        /**
         * Called when user turned on a Topic.
         * @param message The message to send.
         * @param topics the list of topics to send the message.
         */
        void onSendMessage(String message, List<Topic> topics);

    }

}
