package com.github.quarck.qrckwatch.weather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkClient 
{	
	@SuppressWarnings("resource")
	public String getWeatherRss(String uri)
	{
		String ret = null;
		
		InputStream input = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HttpURLConnection connection = null;

		try 
		{
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) 
			{
				return null;
			}

			int fileLength = connection.getContentLength();

			input = connection.getInputStream();

			int total = 0;
			byte data[] = new byte[4096];

			while (total < fileLength || fileLength == -1)
			{
				int toRead = fileLength != -1 ? fileLength - total : data.length;

				if (toRead > data.length)
					toRead = data.length;

				int count = input.read(data);
				if (count == -1)
				{
					if (fileLength != -1)
						throw new Exception("Unexpected end");
					else
						break;
				}

				total += count;
				output.write(data, 0, count);
			}

			ret = output.toString();       	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try 
			{
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} 
			catch (IOException ignored) 
			{
			}

			if (connection != null)
				connection.disconnect();
		}

		return ret;
	}
}
