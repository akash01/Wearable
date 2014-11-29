package com.example.gestureplayer;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener{
	// audio processing thread
	Thread td;
	// sampling rate
	int spr = 44100;
	// audio on and off
	boolean isRunning = true;
	int amp = 1000;
	int buffersize;
	double fr = 140.f;
	double ph = 0.0;
	double twopi = 8.*Math.atan(1.);
	
	// ui seekbar or slider
	SeekBar fSlider;
	double sliderval;
	private AudioTrack audioTrack;
	
	//acceloremeter sensor
	private SensorManager sensorManager;
	private boolean color = false;
	private View aView;
	private long lastUpdate;
	float accelationSquareRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fSlider = (SeekBar) findViewById(R.id.frequency);
		aView = (TextView) findViewById(R.id.acclerometer);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    lastUpdate = System.currentTimeMillis();
	    
		// create a listener for the sliderbar
		OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) sliderval = progress/(double)seekBar.getMax();
				System.out.println(sliderval);
			}
		};
		
		//set the listener on the slider
		fSlider.setOnSeekBarChangeListener(listener);
		// buffer size, holds the size of audio block to be output
		buffersize = AudioTrack.getMinBufferSize(spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
		// create audiotrack object
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT
									,buffersize,AudioTrack.MODE_STREAM);
		
		// signal buffer
		//samples[] = new short[buffersize];
		//int amp = 10000;
		
		//double fr = 440.f;
		
		
		// start audio 
		audioTrack.play();
	
/*		// start a new thread to make audio
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
		*/
		
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
		// register this class as a listener for the orientation and
	    // accelerometer sensors
	    sensorManager.registerListener(this,
	        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	        SensorManager.SENSOR_DELAY_NORMAL);
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
	protected void onPause() {
		// unregister listener
		super.onPause();
		sensorManager.unregisterListener(this);
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

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}
	private void getAccelerometer(SensorEvent event) {
	    float[] values = event.values;
	    // Movement
	    float x = values[0];
	    float y = values[1];
	    float z = values[2];

	    accelationSquareRoot = (x * x + y * y + z * z)
	    			/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
	    
	    
	    long actualTime = event.timestamp;
	    if (accelationSquareRoot >= 2) {
	    	if (actualTime - lastUpdate < 200) {
	    		return;
	    	}
	    	lastUpdate = actualTime;
	    	Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT).show();
	    	if (color) {
	    		aView.setBackgroundColor(Color.GREEN);
	    	} else {
	    		aView.setBackgroundColor(Color.RED);
	    	}
	    	color = !color;
	    }
	    //System.out.println(accelationSquareRoot);
		// start a new thread to make audio
		td = new Thread() {
			public void run() {
				// process priority
				setPriority(Thread.MAX_PRIORITY);
/*				// buffer size, holds the size of audio block to be output
				int buffersize = AudioTrack.getMinBufferSize(spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
				// create audiotrack object
				AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT
											,buffersize,AudioTrack.MODE_STREAM);
				// signal buffer
				short samples[] = new short[buffersize];
				//int amp = 10000;
				int amp = 1000;
				double twopi = 8.*Math.atan(1.);
				//double fr = 440.f;
				double fr = 140.f;
				double ph = 0.0;
				
				// start audio 
				audioTrack.play();*/
				short samples[] = new short[buffersize];
				// audio loop
				while(isRunning) {
					fr =  440 + 440*accelationSquareRoot;
					for(int i=0; i<buffersize;i++) {
						samples[i] = (short) (amp*Math.sin(ph));
						ph += twopi*fr/spr;
					}
					audioTrack.write(samples,0,buffersize);
				}
				System.out.println(accelationSquareRoot);
				
				audioTrack.stop();
				audioTrack.release();
			}
		};
		td.start();
	}
	
}
