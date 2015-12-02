package com.marcelorocha.MQTTSample.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.data.NotificationStorage;
import com.marcelorocha.MQTTSample.model.Notification;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private View mEmptyView;

    private NotificationsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    /**
     * Initialize and setup the views of Activity.
     */
    private void initViews() {
        mEmptyView = findViewById(R.id.empty_message);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notifications_list);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new NotificationsAdapter(this);

        // Configure a Adapter observer to be notified about data changes
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });

        // Set the Adapter
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Show or Hide the Empty view for this screen.
     */
    private void checkAdapterIsEmpty () {
        mEmptyView.setVisibility((mAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }

    /**
     * Load the Notifications on screen.
     */
    private void loadNotifications() {
        List<Notification> notifications = NotificationStorage.getAllNotifications();

        // Reorder the notifications by the data time of their occurrences...
        Collections.sort(notifications, new Comparator<Notification>() {
            @Override
            public int compare(Notification a, Notification b) {
                return b.getDateTime().compareTo(a.getDateTime());
            }
        });

        // Load notifications on screen
        mAdapter.loadNotifications(notifications);
    }

}
