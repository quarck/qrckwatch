package com.github.quarck.qrckwatch;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class Service
{
	private static final String TAG = "Service";
	
	private static int notificationsMask = 0;
	
	private static boolean alarmScheduled = false;
	
	public static void checkAlarm(Context ctx)
	{
		if (!alarmScheduled)
		{
			Alarm.setAlarmMillis(ctx, 5*60*1000);
			alarmScheduled = true;
		}	
	}
	
	private static void sendBitmaskToPebble(Context ctx, int bitmask)
	{
	}
	
	private static int getPhoneChargeLevel(Context context)
	{
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		/*        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;*/
		
		return  Math.round( level / (float)scale * 100.0f);
	}
	
	public static void sendUpdateToPebble(Context context)
	{
		if (!PebbleKit.isWatchConnected(context))
			return;

		int chargeLevel = getPhoneChargeLevel(context);
		
		Lw.d(TAG, "sending bitmask " + notificationsMask + ", batt " + chargeLevel);
		
		try 
		{
			PebbleDictionary data = new PebbleDictionary();
			data.addInt32((byte)Protocol.EntryNotificationsBitmask, notificationsMask);
			data.addInt32((byte)Protocol.EntryChargeLevel, chargeLevel);

			PebbleKit.sendDataToPebble(context, DataReceiver.pebbleAppUUID, data);
		}
		catch (Exception ex)
		{
			Lw.d(TAG, "Exception while sending data to pebble");
		}
	}
	
	public static void setNotificationsMask(Context context, int newMask)
	{
		notificationsMask = newMask;
		sendBitmaskToPebble(context, notificationsMask);
	}
	
	public static void gotPacketFromPebble(Context context, PebbleDictionary data)
	{
		sendUpdateToPebble(context);
	}
}
