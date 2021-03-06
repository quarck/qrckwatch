package com.github.quarck.qrckwatch;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.SparseArray;

import com.github.quarck.qrckwatch.weather.NetworkClient;
import com.github.quarck.qrckwatch.weather.Parser;
import com.github.quarck.qrckwatch.weather.Weather;
import com.github.quarck.qrckwatch.weather.WeatherCodes;

@SuppressLint("Wakelock")

public class WeatherService extends IntentService 
{
	public static final String TAG = "WeatherService";

	private static PowerManager.WakeLock wakeLock = null;

	private static int weatherSeverityLevel = 0;
	private static int weatherCode = 127;
	
	private static long lastWeatherUpdate = 0;

	public static class WeatherBundle
	{
		public Weather weather;
		public long lastUpadted;
	}
	
	private static SparseArray<WeatherBundle> lastWeatherResults = new SparseArray<WeatherBundle>();
	
	public WeatherService() 
	{
		super("WeatherService");
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		int[] locations = new Settings(this).getWeatherLocations();
		
		Lw.d(TAG, "Weather locations: " + locations );
		
		int newWeatherCode = 127;
		int newWeatherSevirity = 0;
	
		for (int idx = 0; idx < locations.length; idx ++ )
		{
			String weatherUrl = "http://weather.yahooapis.com/forecastrss?w=" + locations[idx] + "&u=c";

			if (locations[idx] == 0)
			{
				Lw.d(TAG, "Location at " + idx + " is disabled, skipping");
				continue;
			}
			
			Lw.d(TAG, "Checking weather for location " + locations[idx] + ", url is " + weatherUrl);
			
			try 
			{			
				String rssXml = new NetworkClient().getWeatherRss(weatherUrl);
				
				Weather lastWeather = null;
				
				if (rssXml != null)
				{
					lastWeather = Parser.parse(rssXml);
				}
				else
				{
					Lw.e(TAG, "Failed to get rssXml");
				}
	
				if (lastWeather != null)
				{
					WeatherCodes.WeatherCode currentCode = WeatherCodes.getWeatherCode( lastWeather.currentCode );
	
					WeatherCodes.WeatherCode forecastCode = null;
	
					if (lastWeather.forecast != null )
					{
						forecastCode = WeatherCodes.getWeatherCode( lastWeather.forecast.code );
					}
					else
					{
						Lw.e(TAG, "Has no forecast data for this location");
					}
	
					if (currentCode.severity > 1 && currentCode.severity > newWeatherSevirity)
					{
						newWeatherSevirity = currentCode.severity;
						newWeatherCode = currentCode.code;
						Lw.d(TAG, "Updated[1] weather data to: code: " + newWeatherCode + ", level: " + newWeatherSevirity);
					}
	
					if (forecastCode != null && forecastCode.severity > newWeatherSevirity)
					{
						newWeatherSevirity = forecastCode.severity;
						newWeatherCode = forecastCode.code;
						Lw.d(TAG, "Updated[2] weather data to: code: " + newWeatherCode + ", level: " + newWeatherSevirity);
					}
					
					synchronized(lastWeatherResults)
					{
						WeatherBundle wb = new WeatherBundle();
						wb.weather = lastWeather;
						wb.lastUpadted = System.currentTimeMillis();
						lastWeatherResults.put(locations[idx], wb);
					}
				}
				else
				{
					Lw.e(TAG, "Failed to get weather information");
				}
			}
			catch (Exception ex)
			{
				Lw.e(TAG, "Got exception " + ex);
			}
		}

		weatherSeverityLevel = newWeatherSevirity;
		weatherCode = newWeatherCode;
		
		lastWeatherUpdate = System.currentTimeMillis();


		// release wakelock when nothing else left to do - CPU might go into sleep straight on the ".release" call
		if (wakeLock != null)
			wakeLock.release();
		wakeLock = null;
	}

	public static int getWeatherSeverityLevel()
	{
		return weatherSeverityLevel;
	}

	public static int getWeatherCode()
	{
		return weatherCode;
	}
	
	public static void runWeatherUpdate(Context ctx)
	{
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);

		WeatherService.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "qrckWatch"); 

		wakeLock.acquire();

		Intent it = new Intent(ctx, WeatherService.class);
		ctx.startService(it);
	}

	public static long secondsSinceUpdate()
	{
		long current = System.currentTimeMillis();
		
		return (current - lastWeatherUpdate) / 1000;
	}
	
	public static WeatherBundle getWeatherForLocation(int location)
	{
		WeatherBundle ret = null;
		
		synchronized (lastWeatherResults)
		{
			ret = lastWeatherResults.get(location, null);
		}
		
		return ret;
	}
	
}
