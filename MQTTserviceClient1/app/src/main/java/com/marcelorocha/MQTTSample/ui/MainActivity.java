package com.marcelorocha.MQTTSample.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.marcelorocha.MQTTSample.MQTTClientApp;
import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.model.Topic;
import com.marcelorocha.MQTTSample.network.MQTTservice;
import com.marcelorocha.MQTTSample.network.WebServices;
import com.marcelorocha.MQTTSample.util.DeviceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements
        TopicsAdapter.Callbacks, PublishDialog.Callbacks {

    /**
     * The Intent Filter action for the "PushReceiver" Broadcast Receiver.
     */
    private static final String PUSH_RECEIVED_ACTION = "com.marcelorocha.MQTTSample.PushReceived";

    private View mMainLayout;
    private View mLoadingView;
    private View mEmptyView;

    private TopicsAdapter mAdapter;

    /**
     * Represents the list of topics on server.
     */
    private List<Topic> topics = new ArrayList<>();

	private Messenger service = null;
	private Messenger serviceHandler = null;
	private IntentFilter intentFilter = null;
	private PushReceiver pushReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMQQTService();
    }

    @Override
	protected void onStart() {
    	super.onStart();
    	bindService(new Intent(this, MQTTservice.class), serviceConnection, 0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(serviceConnection);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(pushReceiver, intentFilter);
        getTopicsOnServer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(pushReceiver);
	}

    /**
     * Perform MQTT Service initialization.
     */
    private void initMQQTService() {
        serviceHandler = new Messenger(new ServiceHandler(this));

        intentFilter = new IntentFilter();
        intentFilter.addAction(PUSH_RECEIVED_ACTION);
        pushReceiver = new PushReceiver();
        registerReceiver(pushReceiver, intentFilter, null, null);

        startService(new Intent(this, MQTTservice.class));
    }

	public class PushReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent i) {
			String topic = i.getStringExtra(MQTTservice.TOPIC);
			String message = i.getStringExtra(MQTTservice.MESSAGE);
			Toast.makeText(context, "Push message received - " + topic + ":" + message, Toast.LENGTH_LONG).show();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_notification:
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            case R.id.action_publish:
                showPublishDialog();
                return true;
            default:
                // Do nothing...
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			service = new Messenger(binder);
			Bundle data = new Bundle();
			//data.putSerializable(MQTTservice.CLASSNAME, MainActivity.class);
			data.putCharSequence(MQTTservice.INTENT_NAME, PUSH_RECEIVED_ACTION);
			Message msg = Message.obtain(null, MQTTservice.REGISTER);
			msg.setData(data);
			msg.replyTo = serviceHandler;
			try {
				service.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
    };

    /**
     * Initialize and setup the views of Activity.
     */
    private void initViews() {
        mMainLayout = findViewById(R.id.main_layout);
        mLoadingView = findViewById(R.id.loading_layout);
        mEmptyView = findViewById(R.id.empty_message);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.topics_list);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new TopicsAdapter(this);

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
        if (mAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mLoadingView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTopicTurnedOn(final Topic topic) {
        Bundle data = new Bundle();
        data.putCharSequence(MQTTservice.TOPIC, topic.getTitle());
        Message msg = Message.obtain(null, MQTTservice.SUBSCRIBE);
        msg.setData(data);
        msg.replyTo = serviceHandler;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            Log.e("MQTTSample", e.getMessage());
            Snackbar
                .make(mMainLayout, "Subscribe failed with exception:" + e.getMessage(), Snackbar.LENGTH_LONG)
                .show();
        }
    }

    @Override
    public void onTopicTurnedOff(final Topic topic) {
        Bundle data = new Bundle();
        data.putCharSequence(MQTTservice.TOPIC, topic.getTitle());
        Message msg = Message.obtain(null, MQTTservice.UNSUBSCRIBE);
        msg.setData(data);
        msg.replyTo = serviceHandler;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            Log.e("MQTTSample", e.getMessage());
            Snackbar
                .make(mMainLayout, "Unsubscribe failed with exception:" + e.getMessage(), Snackbar.LENGTH_LONG)
                .show();
        }
    }

    /**
     * Show the Publish dialog on screen.
     */
	private void showPublishDialog() {
        PublishDialog dialog = new PublishDialog(this, topics, this);
        dialog.show();
	}

    @Override
    public void onSendMessage(String message, List<Topic> topics) {
        // Publish topic by topic
        for (Topic topic : topics) {
            Bundle data = new Bundle();
            data.putCharSequence(MQTTservice.TOPIC, topic.getTitle());
            data.putCharSequence(MQTTservice.MESSAGE, message);
            Message msg = Message.obtain(null, MQTTservice.PUBLISH);
            msg.setData(data);
            msg.replyTo = serviceHandler;
            try {
                service.send(msg);
            } catch (RemoteException e) {
                Log.e("MQTTSample", e.getMessage());

                String errorMessage = String.format("Publishing on topic \"%s\" failed " +
                        "with exception: %s", topic,e.getMessage());

                Snackbar
                    .make(mMainLayout, errorMessage, Snackbar.LENGTH_LONG)
                    .show();
            }
        }
    }

    /**
     * Instances of static inner classes do not hold an implicit
     * reference to their outer class.
     */
    private static class ServiceHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        public ServiceHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

	    @Override
	    public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            if (b != null) {
                Boolean result = b.getBoolean(MQTTservice.STATUS);
                String action = "";

                switch (msg.what) {
                    case MQTTservice.SUBSCRIBE:
                        action = "Subscribe";
                        onSubscribe(result, b);
                        // Checking the client id to avoid unnecessary feedback
                        String clientId = b.getString(MQTTservice.CLIENT_ID);
                        if (DeviceUtil.getClientId().equals(clientId)) {
                            return;
                        }
                        break;
                    case MQTTservice.UNSUBSCRIBE:
                        action = "Unsubscribe";
                        onUnsubscribe(result, b);
                        break;
                    case MQTTservice.PUBLISH:
                        action = "Publish";
                        break;
                    case MQTTservice.REGISTER:
                        action = "Register";
                        onRegister(result, b);
                        break;
                    case MQTTservice.SUBSCRIBE_LIST:
                        onSubscribeList(result, b);
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }

                MainActivity activity = mActivity.get();
                View view = activity.mMainLayout;

                Snackbar
                    .make(view, action + (!result ? " fail" : " success"), Snackbar.LENGTH_LONG)
                    .show();
            }

        }

        /**
         * Handle Subscribe message.
         * @param success true if the result of operation was success.
         * @param data The data received from Service.
         */
        private void onSubscribe(boolean success, Bundle data) {
            if (success) {
                String action = "Subscribe";
                MainActivity activity = mActivity.get();

                String clientId = data.getString(MQTTservice.CLIENT_ID);
                String title = data.getString(MQTTservice.TOPIC);

                Topic topic = new Topic();
                topic.setTitle(title);

                int topicPos = activity.topics.indexOf(topic);
                Topic topicInList = activity.topics.get(topicPos);

                if (topicInList.isSubscribed()) {
                    return; // Topic is already subscribed on Server
                }

                // Update topic on service
                WebServices webServices = MQTTClientApp.getWebServices();
                webServices.sendSubscribe(clientId, title, new WebServiceResponse(action));

                // Update topics list
                topic.setSubscribed(true);
                activity.topics.set(topicPos, topic);
            }
        }

        /**
         * Handle Unsubscribe message.
         * @param success true if the result of operation was success.
         * @param data The data received from Service.
         */
        private void onUnsubscribe(boolean success, Bundle data) {
            if (success) {
                String action = "Unsubscribe";
                MainActivity activity = mActivity.get();

                String clientId = data.getString(MQTTservice.CLIENT_ID);
                String title = data.getString(MQTTservice.TOPIC);

                Topic topic = new Topic();
                topic.setTitle(title);

                int topicPos = activity.topics.indexOf(topic);
                Topic topicInList = activity.topics.get(topicPos);

                if (!topicInList.isSubscribed()) {
                    return; // Topic is already unsubscribed on Server
                }

                // Update topic on service
                WebServices webServices = MQTTClientApp.getWebServices();
                webServices.sendUnsubscribe(clientId, title, new WebServiceResponse(action));

                // Update topics list
                topic.setSubscribed(false);
                activity.topics.set(activity.topics.indexOf(topic), topic);
            }
        }

        /**
         * Handle Register message.
         * @param success true if the result of operation was success.
         * @param data The data received from Service.
         */
        private void onRegister(boolean success, Bundle data) {
            if (success) {
                MainActivity activity = mActivity.get();
                // Resubscribe topics subscribed
                ArrayList<String> subscribedTopics = new ArrayList<>();

                for (Topic topic : activity.topics) {
                    if (topic.isSubscribed()) {
                        subscribedTopics.add(topic.getTitle());
                    }
                }

                if (!subscribedTopics.isEmpty()) {
                    Bundle mData = new Bundle();
                    mData.putStringArrayList(MQTTservice.TOPICS_LIST, subscribedTopics);
                    Message msg = Message.obtain(null, MQTTservice.SUBSCRIBE_LIST);
                    msg.setData(mData);
                    msg.replyTo = activity.serviceHandler;
                    try {
                        activity.service.send(msg);
                    } catch (RemoteException e) {
                        Log.e("MQTTSample", e.getMessage());
                    }
                }
            }
        }

        /**
         * Handle Subscribe List message.
         * @param success true if the result of operation was success.
         * @param data The data received from Service.
         */
        private void onSubscribeList(boolean success, Bundle data) {
            if (success) {
                List<String> subscribedTopics = data.getStringArrayList(MQTTservice.TOPICS_SUBSCRIBED);
                if (subscribedTopics != null && !subscribedTopics.isEmpty()) {
                    StringBuilder strTopics = new StringBuilder();
                    Iterator i = subscribedTopics.iterator();
                    for (;;) {
                        strTopics.append(i.next());
                        if (!i.hasNext()) break;
                        strTopics.append(", ");
                    }
                    Log.i(MQTTservice.TOPICS_SUBSCRIBED,
                            "Topics " + strTopics.toString() + " resubscribed with success !!!");
                }
            } else {
                Log.e(MQTTservice.TOPICS_SUBSCRIBED,
                        "An error occurs when tried resubscribe topics subscribed on Server !!!");
            }
        }

        /**
         * Response callback for Subscribe/Unsubscribe requests.
         */
        private class WebServiceResponse implements retrofit.Callback<Void> {

            private String action;

            public WebServiceResponse(String action) {
                this.action = action;
            }

            @Override
            public void success(Void result, Response response) {
                Log.i(WebServiceResponse.class.getSimpleName(), action + " success !!!");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(WebServiceResponse.class.getSimpleName(),
                        action + "failure !!!, reason = " + error.getLocalizedMessage());
            }

        }

    }

    /**
     * Execute the GET Topics request.
     */
    private void getTopicsOnServer() {
        // 1. Show loader if necessary
        mEmptyView.setVisibility(View.GONE);

        if (topics.isEmpty()) {
            mLoadingView.setVisibility(View.VISIBLE);
        }

        // 2. Execute the request
        final WebServices webServices = MQTTClientApp.getWebServices();
        webServices.getTopics(DeviceUtil.getClientId(), new GetTopicsResponse());
    }

    /**
     * Response callback for GET Topics request.
     */
    private class GetTopicsResponse implements Callback<List<Topic>> {

        @Override
        public void success(List<Topic> result, Response response) {
            // Update list of topics
            topics = result;

            mAdapter.loadTopics(topics);
            mLoadingView.setVisibility(View.GONE);
        }

        @Override
        public void failure(RetrofitError error) {
            Snackbar
                .make(mMainLayout, error.getLocalizedMessage(), Snackbar.LENGTH_LONG)
              //.setAction(R.string.snackbar_action, myOnClickListener)
                .show();

            mAdapter.loadTopics(topics);
            mLoadingView.setVisibility(View.GONE);

            if (topics.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

    }

}
