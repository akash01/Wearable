package com.example.gestureplayer;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	
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
