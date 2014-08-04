package com.github.quarck.qrckwatch;

import com.github.quarck.qrckwatch.weather.Weather;
import com.github.quarck.qrckwatch.weather.Weather.DayForecast;
import com.github.quarck.qrckwatch.weather.WeatherCodes;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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

		weatherLoc = settings.getWeatherLoc(intent.getIntExtra("w", 0));
		
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
			
			TextView tvlu = (TextView)rootView.findViewById(R.id.textViewLastUpdated);
			
			WeatherService.WeatherBundle wb = ((WeatherDetailActivity)getActivity()).weatherBundle;
			
			if (wb != null)
			{
				StringBuilder sb = new StringBuilder();
				
				Weather w = wb.weather;
				
				sb.append("<h3>");
				
				if (w.city != null)
				{
					sb.append(w.city);
					if (w.country != null)
					{
						sb.append("/"); 
						sb.append(w.country);
					}
					sb.append(":");
				}
				else
				{
					sb.append("Current:");
				}

				sb.append("</h3><b>"); sb.append(WeatherCodes.getDesciption(w.currentCode)); sb.append("</b>,");
				sb.append(" "); sb.append(w.currentTemp); 
				sb.append("&deg; <i>[real feel: "); sb.append(w.windChill); sb.append("&deg;]</i> <br/>");
				
				sb.append("Wind: &#10138;"); sb.append(w.windDirection);
				sb.append("&deg;, "); sb.append(w.windSpeed); sb.append(" km/h<br/>");
				
				sb.append("Humidity: "); sb.append(w.humidity); sb.append("%, ");
				sb.append("Pressure: "); sb.append(w.pressure); sb.append("mb<br/>");
				sb.append("Visibility: "); sb.append(w.visibility); sb.append(" km<br/>");
				
				sb.append("<br/><h3>Forecast:</h3>");
				
				for (int idx=0; idx< w.forecasts.size(); idx++)
				{
					DayForecast forecast = w.forecasts.get(idx);
					
					if (forecast != null)
					{
						String specialOpen = null, specialClose = null;
						
						int severityLevel = WeatherCodes.getSeverityLevel(forecast.code);
						
						if (severityLevel > 1)
						{
							if (severityLevel <= 4)
							{
								specialOpen = "<font color=\"red\">";
								specialClose = "</font>";
							}
							else if (severityLevel <= 6)
							{
								specialOpen = "<font color=\"red\">[!!] ";
								specialClose = "</font>";
							}
							else 
							{
								specialOpen = "<font color=\"red\">[!!!!] ";
								specialClose = "</font>";
							}
						}

						if (idx != 0)
							sb.append("<br/>");
						
						sb.append("<font color=\"blue\">");
						sb.append(forecast.date.getDate());
						sb.append("/");
						sb.append(forecast.date.getMonth());
						sb.append("/");
						sb.append(forecast.date.getYear()+1900);
						sb.append("</font>");
						sb.append(":<b> ");

						sb.append("<font color=\"#005f00\"><i>");
						sb.append(forecast.tempLow); sb.append("&ndash;");
						sb.append(forecast.tempHigh); sb.append("&deg;</i></font>, ");
					
						if (specialOpen != null)
							sb.append(specialOpen);
						sb.append(WeatherCodes.getDesciption(forecast.code)); sb.append("</b>");
						if (specialClose != null)
							sb.append(specialClose);
							
						sb.append("<br/>");
					}
				}
				
				tv.setText(Html.fromHtml(sb.toString()));

				tvlu.setText("Last updated: " + ((System.currentTimeMillis() - wb.lastUpadted) / 60000) + " minutes ago");
			}
			else
			{
				tv.setText("No weather data");
				tvlu.setText("Last updated: never");
			}
			
			
			return rootView;
		}
	}

}
