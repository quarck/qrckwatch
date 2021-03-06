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

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.getpebble.android.kit.PebbleKit;

public class NotificationReceiverService2 extends NotificationListenerService
{
	public static final String TAG = "NotificationReceiverService";

	public static int dismissedMask = 0;
	
	public static NotificationReceiverService2 instance = null;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Lw.d(TAG, "onCreate()");
		
		instance = this;
	}

	@Override
	public void onDestroy()
	{
		instance = null;
		
		Lw.d(TAG, "onDestroy (??)");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		PebbleService.checkInitialized(this);
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

		PebbleService.checkInitialized(this);
		
		int notificationBit = CommonAppsRegistry.getMaskBitForPackage(updNotification.getPackageName());
		
		dismissedMask = dismissedMask & ~notificationBit; // received/removed notification -- clear dismissal bit 
		
		if (isAdded)
		{
			if (notificationBit == CommonAppsRegistry.Viber 
					|| notificationBit == CommonAppsRegistry.Skype
					|| notificationBit == CommonAppsRegistry.Email)
			{
				Notification ntfy = updNotification.getNotification();
				
				if (ntfy != null)
				{
			        NotificationParser parser = new NotificationParser(this, ntfy);

			        String secondaryTitle = parser.title;
			        String text = parser.text.trim();

			        if (ntfy.tickerText != null && (text == null || text.trim().length() == 0)) 
			        {
			                text = ntfy.tickerText.toString();
			        }

			        PebbleService.sendNotificationToPebble(this, secondaryTitle, text);
				}
			}
		}

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
				
				if (CommonAppsRegistry.isIgnoredApp(packageName))
				{
					Lw.d(TAG, "Ignoring google play annoying notifications");
					continue;
				}

				newBitmask |= CommonAppsRegistry.getMaskBitForPackage(packageName);
			}

			newBitmask = newBitmask & ~dismissedMask;
		}
		else
		{
			Lw.e(TAG, "Can't get list of notifications. WE HAVE NO PERMISSION!! ");
		}
		
		PebbleService.setNotificationsMask(this, newBitmask);
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

	public static void dismissNotifications(Context ctx, int level, int id)
	{
		if (level == Protocol.DismissLevelPhone)
		{
			try
			{
				int mask = dismissIdToMask(id);

				StatusBarNotification[] notifications = instance.getActiveNotifications();

				Lw.d(TAG, "Total number of notifications currently active: " + notifications.length);

				for (StatusBarNotification notification : notifications)
				{
					Lw.d(TAG, "Checking notification" + notification);

					if (notification.isOngoing())
						continue;
					
					String packageName = notification.getPackageName();
					Lw.d(TAG, "Package name is " + packageName);

					int appBit = CommonAppsRegistry.getMaskBitForPackage(packageName);
					
					if ((mask & appBit) != 0)
					{
						Lw.d(TAG, "Dismissing notification with package " + packageName);
						instance.cancelNotification(packageName, notification.getTag(), notification.getId());
					}
				}
			}
			catch (NullPointerException ex)
			{
				Lw.e(TAG, "Got exception while obtaining list of notifications, have no permissions!");
			}
			
			PebbleService.sendDismissalConfirmation(ctx, level, id);
		}
		else if (level == Protocol.DismissLevelWatch)
		{
			int mask = dismissIdToMask(id);
			dismissedMask = dismissedMask | mask;
			
			PebbleService.setDismissalMask(instance, dismissedMask);

			PebbleService.sendDismissalConfirmation(ctx, level, id);
		}
	}
	
	private static int dismissIdToMask(int id)
	{
		int mask = 0;

		switch (id)
		{
		case Protocol.DismissableItemViber:
			mask = CommonAppsRegistry.Viber; 
			break;
		case Protocol.DismissableItemGmail:
			mask = CommonAppsRegistry.GMail;
			break;
		case Protocol.DismissableItemCalendar:
			mask = CommonAppsRegistry.Calendar;
			break;
		case Protocol.DismissableItemMail:
			mask = CommonAppsRegistry.Email;
			break;			
		case Protocol.DismissableItemPhone:
			mask = CommonAppsRegistry.Phone;
			break;
		case Protocol.DismissableItemMessage:
			mask = CommonAppsRegistry.Messages;
			break;
		case Protocol.DismissableItemGoogleHangouts:
			mask = CommonAppsRegistry.GoogleHangouts;
			break;
		case Protocol.DismissableItemSkype:
			mask = CommonAppsRegistry.Skype;
			break;			

		case Protocol.DismissableItemEverything:
			mask = CommonAppsRegistry.Viber
					| CommonAppsRegistry.GMail
					| CommonAppsRegistry.Calendar
					| CommonAppsRegistry.Email
					| CommonAppsRegistry.Phone
					| CommonAppsRegistry.Messages
					| CommonAppsRegistry.GoogleHangouts
					| CommonAppsRegistry.Skype;
			break;
		}
		
		Lw.d(TAG, "ID to mask: " + id + " mask + " + mask);
		
		return mask;
	}
}
