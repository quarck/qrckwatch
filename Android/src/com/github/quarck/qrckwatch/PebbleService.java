package com.github.quarck.qrckwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class PebbleService
{
	private static final String TAG = "PebbleService";
	
	public final static UUID pebbleAppUUID = UUID.fromString("0b633775-2a83-4a28-9d0f-2c06ad154251");
	
	private static int notificationsMask = 0;
	
	private static Object lock = new Object();
	
	private static boolean initialized = false;
	
	private static int failCount = 0;

	public static void checkInitialized(Context ctx)
	{
		boolean needInitialization = false;
		synchronized(lock)
		{
			if (!initialized)
			{
				needInitialization = true;
			}
		}
		
		if (needInitialization)
		{
			registerPebbleCallbacks(ctx);
			
			Alarm.setAlarmMillis(ctx, 5*60*1000); // send periodic status updates to pebble every 5 mins
			
			WeatherServiceAlarm.setAlarmHours(ctx,  1);// run weather check every 1 hour(s)
			
			WeatherService.runWeatherUpdate(ctx);
			
			synchronized(lock)
			{
				initialized = true;
			}
		}
	}

	private static void registerPebbleCallbacks(final Context ctx)
	{
		PebbleKit.registerReceivedAckHandler(ctx, 
			new PebbleAckReceiver(pebbleAppUUID) 
			{
				@Override
				public void receiveAck(Context context, int transactionId) 
				{
					Lw.d(TAG, "Received ack for transaction " + transactionId);

					synchronized (lock)
					{
						failCount = 0;
					}
				}
			});

		PebbleKit.registerReceivedNackHandler(ctx, 
			new PebbleNackReceiver(pebbleAppUUID) 
			{
				@Override
				public void receiveNack(Context context, int transactionId) 
				{
					boolean couldResend = false;

					synchronized (lock)
					{
						if (++failCount < 10)
							couldResend = true;
					}

					if (couldResend)
					{
						Lw.d(TAG, "Received nack for transaction, re-sending status update " + transactionId);						
						sendUpdateToPebble(ctx);
					}
					else
					{
						Lw.d(TAG, "Received nack for transaction, exceded re-try count ");
					}
				}
			});
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

		synchronized (lock)
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
				
				PebbleKit.sendDataToPebble(context, pebbleAppUUID, data);
			}
			catch (Exception ex)
			{
				Lw.d(TAG, "Exception while sending data to pebble");
			}
		}
	}
	
	public static void setNotificationsMask(Context context, int newMask)
	{
		synchronized (lock)
		{
			notificationsMask = newMask;
		}
		sendUpdateToPebble(context);
	}
	
	public static void gotPacketFromPebble(Context context, PebbleDictionary data)
	{
		sendUpdateToPebble(context);
		checkInitialized(context);
	}
	
	public static void sendNotificationToPebble(Context ctx, String title, String body) 
	{
	    final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

	    final Map data = new HashMap();
	    data.put("title", title);
	    data.put("body", body);
	    
	    final JSONObject jsonData = new JSONObject(data);
	    final String notificationData = new JSONArray().put(jsonData).toString();

	    i.putExtra("messageType", "PEBBLE_ALERT");
	    i.putExtra("sender", "QrckWatch");
	    i.putExtra("notificationData", notificationData);

	    Lw.d(TAG, "About to send a modal alert to Pebble: " + notificationData);
	    ctx.sendBroadcast(i);
	}
}
