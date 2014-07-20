package com.github.quarck.qrckwatch.weather;

import java.util.ArrayList;
import java.util.Date;

public class Weather 
{
	public static class DayForecast
	{
		public Date date = null;
		public float tempLow = 0.0f;
		public float tempHigh = 0.0f;
		public int code = -1;
		public String conditionText = null;
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append("date="); sb.append(date); sb.append(";");
			sb.append("tempLow="); sb.append(tempLow); sb.append(";");
			sb.append("tempHigh="); sb.append(tempHigh); sb.append(";");
			sb.append("code="); sb.append(code); sb.append(";");
			sb.append("conditionText="); sb.append(conditionText); sb.append(";");
		
			return sb.toString();
		}		
	}

	public float windChill = 0.0f; 
	public float windDirection = 0.0f; 
	public float windSpeed = 0.0f;
	
	public float humidity = 0.0f;
	public float visibility = 0.0f;
	public float pressure = 0.0f;
	public int rising = 0;
	
	public String country = null;
	public String city = null;

	public String currentCondition = null;
	public int currentCode = -1;
	public float currentTemp = 0.0f;
	public String currentDateText = null;
	
	public ArrayList<DayForecast> forecasts = new ArrayList<DayForecast>();
	
	public DayForecast forecast = null;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("windChill="); sb.append(windChill); sb.append(";");
		sb.append("windDirection="); sb.append(windDirection); sb.append(";");
		sb.append("windSpeed="); sb.append(windSpeed); sb.append(";");
		sb.append("humidity="); sb.append(humidity); sb.append(";");
		sb.append("visibility="); sb.append(visibility); sb.append(";");
		sb.append("pressure="); sb.append(pressure); sb.append(";");
		sb.append("rising="); sb.append(rising); sb.append(";");
		sb.append("currentCondition="); sb.append(currentCondition); sb.append(";");
		sb.append("currentCode="); sb.append(currentCode); sb.append(";");
		sb.append("currentTemp="); sb.append(currentTemp); sb.append(";");
		sb.append("currentDateText="); sb.append(currentDateText); sb.append(";");
		sb.append("forecast="); 

		for (int idx = 0; idx < forecasts.size(); idx ++)
		{
			DayForecast df = forecasts.get(idx);

			sb.append(idx); sb.append(":["); sb.append(df);sb.append("]");
		}
		
		sb.append(";");
		
		return sb.toString();
	}	
}
