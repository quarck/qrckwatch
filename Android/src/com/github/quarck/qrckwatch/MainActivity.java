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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private static String TAG = "MainActivity";

	private Settings settings = null;
	
	private boolean shownSecurityPane = false;
	
	private EditText textView1 = null;
	private EditText textView2 = null;
	private EditText textView3 = null;
	private EditText textView4 = null;
	private EditText textView5 = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Lw.d(TAG, "onCreateView");

		settings = new Settings(this);
		
		textView1 = ((EditText)findViewById(R.id.editText1));
		textView2 = ((EditText)findViewById(R.id.editText2));
		textView3 = ((EditText)findViewById(R.id.editText3));
		textView4 = ((EditText)findViewById(R.id.editText4));
		textView5 = ((EditText)findViewById(R.id.editText5));
		
		textView1.setText(Integer.toString(settings.getWeatherLoc1()));
		textView2.setText(Integer.toString(settings.getWeatherLoc2()));
		textView3.setText(Integer.toString(settings.getWeatherLoc3()));
		textView4.setText(Integer.toString(settings.getWeatherLoc4()));
		textView5.setText(Integer.toString(settings.getWeatherLoc5()));
		
		((Button)findViewById(R.id.buttonSaveWeather)).setOnClickListener(
				new OnClickListener() 
				{
					@Override
					public void onClick(View arg0)
					{
						settings.setWeatherLoc1(Integer.valueOf(textView1.getText().toString()));
						settings.setWeatherLoc2(Integer.valueOf(textView2.getText().toString()));
						settings.setWeatherLoc3(Integer.valueOf(textView3.getText().toString()));
						settings.setWeatherLoc4(Integer.valueOf(textView4.getText().toString()));
						settings.setWeatherLoc5(Integer.valueOf(textView5.getText().toString()));
						
						WeatherService.runWeatherUpdate(MainActivity.this);
					}
				}
			);
	}
	
	@Override
	public void onStart()
	{
		Lw.d(TAG, "onStart()");
		super.onStart();
	}

	@Override
	public void onStop()
	{
		Lw.d(TAG, "onStop()");
		super.onStop();
	}
	
	@Override 
	public void onPause()
	{
		Lw.d(TAG, "onPause");
		super.onPause();
	}
	
	@Override 
	public void onResume()
	{
		Lw.d(TAG, "onResume");
		super.onResume();

		if (!shownSecurityPane)
		{
			shownSecurityPane = true;
			Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
			startActivity(intent);
		}
	}
}
