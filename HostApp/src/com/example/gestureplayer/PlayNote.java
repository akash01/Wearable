package com.example.gestureplayer;

import android.util.Log;
import android.widget.TextView;

public class PlayNote {

	public void NoteFile() {
		Log.d("PlayNote" ,"Completed");
		float f2 = 20;
		TextView localTextView;
		StringBuilder localStringBuilder;

		localTextView = MainActivity.note;
		localTextView.setText(Math.round(f2) + " play");
		
	}

}
