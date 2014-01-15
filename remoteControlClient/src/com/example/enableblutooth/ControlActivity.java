package com.example.enableblutooth;

import java.io.IOException;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ControlActivity extends Activity {
	BluetoothDevice mDevice;
	BluetoothService mBluetoothService = null;
	Button []  mButtons = new Button[KeyButtons.KEY_COUNT.ordinal()];
	public enum KeyButtons {
        OK, BACK, UP, DOWN, LEFT, RIGHT, KEY_COUNT;
    }
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == mButtons[KeyButtons.OK.ordinal()]) {
				mBluetoothService.sendKey(66);
			} else if(v == mButtons[KeyButtons.BACK.ordinal()]) {
				mBluetoothService.sendKey(4);
			} else if(v == mButtons[KeyButtons.UP.ordinal()]) {
				mBluetoothService.sendKey(19);
			} else if(v == mButtons[KeyButtons.DOWN.ordinal()]) {
				mBluetoothService.sendKey(20);
			} else if(v == mButtons[KeyButtons.LEFT.ordinal()]) {
				mBluetoothService.sendKey(21);
			} else if(v == mButtons[KeyButtons.RIGHT.ordinal()]) {
				mBluetoothService.sendKey(22);
			}
		}
	};
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBluetoothService = ((BluetoothService.LocalBinder)service).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
    };
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		String devName = bundle.getString("device_name");
		setTitle(devName);
		mButtons[KeyButtons.OK.ordinal()] = (Button) findViewById(R.id.ok_btn);
		mButtons[KeyButtons.BACK.ordinal()] = (Button) findViewById(R.id.back_btn);
		mButtons[KeyButtons.UP.ordinal()] = (Button) findViewById(R.id.up_btn);
		mButtons[KeyButtons.DOWN.ordinal()] = (Button) findViewById(R.id.down_btn);
		mButtons[KeyButtons.LEFT.ordinal()] = (Button) findViewById(R.id.left_btn);
		mButtons[KeyButtons.RIGHT.ordinal()] = (Button) findViewById(R.id.right_btn);
		for(int i = 0; i < KeyButtons.KEY_COUNT.ordinal(); i++){
    		 mButtons[i].setOnClickListener(clickListener);
    	 }
	    this.bindService(new Intent(this, BluetoothService.class),
                this.serviceConnection, BIND_AUTO_CREATE);
	}
}
