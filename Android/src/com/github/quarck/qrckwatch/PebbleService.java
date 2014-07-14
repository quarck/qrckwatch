package com.github.quarck.qrckwatch;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class PebbleService
{
	private static final String TAG = "PebbleService";
	
	private static int notificationsMask = 0;
	
	private static boolean alarmScheduled = false;

	public static void checkAlarm(Context ctx)
	{
		if (!alarmScheduled)
		{
			Alarm.setAlarmMillis(ctx, 5*60*1000); // send periodic status updates to pebble every 5 mins
			
			WeatherServiceAlarm.setAlarmHours(ctx,  3);// run weather check every 3 hours
			
			WeatherService.runWeatherUpdate(ctx);
			
			alarmScheduled = true;
		}
	}

	private static int getPhoneChargeLevel(Context context)
	{
		synchronized (PebbleService.class)
		{
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = context.registerReceiver(null, ifilter);
			
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	
			return  Math.round( level / (float)scale * 100.0f);
		}
	}

	
	public static void sendUpdateToPebble(Context context)
	{
		if (!PebbleKit.isWatchConnected(context))
			return;

		int chargeLevel = getPhoneChargeLevel(context);

		synchronized (PebbleService.class)
		{
			int weatherLevel = WeatherService.getWeatherSeverityLevel();
			int weatherCode = WeatherService.getWeatherCode();
					
			Lw.d(TAG, "sending: mask: " + notificationsMask + ", batt: " + chargeLevel + 
					(weatherLevel > 1 ? (", wthr_cd: " + weatherCode + ", lvl: " + weatherLevel) : ""));

			try 
			{
				PebbleDictionary data = new PebbleDictionary();
				data.addInt32((byte)Protocol.EntryNotificationsBitmask, notificationsMask);
				data.addUint8((byte)Protocol.EntryChargeLevel, (byte)chargeLevel);
	
				if (weatherLevel > 1)
				{
					data.addUint8((byte)Protocol.EntryWeatherAlert, (byte)weatherCode);
				}
				
				PebbleKit.sendDataToPebble(context, DataReceiver.pebbleAppUUID, data);
			}
			catch (Exception ex)
			{
				Lw.d(TAG, "Exception while sending data to pebble");
			}
		}
	}
	
	public static void setNotificationsMask(Context context, int newMask)
	{
		notificationsMask = newMask;
		sendUpdateToPebble(context);
	}
	
	public static void gotPacketFromPebble(Context context, PebbleDictionary data)
	{
		sendUpdateToPebble(context);
		checkAlarm(context);
	}
}
