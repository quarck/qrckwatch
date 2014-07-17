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
		
		weatherCodesMap.put(0, new WeatherCode(0, "Tornado", 10));
		weatherCodesMap.put(1, new WeatherCode(1, "Tropical Storm", 10));
		weatherCodesMap.put(2, new WeatherCode(2, "Hurricane", 10));
		weatherCodesMap.put(3, new WeatherCode(3, "Severe Thunderstorms", 6));
		weatherCodesMap.put(4, new WeatherCode(4, "Thunderstorms", 4));
		weatherCodesMap.put(5, new WeatherCode(5, "Mixed Rain and Snow", 4));
		weatherCodesMap.put(6, new WeatherCode(6, "Mixed Rain and Sleet", 4));
		weatherCodesMap.put(7, new WeatherCode(7, "Mixed Snow and Sleet", 4));
		weatherCodesMap.put(8, new WeatherCode(8, "Freezing Drizzle", 4));
		weatherCodesMap.put(9, new WeatherCode(9, "Drizzle", 3));
		weatherCodesMap.put(10, new WeatherCode(10, "Freezing Rain", 4));
		
		weatherCodesMap.put(11, new WeatherCode(11, "Showers", 4));
		weatherCodesMap.put(12, new WeatherCode(12, "Showers", 4));
		weatherCodesMap.put(13, new WeatherCode(13, "Snow Flurries", 4));
		weatherCodesMap.put(14, new WeatherCode(14, "Light Snow Showers", 4));
		weatherCodesMap.put(15, new WeatherCode(15, "Blowing Snow", 5));
		weatherCodesMap.put(16, new WeatherCode(16, "Snow", 4));
		weatherCodesMap.put(17, new WeatherCode(17, "Hail", 6));
		weatherCodesMap.put(18, new WeatherCode(18, "Sleet", 4));
		weatherCodesMap.put(19, new WeatherCode(19, "Dust", 4));
		weatherCodesMap.put(20, new WeatherCode(20, "Foggy", 2));
		
		weatherCodesMap.put(21, new WeatherCode(21, "Haze", 1));
		weatherCodesMap.put(22, new WeatherCode(22, "Smoky", 1));
		weatherCodesMap.put(23, new WeatherCode(23, "Blustery", 2));
		weatherCodesMap.put(24, new WeatherCode(24, "Windy", 1));
		weatherCodesMap.put(25, new WeatherCode(25, "Cold", 2));
		weatherCodesMap.put(26, new WeatherCode(26, "Cloudy", 0));
		weatherCodesMap.put(27, new WeatherCode(27, "Mostly Cloudy (Night)", 0));
		weatherCodesMap.put(28, new WeatherCode(28, "Mostly Cloudy (Day)", 0));
		weatherCodesMap.put(29, new WeatherCode(29, "Partly Cloudy (Night)", 0));
		weatherCodesMap.put(30, new WeatherCode(30, "Partly Cloudy (Day)", 0));
		
		weatherCodesMap.put(31, new WeatherCode(31, "Clear (Night)", 0));
		weatherCodesMap.put(32, new WeatherCode(32, "Sunny", 0));
		weatherCodesMap.put(33, new WeatherCode(33, "Fair (Night)", 0));
		weatherCodesMap.put(34, new WeatherCode(34, "Fair (Day)", 0));
		weatherCodesMap.put(35, new WeatherCode(35, "Mixed Rain and Hail", 4));
		weatherCodesMap.put(36, new WeatherCode(36, "Hot", 2));
		weatherCodesMap.put(37, new WeatherCode(37, "Isolated Thunderstorms", 5));
		weatherCodesMap.put(38, new WeatherCode(38, "Scattered Thunderstorms", 5));
		weatherCodesMap.put(39, new WeatherCode(39, "Scattered Thunderstorms", 5));
		weatherCodesMap.put(40, new WeatherCode(40, "Scattered Showers", 4));
		
		weatherCodesMap.put(41, new WeatherCode(41, "Heavy Snow", 5));
		weatherCodesMap.put(42, new WeatherCode(42, "Scattered Snow Showers", 4));
		weatherCodesMap.put(43, new WeatherCode(43, "Heavy Snow", 5));
		weatherCodesMap.put(44, new WeatherCode(44, "Partly Cloudy", 0));
		weatherCodesMap.put(45, new WeatherCode(45, "Thundershowers", 5));
		weatherCodesMap.put(46, new WeatherCode(46, "Snow Showers", 5));
		weatherCodesMap.put(47, new WeatherCode(47, "Isolated Thundershowers", 5));

		weatherCodesMap.put(3200, new WeatherCode(3200, "Not Available", 0));
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
		String desc = "Unknown";
		
		WeatherCode c = getWeatherCode(code);
		if (c != null)
			desc = c.name;
		
		return desc;
	}
}
