package com.github.quarck.qrckwatch;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.github.quarck.qrckwatch.weather.NetworkClient;
import com.github.quarck.qrckwatch.weather.Parser;
import com.github.quarck.qrckwatch.weather.Weather;
import com.github.quarck.qrckwatch.weather.WeatherCodes;

@SuppressLint("Wakelock")

public class WeatherService extends IntentService 
{
	public static final String TAG = "WeatherService";

	private static Weather lastWeather = null;
	
	private static PowerManager.WakeLock wakeLock = null;

	private static int weatherSeverityLevel = 0;
	private static int weatherCode = 127;

	public WeatherService() 
	{
		super("WeatherService");
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		String hardcodedForNowUrl = "http://weather.yahooapis.com/forecastrss?w=560755&u=c";

		try 
		{			
			String rssXml = new NetworkClient().getWeatherRss(hardcodedForNowUrl);
			
			if (rssXml != null)
			{
				lastWeather = Parser.parse(rssXml);
			}

			if (lastWeather != null)
			{
				WeatherCodes.WeatherCode currentCode = WeatherCodes.getWeatherCode( lastWeather.currentCode );

				WeatherCodes.WeatherCode forecastCode = null;

				if (lastWeather.forecast != null )
					forecastCode = WeatherCodes.getWeatherCode( lastWeather.forecast.code );

				if (currentCode.severity > 1)
				{
					weatherSeverityLevel = currentCode.severity;
					weatherCode = currentCode.code;
				}

				if (forecastCode != null && forecastCode.severity > currentCode.severity)
				{
					weatherSeverityLevel = forecastCode.severity;
					weatherCode = forecastCode.code;
				}
			}
		}
		catch (Exception ex)
		{
		}
		finally
		{
			if (wakeLock != null)
				wakeLock.release();
			wakeLock = null;
		}
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

	public static void test(Context ctx)
	{
/*		weatherCode++;
		weatherSeverityLevel = 10;
		PebbleService.sendUpdateToPebble(ctx); 
	*/
	//	runWeatherUpdate(ctx);
	}
}
