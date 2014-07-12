package com.github.quarck.qrckwatch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WeatherServiceAlarm extends BroadcastReceiver
{
	public static final String TAG = "WeatherServiceAlarm";
	
	public WeatherServiceAlarm()
	{
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) // alarm fired
	{
		Lw.d(TAG, "WeatherServiceAlarm received");
		WeatherService.runWeatherUpdate(context);
	}
	
	public static void setAlarmHours(Context context, int repeatHours)
	{
		Lw.d(TAG, "Setting WeatherServiceAlarm with repeation interval " + repeatHours + " hours");
		
		int repeatMillis = repeatHours * 3600 * 1000;
		
		Intent intent = new Intent(context, WeatherServiceAlarm.class);
		PendingIntent pendIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager(context).setInexactRepeating(AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis() + repeatMillis, repeatMillis, pendIntent);
	}

	public static void cancelAlarm(Context context)
	{
		Lw.d(TAG, "Cancelling weather alarm");
		Intent intent = new Intent(context, Alarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmManager(context).cancel(sender);
	}

	private static AlarmManager alarmManager(Context context)
	{
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}	
}
