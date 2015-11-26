package com.marcelorocha.MQTTSample.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.marcelorocha.MQTTSample.MQTTClientApp;

/**
 * Util class to get Device data.
 */
public class DeviceUtil {

    /**
     * Get the Device wi-fi macaddress.
     * @return The Device wi-fi macaddress.
     */
    public static String getMacAddress() {
        Context context = MQTTClientApp.getContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

}
