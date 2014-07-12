package com.github.quarck.qrckwatch.weather;

import android.util.SparseArray;

public class WeatherCodes 
{
	public static class WeatherCode
	{
		public int code;
		public String name;
		public int severity; // 10 - tornado, 2 - rain, 0 - nothing
		
		public WeatherCode(int _code, String _name, int _severity)
		{
			code = _code;
			name = _name;
			severity = _severity;
		}
	}

	public static SparseArray<WeatherCode> weatherCodesMap;
	
	static 
	{
		weatherCodesMap = new SparseArray<WeatherCode>();
		
		weatherCodesMap.put(0, new WeatherCode(0, "tornado", 10));
		weatherCodesMap.put(1, new WeatherCode(1, "tropical storm", 10));
		weatherCodesMap.put(2, new WeatherCode(2, "hurricane", 10));
		weatherCodesMap.put(3, new WeatherCode(3, "severe thunderstorms", 5));
		weatherCodesMap.put(4, new WeatherCode(4, "thunderstorms", 3));
		weatherCodesMap.put(5, new WeatherCode(5, "mixed rain and snow", 3));
		weatherCodesMap.put(6, new WeatherCode(6, "mixed rain and sleet", 3));
		weatherCodesMap.put(7, new WeatherCode(7, "mixed snow and sleet", 3));
		weatherCodesMap.put(8, new WeatherCode(8, "freezing drizzle", 3));
		weatherCodesMap.put(9, new WeatherCode(9, "drizzle", 2));
		weatherCodesMap.put(10, new WeatherCode(10, "freezing rain", 3));
		
		weatherCodesMap.put(11, new WeatherCode(11, "showers", 2));
		weatherCodesMap.put(12, new WeatherCode(12, "showers", 2));
		weatherCodesMap.put(13, new WeatherCode(13, "snow flurries", 3));
		weatherCodesMap.put(14, new WeatherCode(14, "light snow showers", 2));
		weatherCodesMap.put(15, new WeatherCode(15, "blowing snow", 4));
		weatherCodesMap.put(16, new WeatherCode(16, "snow", 3));
		weatherCodesMap.put(17, new WeatherCode(17, "hail", 5));
		weatherCodesMap.put(18, new WeatherCode(18, "sleet", 3));
		weatherCodesMap.put(19, new WeatherCode(19, "dust", 3));
		weatherCodesMap.put(20, new WeatherCode(20, "foggy", 2));
		
		weatherCodesMap.put(21, new WeatherCode(21, "haze", 1));
		weatherCodesMap.put(22, new WeatherCode(22, "smoky", 1));
		weatherCodesMap.put(23, new WeatherCode(23, "blustery", 2));
		weatherCodesMap.put(24, new WeatherCode(24, "windy", 1));
		weatherCodesMap.put(25, new WeatherCode(25, "cold", 3));
		weatherCodesMap.put(26, new WeatherCode(26, "cloudy", 0));
		weatherCodesMap.put(27, new WeatherCode(27, "mostly cloudy (night)", 0));
		weatherCodesMap.put(28, new WeatherCode(28, "mostly cloudy (day)", 0));
		weatherCodesMap.put(29, new WeatherCode(29, "partly cloudy (night)", 0));
		weatherCodesMap.put(30, new WeatherCode(30, "partly cloudy (day)", 0));
		
		weatherCodesMap.put(31, new WeatherCode(31, "clear (night)", 0));
		weatherCodesMap.put(32, new WeatherCode(32, "sunny", 0));
		weatherCodesMap.put(33, new WeatherCode(33, "fair (night)", 0));
		weatherCodesMap.put(34, new WeatherCode(34, "fair (day)", 0));
		weatherCodesMap.put(35, new WeatherCode(35, "mixed rain and hail", 3));
		weatherCodesMap.put(36, new WeatherCode(36, "hot", 2));
		weatherCodesMap.put(37, new WeatherCode(37, "isolated thunderstorms", 4));
		weatherCodesMap.put(38, new WeatherCode(38, "scattered thunderstorms", 4));
		weatherCodesMap.put(39, new WeatherCode(39, "scattered thunderstorms", 4));
		weatherCodesMap.put(40, new WeatherCode(40, "scattered showers", 3));
		
		weatherCodesMap.put(41, new WeatherCode(41, "heavy snow", 4));
		weatherCodesMap.put(42, new WeatherCode(42, "scattered snow showers", 4));
		weatherCodesMap.put(43, new WeatherCode(43, "heavy snow", 4));
		weatherCodesMap.put(44, new WeatherCode(44, "partly cloudy", 0));
		weatherCodesMap.put(45, new WeatherCode(45, "thundershowers", 4));
		weatherCodesMap.put(46, new WeatherCode(46, "snow showers", 4));
		weatherCodesMap.put(47, new WeatherCode(47, "isolated thundershowers", 4));

		//
		weatherCodesMap.put(3200, new WeatherCode(3200, "not available", 0));
	}

	public static WeatherCode getWeatherCode(int code)
	{
		return weatherCodesMap.get(code, null);
	}

	public static int getSeverityLevel(int code)
	{
		int level = 0;
		
		WeatherCode c = getWeatherCode(code);
		if (c != null)
			level = c.severity;
		
		return level;
	}

	public static String getDesciption(int code)
	{
		String desc = "unknown";
		
		WeatherCode c = getWeatherCode(code);
		if (c != null)
			desc = c.name;
		
		return desc;
	}
}
