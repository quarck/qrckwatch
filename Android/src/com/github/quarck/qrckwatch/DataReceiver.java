package com.github.quarck.qrckwatch;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

import java.util.UUID;

import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;


public class DataReceiver extends BroadcastReceiver 
{
	public final static UUID pebbleAppUUID = UUID.fromString("0b633775-2a83-4a28-9d0f-2c06ad154251");
	
	public void receiveData(final Context context, final int transactionId, final PebbleDictionary data)
	{
		Service.checkAlarm(context);
		
		PebbleKit.sendAckToPebble(context, transactionId);

		Service.gotPacketFromPebble(context, data);
	}

	public void onReceive(final Context context, final Intent intent) 
	{
		final UUID receivedUuid = (UUID) intent.getSerializableExtra(APP_UUID);

		if (!receivedUuid.equals(pebbleAppUUID)) 
		{
			return;
		}

		final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
		final String jsonData = intent.getStringExtra(MSG_DATA);
		if (jsonData == null || jsonData.isEmpty()) 
		{
			return;
		}

		try 
		{
			final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
			receiveData(context, transactionId, data);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
			return;
		}
	}

}
