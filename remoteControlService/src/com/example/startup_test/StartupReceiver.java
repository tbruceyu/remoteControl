package com.example.startup_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver{
	public static final String TAG = "MainActivity";
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "Received!!!");
			Intent i = new Intent("com.example.startup_test.PbapService");
			context.startService(i);
			
		}
	}
}
