package com.github.quarck.qrckwatch.weather;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class Parser 
{
	@SuppressWarnings("deprecation")
	public static Weather parse(String rssXml)
	{
		Weather ret = new Weather();
		
		InputStream in = new ByteArrayInputStream(rssXml.getBytes());
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();;
		DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			org.w3c.dom.Document document = builder.parse(in);
			
			NodeList rootNodes = document.getChildNodes();
			
			for (int rootIdx = 0; rootIdx < rootNodes.getLength(); rootIdx++)
			{
				Node rootChild = rootNodes.item(rootIdx);
			
				if (rootChild.getNodeName().equals("rss") )
				{
					NodeList rssNodes = rootChild.getChildNodes();

					for (int rssIdx = 0; rssIdx < rssNodes.getLength(); rssIdx++ )
					{
						Node rssChild = rssNodes.item(rssIdx);
						
						if (rssChild.getNodeName().equals("channel"))
						{
							NodeList channelNodes = rssChild.getChildNodes();
							
							for (int channelIdx = 0; channelIdx < channelNodes.getLength(); channelIdx ++ )
							{
								Node channelChild = channelNodes.item(channelIdx);
								
								String name = channelChild.getNodeName();
								
								if (name.equals("yweather:wind"))
								{
									NamedNodeMap attrs = channelChild.getAttributes();
									
									Node chill = attrs.getNamedItem("chill");
									Node direction = attrs.getNamedItem("direction");
									Node speed = attrs.getNamedItem("speed");
									
									if (chill != null)
									{
										Log.d("WEATHER", "chill = " + chill.getNodeValue());
										ret.windChill = Float.valueOf(chill.getNodeValue());
									}
									if (direction != null)
									{
										Log.d("WEATHER", "direction = " + direction.getNodeValue());
										ret.windDirection = Float.valueOf(direction.getNodeValue());
									}
									if (speed != null)
									{
										Log.d("WEATHER", "speed = " + speed.getNodeValue());
										ret.windSpeed = Float.valueOf(speed.getNodeValue());
									}
								}
								else if (name.equals("yweather:atmosphere"))
								{
									NamedNodeMap attrs = channelChild.getAttributes();
	
									Node humidity = attrs.getNamedItem("humidity");
									Node visibility = attrs.getNamedItem("visibility");
									Node pressure = attrs.getNamedItem("pressure");
									Node rising = attrs.getNamedItem("rising");

									if (humidity != null)
									{
										Log.d("WEATHER", "humidity = " + humidity.getNodeValue());
										ret.humidity = Float.valueOf(humidity.getNodeValue());
									}
									if (visibility != null)
									{
										Log.d("WEATHER", "visibility = " + visibility.getNodeValue());
										ret.visibility = Float.valueOf(visibility.getNodeValue());
									}
									if (pressure != null)
									{
										Log.d("WEATHER", "pressure = " + pressure.getNodeValue());
										ret.pressure = Float.valueOf(pressure.getNodeValue());
									}
									if (rising != null)
									{
										Log.d("WEATHER", "rising = " + rising.getNodeValue());
										ret.rising = Integer.valueOf(rising.getNodeValue());
									}
								}
								else if (name.equals("item"))
								{
									NodeList itemChilds = channelChild.getChildNodes();
									
									for (int itemIdx = 0; itemIdx < itemChilds.getLength(); itemIdx ++ )
									{
										Node itemChild = itemChilds.item(itemIdx);
	
										if (itemChild.getNodeName().equals("yweather:condition"))
										{
											NamedNodeMap attrs = itemChild.getAttributes();

											Node text = attrs.getNamedItem("text");
										    Node code = attrs.getNamedItem("code");
										    Node temp = attrs.getNamedItem("temp");									

										    if (text != null)
										    {
												Log.d("WEATHER", "text = " + text.getNodeValue());
												ret.currentCondition = text.getNodeValue();
										    }
											if (code != null)
											{
												Log.d("WEATHER", "code = " + code.getNodeValue());
												ret.currentCode = Integer.valueOf(code.getNodeValue());
											}
											if (temp != null)
											{
												Log.d("WEATHER", "temp = " + temp.getNodeValue());
												ret.currentTemp = Float.valueOf(temp.getNodeValue());
											}
										}
										else if (itemChild.getNodeName().equals("yweather:forecast"))
										{
											NamedNodeMap attrs = itemChild.getAttributes();

											// parse forecast
								    	    Node text = attrs.getNamedItem("text");
								    	    Node code = attrs.getNamedItem("code");
								    	    Node low = attrs.getNamedItem("low");
								    	    Node high = attrs.getNamedItem("high");
								    	    Node date = attrs.getNamedItem("date");

											Weather.DayForecast df = new Weather.DayForecast();
											
											if (text != null)
											{
												Log.d("WEATHER", "text = " + text.getNodeValue());
												df.conditionText = text.getNodeValue();
											}
											if (code != null)
											{
												Log.d("WEATHER", "code = " + code.getNodeValue());
												df.code = Integer.valueOf(code.getNodeValue());
											}
											if (low != null)
											{
												Log.d("WEATHER", "low = " + low.getNodeValue());
												df.tempLow = Float.valueOf(low.getNodeValue());
											}
											if (high != null)
											{
												Log.d("WEATHER", "high = " + high.getNodeValue());
												df.tempHigh = Float.valueOf(high.getNodeValue());
											}
											if (date != null)
											{
												Log.d("WEATHER", "date = " + date.getNodeValue());
												df.date = ParseDate(date.getNodeValue());
											}

											ret.forecasts.add(df);
										}
									}
								}
							}
							
							break;
						}
					}
					

					break;
				}
			}			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			ret = null;
		}
		
		if (ret != null && ret.forecasts.size() > 0 )
		{
			Date now = new Date(System.currentTimeMillis());
			
			for (int idx = 0; idx < ret.forecasts.size(); idx ++ )
			{
				Date forecastDate = ret.forecasts.get(idx).date;
				
				if ((now.getDate() == forecastDate.getDate()) && 
					(now.getMonth() == forecastDate.getMonth()) &&
					(now.getYear() == forecastDate.getYear())
					)
				{
					ret.forecast = ret.forecasts.get(idx);
					Log.w("WEATHER", "Will use forecast for " + forecastDate);
					break;
				}
			}
		}
		
		if (ret != null)
		{
			Log.w("WEATHER", "Returning: " + ret.forecast.toString());
		}
				
		return ret;
	}


	private static Date ParseDate(String strDate) 
	{
		DateFormat df = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
		Date result;
		try 
		{
			result = df.parse(strDate);
			
			Log.d("WEATHER", "Parsed date: " + result);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
			result = null;
		}  
		
		return result;
	}
}
