/*
 * Copyright (c) 2014 HygieiaSoft
 * Distributed under the MIT License.
 * (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
 */
package org.hygieiasoft.cordova.uid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UID extends CordovaPlugin {

	private static final String TAG = UID.class.getSimpleName();

	private static final int PHONE_STATE_CODE = 1;

	public static String uuid; // Device UUID
	public static String imei; // Device IMEI
	public static String imsi; // Device IMSI
	public static String iccid; // Sim IMSI
	public static String mac; // MAC address

	private CallbackContext callbackContext;
    private Context context;

    private static boolean needRequestPermission = true;

	/**
	 * Constructor.
	 */
	public UID() {
	}

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 *
	 * @param cordova The context of the main Activity.
	 * @param webView The CordovaWebView Cordova is running in.
	 */
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

        Log.d(TAG, "*********** UID initialize " );

		this.context = cordova.getActivity().getApplicationContext();
	}

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action            The action to execute.
	 * @param args              JSONArry of arguments for the plugin.
	 * @param callbackContext   The callback id used when calling back into JavaScript.
	 * @return                  True if the action was valid, false if not.
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;

		JSONObject r = new JSONObject();

        Log.d(TAG, "*********** UID execute: " + args);

		if (action.equals("getUID")) {
			if( needRequestPermission && (args != null && args.length() > 0 && "appExecute".equals(args.getString(0)))){
				if( !PermissionHelper.hasPermission(this, Manifest.permission.READ_PHONE_STATE)) {
					needRequestPermission = false;

					PermissionHelper.requestPermission(this, PHONE_STATE_CODE, Manifest.permission.READ_PHONE_STATE);

					UID.imei = "";
					UID.imsi = "";
					UID.iccid = "";

					return true;
				} else {

	                if (UID.imei == null || UID.imei.isEmpty()) {
	                    r.put("IMEI", getImei(context));
	                    r.put("IMSI", getImsi(context));
	                    r.put("ICCID", getIccid(context));
	                } else {
	                    r.put("IMEI", UID.imei);
	                    r.put("IMSI", UID.imsi);
	                    r.put("ICCID", UID.iccid);
	                }
				}
			}else{
				r.put("UUID", UID.uuid);
				r.put("IMEI", UID.imei);
				r.put("IMSI", UID.imsi);

			}
		
			r.put("ICCID", UID.iccid);
			r.put("MAC", UID.mac);

			PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, r);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);

			return true;

		} else {
			return false;
		}
	}

	/**
	 * Get the device's Universally Unique Identifier (UUID).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getUuid(Context context) {
		String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return uuid;
	}

	/**
	 * Get the device's International Mobile Station Equipment Identity (IMEI).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getImei(Context context) {
		final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephony.getDeviceId();
		return imei;
	}

	/**
	 * Get the device's International mobile Subscriber Identity (IMSI).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getImsi(Context context) {
		final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephony.getSubscriberId();
		return imsi;
	}

	/**
	 * Get the sim's Integrated Circuit Card Identifier (ICCID).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getIccid(Context context) {
		final TelephonyManager mTelephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String iccid = mTelephony.getSimSerialNumber();
		return iccid;
	}

	/**
	 * Get the Media Access Control address (MAC).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getMac(Context context) {
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		final WifiInfo wInfo = wifiManager.getConnectionInfo();
		String mac = wInfo.getMacAddress();
		return mac;
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
										  int[] grantResults) throws JSONException {

		Log.d(TAG, "onRequestPermissionResult: ***");

		needRequestPermission = true;

		for(int i=0;i<permissions.length;i++) {
			if( permissions[i].equals(Manifest.permission.READ_PHONE_STATE) ) {

				if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
					UID.imei = getImei(context);
					UID.imsi = getImsi(context);
					UID.iccid = getIccid(context);

					JSONObject r = new JSONObject();
					r.put("UUID", UID.uuid);
					r.put("IMEI", UID.imei);
					r.put("IMSI", UID.imsi);
					r.put("ICCID", UID.iccid);
					r.put("MAC", UID.mac);

					PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, r);
					pluginResult.setKeepCallback(true);
					callbackContext.sendPluginResult(pluginResult);
				} else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
					Log.w(TAG, "onRequestPermissionResult: permission READ_PHONE_STATE denied");

					//it's for execute callback.
					PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
					pluginResult.setKeepCallback(true);
					callbackContext.sendPluginResult(pluginResult);
				}

			}
		}

	}
}