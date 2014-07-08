/*
 * Copyright (c) 2014, Sergey Parshin, quarck@gmail.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.quarck.qrckwatch;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class NotificationReceiverService extends NotificationListenerService
{
	public static final String TAG = "Service";

	public static int notificationsMask = 0;
	
	public static boolean alarmScheduled = false;
	
	private static void sendBitmaskToPebble(Context ctx, int bitmask)
	{
		Lw.d(TAG, "sending bitmask " + bitmask);
		
		try 
		{
			PebbleDictionary data = new PebbleDictionary();
			data.addUint8(0, (byte)Protocol.MsgNotificationsBitmask);
			data.addInt32(1, bitmask);	
			PebbleKit.sendDataToPebble(ctx, DataReceiver.pebbleAppUUID, data);
		}
		catch (Exception ex)
		{
			Lw.d(TAG, "Exception while sending data to pebble");
		}
	}
	
	public static void sendBitmaskToPebble(Context ctx)
	{
		sendBitmaskToPebble(ctx, notificationsMask);
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Lw.d(TAG, "onCreate()");
	}

	@Override
	public void onDestroy()
	{
		Lw.d(TAG, "onDestroy (??)");
		super.onDestroy();
	}

	public static void checkAlarm(Context ctx)
	{
		if (!alarmScheduled)
		{
			Alarm.setAlarmMillis(ctx, 5*60*1000);
			alarmScheduled = true;
		}	
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		checkAlarm(this);
		return super.onBind(intent);
	}
	
	private void update(StatusBarNotification updNotification, boolean isAdded)
	{
		Lw.d(TAG, "update");

		if (!PebbleKit.isWatchConnected(this))
		{
			Lw.d(TAG, "Watch is not connected, returning");
			return;
		}

		checkAlarm(this);

		StatusBarNotification[] notifications = null;

		try
		{
			notifications = getActiveNotifications();
		}
		catch (NullPointerException ex)
		{
			Lw.e(TAG, "Got exception while obtaining list of notifications, have no permissions!");
		}

		int newBitmask = 0;
		
		if (notifications != null)
		{
			Lw.d(TAG, "Total number of notifications currently active: " + notifications.length);

			for (StatusBarNotification notification : notifications)
			{
				Lw.d(TAG, "Checking notification" + notification);

				if (notification.isOngoing())
				{
					Lw.d(TAG, "Ignoring ongoing notification");
					continue;
				}
				
				String packageName = notification.getPackageName();
				Lw.d(TAG, "Package name is " + packageName);
				
				newBitmask |= CommonAppsRegistry.getMaskBitForPackage(packageName);
			}
		}
		else
		{
			Lw.e(TAG, "Can't get list of notifications. WE HAVE NO PERMISSION!! ");
		}
		
		notificationsMask = newBitmask;
		sendBitmaskToPebble(this, notificationsMask);
	}

	@Override
	public void onNotificationPosted(StatusBarNotification arg0)
	{
		Lw.d(TAG, "Notification posted: " + arg0);
		update(arg0, true);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification arg0)
	{
		Lw.d(TAG, "Notification removed: " + arg0);
		update(arg0, false);
	}

	public static void gotPacketFromPebble(Context context, int id, PebbleDictionary data)
	{
		if (id == Protocol.MsgRequestAll)
		{
			sendBitmaskToPebble(context);
			// send charge level
		}
	}
}
