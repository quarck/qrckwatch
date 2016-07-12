package com.github.quarck.qrckwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppUpdatedBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		// after each update we are loosing permission to get notifications,
		// so service actually gets disabled, update settings to reflect this and 
		// then - ask user to re-enable permission for us		

		Intent mainActivityIntent = new Intent(context, MainActivity.class);
		PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

		Notification notification = new Notification.Builder(context)
			.setContentTitle("QrckWatch updated")
			.setContentText("Please re-enable in settings")
			.setSmallIcon(R.drawable.ic_launcher)
			.setPriority(Notification.PRIORITY_HIGH)
			.setContentIntent(pendingMainActivityIntent)
			.setAutoCancel(true)
			.build();
	
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE))
			.notify(0, notification); // would update if already exists
	}
}
