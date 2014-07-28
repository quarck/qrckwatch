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
import android.widget.ToggleButton;

public class MainActivity extends Activity
{
	private static String TAG = "MainActivity";

	private Settings settings = null;
	
	private EditText textView[] = new EditText[5];
	private ToggleButton toggleButton[] = new ToggleButton[5];
	
	private TextView textViewLastUpdated = null;

	private void saveAll()
	{
		for (int i=0; i<5; ++i)
		{
			try
			{
				settings.setWeatherLoc(i, Integer.valueOf(textView[i].getText().toString()));
			}
			catch (Exception ex)
			{
			}
		}
		
		for (int i=0; i<5; ++i)
			settings.setWeatherLocEnabled(i, toggleButton[i].isChecked());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Lw.d(TAG, "onCreateView");

		settings = new Settings(this);
		
		textView[0] = ((EditText)findViewById(R.id.editText1));
		textView[1] = ((EditText)findViewById(R.id.editText2));
		textView[2] = ((EditText)findViewById(R.id.editText3));
		textView[3] = ((EditText)findViewById(R.id.editText4));
		textView[4] = ((EditText)findViewById(R.id.editText5));

		toggleButton[0] = ((ToggleButton)findViewById(R.id.tbLoc1));
		toggleButton[1] = ((ToggleButton)findViewById(R.id.tbLoc2));
		toggleButton[2] = ((ToggleButton)findViewById(R.id.tbLoc3));
		toggleButton[3] = ((ToggleButton)findViewById(R.id.tbLoc4));
		toggleButton[4] = ((ToggleButton)findViewById(R.id.tbLoc5));

		for (int i=0; i<5; ++i)
			textView[i].setText(Integer.toString(settings.getWeatherLoc(i)));

		for (int i=0; i<5; ++i)
			toggleButton[i].setChecked(settings.getWeatherLocEnabled(i));
		
		textViewLastUpdated = ((TextView)findViewById(R.id.textViewLastUpdated));
		
		textViewLastUpdated.setText("Since last: "  + Long.toString(WeatherService.secondsSinceUpdate()));
		
		
		OnClickListener listener = 
			new OnClickListener() 
			{
				@Override
				public void onClick(View arg0)
				{
					saveAll();
					WeatherService.runWeatherUpdate(MainActivity.this);
				}
			};
		
		((Button)findViewById(R.id.buttonSaveWeather)).setOnClickListener(listener);

		((ToggleButton)findViewById(R.id.tbLoc1)).setOnClickListener(listener);
		((ToggleButton)findViewById(R.id.tbLoc2)).setOnClickListener(listener);
		((ToggleButton)findViewById(R.id.tbLoc3)).setOnClickListener(listener);
		((ToggleButton)findViewById(R.id.tbLoc4)).setOnClickListener(listener);
		((ToggleButton)findViewById(R.id.tbLoc5)).setOnClickListener(listener);
		
		
		((Button)findViewById(R.id.buttonL1W)).setOnClickListener( 
				new OnClickListener()  { @Override public void onClick(View arg0) { displayWeather(0); } } 
			);
		((Button)findViewById(R.id.buttonL2W)).setOnClickListener( 
				new OnClickListener()  { @Override public void onClick(View arg0) { displayWeather(1); } } 
			);
		((Button)findViewById(R.id.buttonL3W)).setOnClickListener( 
				new OnClickListener()  { @Override public void onClick(View arg0) { displayWeather(2); } } 
			);
		((Button)findViewById(R.id.buttonL4W)).setOnClickListener( 
				new OnClickListener()  { @Override public void onClick(View arg0) { displayWeather(3); } } 
			);
		((Button)findViewById(R.id.buttonL5W)).setOnClickListener( 
				new OnClickListener()  { @Override public void onClick(View arg0) { displayWeather(4); } } 
			);

		
		((Button)findViewById(R.id.buttonCfg)).setOnClickListener( 
				new OnClickListener()  { @Override public void onClick(View arg0) { configSecurity(); } } 
			);

		//
	}
	
	protected void configSecurity()
	{
		Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
		startActivity(intent);
	}

	private void displayWeather(int locCode)
	{
		Intent in = new Intent(this, WeatherDetailActivity.class);
		in.putExtra("w", locCode);
		this.startActivity(in);
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

	}
}
