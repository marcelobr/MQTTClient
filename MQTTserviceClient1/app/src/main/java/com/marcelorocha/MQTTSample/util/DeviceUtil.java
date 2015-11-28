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

    /**
     * Get the Client Id to use on Service.
     * @return the Client Id.
     */
    public static String getClientId() {
        // Set a default dummy value...
        String result = "00:11:22:33:44";

        // Generate the client id based on Device mac address.
        String deviceMacAddress = DeviceUtil.getMacAddress();
        if (deviceMacAddress != null && !deviceMacAddress.isEmpty()) {
            result = deviceMacAddress.replace(":", "");
        }

        return result;
    }

}
