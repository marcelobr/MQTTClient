package com.marcelorocha.MQTTSample.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.marcelorocha.MQTTSample.BuildConfig;
import com.marcelorocha.MQTTSample.R;
import com.marcelorocha.MQTTSample.data.NotificationStorage;
import com.marcelorocha.MQTTSample.util.DeviceUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MQTTservice extends Service {

	private static boolean serviceRunning = false;
	private static int mid = 0;
	private static MQTTConnection connection = null;
	private final Messenger clientMessenger = new Messenger(new ClientHandler());

	@Override
	public void onCreate() {
		super.onCreate();
		connection = new MQTTConnection();
	}
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isRunning()) {
			return START_STICKY;
		}
		
		super.onStartCommand(intent, flags, startId);

		/*
		 * Start the MQTT Thread.
		 */
	    connection.start();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		connection.end();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		/*
		 * Return a reference to our client handler.
		 */
		return clientMessenger.getBinder();
	}
	
	 private synchronized static boolean isRunning() {
		 /*
		  * Only run one instance of the service.
		  */
		 if (serviceRunning == false) {
			 serviceRunning = true;
			 return false;
		 } else {
			 return true;
		 }
	 }

	 /*
	  * These are the supported messages from bound clients
	  */
	 public static final int REGISTER = 0;
	 public static final int SUBSCRIBE = 1;
	 public static final int PUBLISH = 2;
     public static final int UNSUBSCRIBE = -1;
     public static final int SUBSCRIBE_LIST = 10;

	 /*
	  * Fixed strings for the supported messages.
	  */
	 public static final String TOPIC = "topic";
	 public static final String MESSAGE = "message";
	 public static final String STATUS = "status";
	 public static final String CLASSNAME = "classname";
	 public static final String INTENT_NAME = "intentname";
     public static final String CLIENT_ID = "client_id";
     public static final String TOPICS_LIST = "topics_list";
     public static final String TOPICS_SUBSCRIBED = "topics_subscribed";

	 /*
	  * This class handles messages sent to the service by
	  * bound clients.
	  */
	 class ClientHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
        	 boolean status = false;

        	 switch (msg.what) {
                 case SUBSCRIBE:
                 case SUBSCRIBE_LIST:
                 case UNSUBSCRIBE:
                 case PUBLISH:
                        /*
                         * These two requests should be handled by
                         * the connection thread, call makeRequest
                         */
                        connection.makeRequest(msg);
                        break;
                 case REGISTER:
                 {
                     Bundle b = msg.getData();
                     if (b != null) {
                         Object target = b.getSerializable(CLASSNAME);
                         if (target != null) {
                             /*
                              * This request can be handled in-line
                              * call the API
                              */
                             connection.setPushCallback((Class<?>) target);
                             status = true;
                         }
                         CharSequence cs = b.getCharSequence(INTENT_NAME);
                         if (cs != null) {
                             String name = cs.toString().trim();
                             if (name.isEmpty() == false) {
                                /*
                                * This request can be handled in-line
                                * call the API
                                */
                                 connection.setIntentName(name);
                                 status = true;
                             }
                         }

                         // Subscribe the own device Topic
                         Bundle data = new Bundle();
                         data.putCharSequence(MQTTservice.TOPIC, DeviceUtil.getClientId());
                         Message msgToSend = Message.obtain(null, MQTTservice.SUBSCRIBE);
                         msgToSend.setData(data);
                         connection.makeRequest(msgToSend);
                     }
                     ReplytoClient(msg.replyTo, msg.what, status);
                     break;
                 }
             }
         }
     }

    private void ReplytoClient(Messenger responseMessenger, int type, boolean status) {
		ReplytoClient(responseMessenger, type, status, null);
    }

    private void ReplytoClient(Messenger responseMessenger, int type, boolean status, Bundle data) {
		/*
		 * A response can be sent back to a requester when
		 * the replyTo field is set in a Message, passed to this
		 * method as the first parameter.
		 */
        if (responseMessenger != null) {
            if (data == null) data = new Bundle();
            data.putBoolean(STATUS, status);
            Message reply = Message.obtain(null, type);
            reply.setData(data);

            try {
                responseMessenger.send(reply);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    enum CONNECT_STATE {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }
	
	private class MQTTConnection extends Thread {

        private Class<?> launchActivity = null;
        private String intentName = null;
        private MsgHandler msgHandler = null;
        private static final int STOP = PUBLISH + 1;
        private static final int CONNECT = PUBLISH + 2;
        private static final int RESETTIMER = PUBLISH + 3;
        private CONNECT_STATE connState = CONNECT_STATE.DISCONNECTED;
		
        MQTTConnection() {
            msgHandler = new MsgHandler();
            msgHandler.sendMessage(Message.obtain(null, CONNECT));
        }

        public void end() {
            msgHandler.sendMessage(Message.obtain(null, STOP));
        }
		
        public void makeRequest(Message msg) {
            /*
             * It is expected that the caller only invokes
             * this method with valid msg.what.
             */
            msgHandler.sendMessage(Message.obtain(msg));
        }
		
        public void setPushCallback(Class<?> activityClass) {
            launchActivity = activityClass;
        }
		
        public void setIntentName(String name) {
            intentName = name;
        }

		private class MsgHandler extends Handler implements MqttCallback {

			//private final String HOST = "iot.eclipse.org";
		    //private final int PORT = 1883;

            private final String HOST = BuildConfig.HOST;
            private final int PORT = BuildConfig.PORT;
            private final String uri = "tcp://" + HOST + ":" + PORT;
            private final int MINTIMEOUT = 2000;
            private final int MAXTIMEOUT = 32000;
            private int timeout = MINTIMEOUT;
            private MqttClient client = null;
            private MqttConnectOptions options = new MqttConnectOptions();
            //private Vector<String> topics = new Vector<String>();
            private String clientId = null;

			MsgHandler() {
				//options.setCleanSession(true);
				options.setCleanSession(false);
			    try {
					//client = new MqttClient(uri, MqttClient.generateClientId(), null);
                    clientId = DeviceUtil.getClientId();
					client = new MqttClient(uri, clientId, null);
					client.setCallback(this);
				} catch (MqttException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				    case STOP:
                    {
                        /*
                         * Clean up, and terminate.
                         */
                        client.setCallback(null);
                        if (client.isConnected()) {
                            try {
                                client.disconnect();
                                client.close();
                            } catch (MqttException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        getLooper().quit();
                        break;
                    }
                    case CONNECT:
                    {
                        if (connState != CONNECT_STATE.CONNECTED) {
                            try {
                                client.connect(options);
                                connState = CONNECT_STATE.CONNECTED;
                                Log.d(getClass().getCanonicalName(), "Connected");
                                timeout = MINTIMEOUT;
                            } catch (MqttException e) {
                                Log.d(getClass().getCanonicalName(), "Connection attemp failed with reason code = " + e.getReasonCode() + e.getCause());
                                if (timeout < MAXTIMEOUT) {
                                    timeout *= 2;
                                }
                                this.sendMessageDelayed(Message.obtain(null, CONNECT), timeout);
                                return;
                            }

                            ///*
                            // * Re-subscribe to previously subscribed topics
                            // */
                            //Iterator<String> i = topics.iterator();
                            //while (i.hasNext()) {
                            //    subscribe(i.next());
                            //}
                        }
                        break;
                    }
                    case RESETTIMER:
                    {
                        timeout = MINTIMEOUT;
                        break;
                    }
                    case SUBSCRIBE:
                    {
                        boolean status = false;
                        Bundle b = msg.getData();
                        String topic = null;
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    status = subscribe(topic);
                                    ///*
                                    // * Save this topic for re-subscription if needed.
                                    // */
                                    //if (status)
                                    //{
                                    //    topics.add(topic);
                                    //}
                                }
                            }
                        }
                        Bundle dataToSend = new Bundle();
                        dataToSend.putString(CLIENT_ID, clientId);
                        dataToSend.putString(TOPIC, topic);
                        ReplytoClient(msg.replyTo, msg.what, status, dataToSend);
                        break;
                    }
                    case SUBSCRIBE_LIST:
                    {
                        Bundle b = msg.getData();
                        ArrayList<String> subscribedTopics = new ArrayList<>();
                        int topicsSize = 0;
                        if (b != null) {
                            List<String> topics = b.getStringArrayList(TOPICS_LIST);
                            if (topics != null) {
                                topicsSize = topics.size();
                                if (topics.isEmpty() == false) {
                                    for (String topic : topics) {
                                        boolean result = subscribe(topic);
                                        if (result) {
                                            subscribedTopics.add(topic);
                                        }
                                    }
                                }
                            }
                        }
                        boolean status = subscribedTopics.size() == topicsSize;
                        Bundle dataToSend = new Bundle();
                        dataToSend.putString(CLIENT_ID, clientId);
                        dataToSend.putStringArrayList(TOPICS_SUBSCRIBED, subscribedTopics);
                        ReplytoClient(msg.replyTo, msg.what, status, dataToSend);
                        break;
                    }
                    case UNSUBSCRIBE:
                    {
                        boolean status = false;
                        Bundle b = msg.getData();
                        String topic = null;
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    status = unsubscribe(topic);
                                    ///*
                                    // * Save this topic for re-subscription if needed.
                                    // */
                                    //if (status)
                                    //{
                                    //    topics.remove(topic);
                                    //}
                                }
                            }
                        }
                        Bundle dataToSend = new Bundle();
                        dataToSend.putString(CLIENT_ID, clientId);
                        dataToSend.putString(TOPIC, topic);
                        ReplytoClient(msg.replyTo, msg.what, status, dataToSend);
                        break;
                    }
                    case PUBLISH:
                    {
                        boolean status = false;
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    cs = b.getCharSequence(MESSAGE);
                                    if (cs != null) {
                                        String message = cs.toString().trim();
                                        if (message.isEmpty() == false) {
                                            status = publish(topic, message);
                                        }
                                    }
                                }
                            }
                        }
                        ReplytoClient(msg.replyTo, msg.what, status);
                        break;
                    }
				}
			}

			private boolean subscribe(String topic) {
				try {
					client.subscribe(topic);
				} catch (MqttException e) {
					Log.d(getClass().getCanonicalName(), "Subscribe failed with reason code = " + e.getReasonCode());
					return false;
				}
				return true;
			}

            private boolean unsubscribe(String topic) {
                try {
                    client.unsubscribe(topic);
                } catch (MqttException e) {
                    Log.d(getClass().getCanonicalName(), "Unsubscribe failed with reason code = " + e.getReasonCode());
                    return false;
                }
                return true;
            }

			private boolean publish(String topic, String msg) {
				try {
					MqttMessage message = new MqttMessage();
					message.setPayload(msg.getBytes());
					client.publish(topic, message);
				} catch (MqttException e) {
					Log.d(getClass().getCanonicalName(), "Publish failed with reason code = " + e.getReasonCode());
					return false;
				}		
				return true;
			}
			
			@Override
			public void connectionLost(Throwable arg0) {
				Log.d(getClass().getCanonicalName(), "connectionLost");
				connState = CONNECT_STATE.DISCONNECTED;
				sendMessageDelayed(Message.obtain(null, CONNECT), timeout);
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0) {
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				Log.d(getClass().getCanonicalName(), topic + ":" + message.toString());
				
				if (intentName != null) {
					Intent intent = new Intent();
					intent.setAction(intentName);
					intent.putExtra(TOPIC, topic);
					intent.putExtra(MESSAGE, message.toString());
					sendBroadcast(intent);
				}
				

				Context context = getBaseContext();
				PendingIntent pendingIntent = null;
					
				if (launchActivity != null) {
                    // Intent for the activity to open when user selects the notification
                    Intent intent = new Intent(context, launchActivity);

                    // Use TaskStackBuilder to build the back stack and get the PendingIntent
                    pendingIntent = TaskStackBuilder.create(context)
                                // add all of launchActivity's parents to the stack,
                                // followed by launchActivity itself
                                .addNextIntentWithParentStack(intent)
                                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

					// Build the pending intent that will start the appropriate activity
					//pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				}

                // Get the message
                String strMessage = message.toString();

				// Build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setAutoCancel(true)
                        .setContentIntent(pendingIntent)
				        .setContentText(strMessage)
				        .setSmallIcon(R.drawable.ic_launcher);

				Notification notification = builder.build();
				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nm.notify(mid++, notification);

                // Store the Notification on storage
                NotificationStorage.saveNotification(strMessage);
			}

		}

	}

}
