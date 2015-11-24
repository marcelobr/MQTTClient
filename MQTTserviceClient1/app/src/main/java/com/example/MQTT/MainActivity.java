package com.example.MQTT;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.MQTT.network.WebServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private View mLoadingView;
    private View mEmptyView;

    private TopicsAdapter mAdapter;

    /**
     * Represents the list of topics on server.
     */
    private List<Topic> topics = new ArrayList<>();

	private Messenger service = null;
	private final Messenger serviceHandler = new Messenger(new ServiceHandler());
	private IntentFilter intentFilter = null;
	private PushReceiver pushReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
       
    	intentFilter = new IntentFilter();
    	intentFilter.addAction("com.example.MQTT.PushReceived");
    	pushReceiver = new PushReceiver();
        registerReceiver(pushReceiver, intentFilter, null, null);

        startService(new Intent(this, MQTTservice.class));	
		addSubscribeButtonListener();
		addPublishButtonListener();
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

        // Get topics on server...
        (new GetAllTopics()).execute();
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
			data.putCharSequence(MQTTservice.INTENTNAME, "com.example.MQTT.PushReceived");
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
        mLoadingView = findViewById(R.id.loading_layout);
        mEmptyView = findViewById(R.id.empty_message);

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.topics_list);
        mAdapter = new TopicsAdapter();

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

    private void addSubscribeButtonListener() {
//	    Button subscribeButton = (Button) findViewById(R.id.buttonSubscribe);
//	    subscribeButton.setOnClickListener(new OnClickListener() {
//	    	InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//	    	@Override
//			public void onClick(View arg0) {
//	    		TextView result = (TextView) findViewById(R.id.textResultStatus);
//	    		EditText t = (EditText) findViewById(R.id.EditTextTopic);
//	    		String topic = t.getText().toString().trim();
//	    		inputMethodManager.hideSoftInputFromWindow(result.getWindowToken(), 0);
//
//	    		if (topic != null && !topic.isEmpty()) {
//	    			result.setText("");
//	    			Bundle data = new Bundle();
//	    			data.putCharSequence(MQTTservice.TOPIC, topic);
//	    			Message msg = Message.obtain(null, MQTTservice.SUBSCRIBE);
//	    			msg.setData(data);
//	    			msg.replyTo = serviceHandler;
//	    			try {
//	    				service.send(msg);
//	    			} catch (RemoteException e) {
//	    				e.printStackTrace();
//	    				result.setText("Subscribe failed with exception:" + e.getMessage());
//	    			}
//	    		} else {
//	    			result.setText("Topic required.");
//	    		}
//			}
//		});
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

	class ServiceHandler extends Handler {

	    @Override
	    public void handleMessage(Message msg) {
		   	 switch (msg.what) {
                 case MQTTservice.SUBSCRIBE: break;
                 case MQTTservice.PUBLISH:	 break;
                 case MQTTservice.REGISTER:	 break;
                 default:
                     super.handleMessage(msg);
                     return;
             }
	   	 
	  		 Bundle b = msg.getData();
	  		 if (b != null) {
	  			 //TextView result = (TextView) findViewById(R.id.textResultStatus);
	  			 //Boolean status = b.getBoolean(MQTTservice.STATUS);
                 //result.setText(!status ? "Fail" : "Success");
	  		 }
	    }

	}

    /**
     * Task for get all Topics from Server.
     */
    class GetAllTopics extends AsyncTask<Void, Void , List<String>> {

        @Override
        protected void onPreExecute() {
            if (topics.isEmpty()) {
                mLoadingView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            final WebServices webServices = MQTTClientApp.getWebServices();
            return webServices.getAllTopics();
        }

        @Override
        protected void onPostExecute(List<String> result) {
            // Update list of topics
            for (int pos = 0; pos < result.size(); pos++) {
                Topic topic = new Topic();
                topic.setTitle(result.get(pos));

                if (!topics.contains(topic)) {
                    topics.add(pos, topic);
                }
            }

            mAdapter.loadTopics(topics);
            mLoadingView.setVisibility(View.GONE);

            if (topics.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

    }

}
