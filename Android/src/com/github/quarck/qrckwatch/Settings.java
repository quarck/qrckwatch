/*
 * Copyright (c) 2014, Sergey Parshin, quarck@gmail.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.github.quarck.qrckwatch;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Settings
{
	private Context context = null;

	private static final String[] WEATHER_LOC = {"wl1", "wl2", "wl3", "wl4", "wl5"};
	private static final String[] WEATHER_LOC_ENABLED = {"wle1", "wle2", "wle3", "wle4", "wle5"};
	
	private SharedPreferences prefs = null;

	public Settings(Context ctx)
	{
		context = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public int getWeatherLoc(int idx)
	{
		return prefs.getInt(WEATHER_LOC[idx], 0); 
	}

	public void setWeatherLoc(int idx, int loc)
	{
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(WEATHER_LOC[idx], loc);		
		editor.commit();
	}
	
	public boolean getWeatherLocEnabled(int idx)
	{
		return prefs.getBoolean(WEATHER_LOC_ENABLED[idx], false); 
	}

	public void setWeatherLocEnabled(int idx, boolean val)
	{
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(WEATHER_LOC_ENABLED[idx], val);		
		editor.commit();
	}

	public int[] getWeatherLocations()
	{
		int[] ret = null;
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();

		for (int i=0; i<5; ++i)
		{
			int loc = prefs.getInt(WEATHER_LOC[i], 0);
			if (loc != 0 && prefs.getBoolean(WEATHER_LOC_ENABLED[i], false)) 
				tmp.add(loc);
		}
		
		ret = new int[tmp.size()];
		for (int idx = 0; idx < tmp.size(); idx ++)
			ret[idx] = tmp.get(idx).intValue();
		
		return ret;
	}
}
