package com.github.quarck.qrckwatch;

import com.github.quarck.qrckwatch.weather.Weather;
import com.github.quarck.qrckwatch.weather.Weather.DayForecast;
import com.github.quarck.qrckwatch.weather.WeatherCodes;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

public class WeatherDetailActivity extends Activity
{
	
	WeatherService.WeatherBundle weatherBundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_detail);

		Settings settings = new Settings(this);
		
		Intent intent = getIntent();		
		
		int weatherLoc = 0;

		switch (intent.getIntExtra("w", 0))
		{
		case 1: 
			weatherLoc = settings.getWeatherLoc1(); break;
		case 2: 
			weatherLoc = settings.getWeatherLoc2(); break;
		case 3: 
			weatherLoc = settings.getWeatherLoc3(); break;
		case 4: 
			weatherLoc = settings.getWeatherLoc4(); break;
		case 5: 
			weatherLoc = settings.getWeatherLoc5(); break;
		}
		
		weatherBundle = WeatherService.getWeatherForLocation(weatherLoc);
		
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{
		public PlaceholderFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_weather_detail, container, false);
	
			TextView tv = (TextView)rootView.findViewById(R.id.textViewWeatherDetails);
			
			WeatherService.WeatherBundle wb = ((WeatherDetailActivity)getActivity()).weatherBundle;
			
			if (wb != null)
			{
				StringBuilder sb = new StringBuilder();
				
				sb.append("Last updated: ");
				sb.append((System.currentTimeMillis() - wb.lastUpadted) / 60000);
				sb.append(" minutes ago\n");
				
				Weather w = wb.weather;
				
				sb.append("Current conditions:\n");
				sb.append("Wind: direction: "); sb.append(w.windDirection);
				sb.append(", speed: "); sb.append(w.windSpeed); sb.append("\n");
				
				sb.append("Humidity: "); sb.append(w.humidity); sb.append("\n");
				sb.append("Visibility: "); sb.append(w.visibility); sb.append("\n");
				sb.append("Pressure: "); sb.append(w.pressure); sb.append("\n");
				
				sb.append("Temperature: "); sb.append(w.currentTemp); 
				sb.append(", real feel: "); sb.append(w.windChill); sb.append("\n");
				sb.append("Cond: "); sb.append(WeatherCodes.getDesciption(w.currentCode)); sb.append("\n");
	
				for (int idx=0; idx< w.forecasts.size(); idx++)
				{
					DayForecast forecast = w.forecasts.get(idx);
					
					if (forecast != null)
					{
						sb.append("\nForecast for ");
						sb.append(forecast.date.getDate());
						sb.append("/");
						sb.append(forecast.date.getMonth());
						sb.append("/");
						sb.append(forecast.date.getYear()+1900);
						if (forecast == w.forecast)
							sb.append(" [CURRENT]");
						sb.append(":\n");
	
						sb.append("Temperature: "); sb.append(forecast.tempLow); sb.append(" to ");
						sb.append(forecast.tempHigh); sb.append("\n");
						
						sb.append("Cond: "); sb.append(WeatherCodes.getDesciption(forecast.code)); sb.append("  ");
						
						int severityLevel = WeatherCodes.getSeverityLevel(forecast.code);
						
						if (severityLevel > 1)
						{
							sb.append(" <--- ");
							
							if (severityLevel <= 2)
								sb.append("Warning\n");
							else if (severityLevel <= 5)
								sb.append("!! WARNING !!\n");
							else 
								sb.append("!!!! EMERGENCY !!!!\n");
						}
						else
						{
							sb.append("\n");
						}
					}
				}
				
				tv.setText(sb.toString());
			}
			else
			{
				tv.setText("No weather data");
			}
			
			
			return rootView;
		}
	}

}
