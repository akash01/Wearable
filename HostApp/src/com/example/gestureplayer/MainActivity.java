package com.example.gestureplayer;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	// audio processing thread
	Thread td;
	// sampling rate
	int spr = 44100;
	// audio on and off
	boolean isRunning = true;
	
	// ui seekbar or slider
	SeekBar fSlider;
	double sliderval;
	
	private PlayNote playNote;
	static TextView note;
	private Handler mHandler = new Handler();
	
	private Runnable mUpdateTimer = new Runnable() { 
		@Override
		public void run() {
			playNote.NoteFile();
			Log.d(getLocalClassName(), "void run");
			mHandler.postDelayed(mUpdateTimer, 200L);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		note = (TextView) findViewById(R.id.note);
		fSlider = (SeekBar) findViewById(R.id.frequency);
		
		// create a listener for the sliderbar
		OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) sliderval = progress/(double)seekBar.getMax();
			}
		};
		
		//set the listener on the slider
		fSlider.setOnSeekBarChangeListener(listener);
		
		// start a new thread to make audio
		td = new Thread() {
			public void run() {
				// process priority
				setPriority(Thread.MAX_PRIORITY);
				// buffer size, holds the size of audio block to be output
				int buffersize = AudioTrack.getMinBufferSize(spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
				// create audiotrack object
				AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT
											,buffersize,AudioTrack.MODE_STREAM);
				// signal buffer
				short samples[] = new short[buffersize];
				int amp = 10000;
				double twopi = 8.*Math.atan(1.);
				double fr = 440.f;
				double ph = 0.0;
				
				// start audio 
				audioTrack.play();
				// audio loop
				while(isRunning) {
					fr =  440 + 440*sliderval;
					for(int i=0; i<buffersize;i++) {
						samples[i] = (short) (amp*Math.sin(ph));
						ph += twopi*fr/spr;
					}
					audioTrack.write(samples,0,buffersize);
				}
				audioTrack.stop();
				audioTrack.release();
			}
		};
		td.start();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onStart() {
		super.onStart();
		Log.d(getLocalClassName(), "run function");
	}

	protected void onStop() {
		super.onStop();
	}
	
	protected void onResume() {
		super.onResume();
	}
	
	protected void onDestroy() {
		super.onDestroy();

		if (Build.VERSION.SDK_INT < 14)
			System.exit(0);
		
		// when app is closed audio is stopped
		isRunning = false;
		try {
			td.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		td = null;
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
