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

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements TopicsAdapter.Callbacks {

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

        serviceHandler = new Messenger(new ServiceHandler(mMainLayout));

    	intentFilter = new IntentFilter();
    	intentFilter.addAction(PUSH_RECEIVED_ACTION);
    	pushReceiver = new PushReceiver();
        registerReceiver(pushReceiver, intentFilter, null, null);

        startService(new Intent(this, MQTTservice.class));
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
        int id = item.getItemId();
        if (id == R.id.action_publish) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			service = new Messenger(binder);
			Bundle data = new Bundle();
			//data.putSerializable(MQTTservice.CLASSNAME, MainActivity.class);
			data.putCharSequence(MQTTservice.INTENTNAME, PUSH_RECEIVED_ACTION);
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

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.topics_list);
        mAdapter = new TopicsAdapter();
        mAdapter.setCallbacksImpl(this);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configure a Adapter observer to be notified about data changes
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });

        // Set the Adapter
        mRecyclerView.setAdapter(mAdapter);
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
    public void onSubscribeTopic(final String topic) {
        Bundle data = new Bundle();
        data.putCharSequence(MQTTservice.TOPIC, topic);
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

	private void addPublishButtonListener() {
//	    Button publishButton = (Button) findViewById(R.id.buttonPublish);
//	    publishButton.setOnClickListener(new OnClickListener() {
//	    	InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//	    	@Override
//			public void onClick(View arg0) {
//	    		EditText t = (EditText) findViewById(R.id.EditTextTopic);
//	    		EditText m = (EditText) findViewById(R.id.editTextMessage);
//	    		TextView result = (TextView) findViewById(R.id.textResultStatus);
//	    		inputMethodManager.hideSoftInputFromWindow(result.getWindowToken(), 0);
//
//	    		String topic = t.getText().toString().trim();
//	    		String message = m.getText().toString().trim();
//
//	    		if (topic != null && !topic.isEmpty() && message != null && !message.isEmpty()) {
//	    			result.setText("");
//	    			Bundle data = new Bundle();
//	    			data.putCharSequence(MQTTservice.TOPIC, topic);
//	    			data.putCharSequence(MQTTservice.MESSAGE, message);
//	    			Message msg = Message.obtain(null, MQTTservice.PUBLISH);
//	    			msg.setData(data);
//	    			msg.replyTo = serviceHandler;
//	    			try {
//	    				service.send(msg);
//	    			} catch (RemoteException e) {
//	    				e.printStackTrace();
//	    				result.setText("Publish failed with exception:" + e.getMessage());
//	    			}
//	    		} else {
//	    			result.setText("Topic and message required.");
//	    		}
//			}
//		});
	}

	static class ServiceHandler extends Handler {

        private View view;

        public ServiceHandler(View view) {
            this.view = view;
        }

	    @Override
	    public void handleMessage(Message msg) {
            String action = "";

            switch (msg.what) {
                case MQTTservice.SUBSCRIBE: action = "Subscribe"; break;
                case MQTTservice.PUBLISH:	action = "Publish";   break;
                case MQTTservice.REGISTER:	action = "Register";  break;
                default:
                    super.handleMessage(msg);
                    return;
            }

            Bundle b = msg.getData();
            if (b != null) {
                Boolean status = b.getBoolean(MQTTservice.STATUS);
                Snackbar
                    .make(view, action + (!status ? " fail" : " success"), Snackbar.LENGTH_LONG)
                    .show();
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
        webServices.getAllTopics(new GetAllTopicsResponse());
    }

    /**
     * Task for get all Topics from Server.
     */
    private class GetAllTopicsResponse implements Callback<List<String>> {

        @Override
        public void success(List<String> result, Response response) {
            // Update list of topics
            topics.clear();
            for (int pos = 0; pos < result.size(); pos++) {
                Topic topic = new Topic();
                topic.setTitle(result.get(pos));

                if (!topics.contains(topic)) {
                    topics.add(pos, topic);
                }
            }

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
