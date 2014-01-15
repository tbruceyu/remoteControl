package com.example.enableblutooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BluetoothService extends Service {
	private static final String TAG = "MainActivity";
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	private Handler mHandler = null;
	BluetoothAdapter mAdapter;
	BluetoothSocket mSocket;
    private int mState;
	private InputStream mInputStream = null;
	private OutputStream mOutputStream = null;
	private final IBinder mBinder = new LocalBinder();
	 public class LocalBinder extends Binder {
	        BluetoothService getService() {
	            return BluetoothService.this;
	        }
	    }
    public BluetoothService() {
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	public void Test() {
		Log.d(MainActivity.TAG, "hehe");
	}
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        try {
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
			mSocket.connect();
			mInputStream = mSocket.getInputStream();
			mOutputStream = mSocket.getOutputStream();
	        setState(STATE_CONNECTED);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public synchronized void disconnect() {
    	try {
			mSocket.close();
			setState(STATE_NONE);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public void sendKey(int keyCode) {
    	String buf = "key:"+keyCode+"\n";
    	try {
			mOutputStream.write(buf.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }
    public synchronized int getState() {
        return mState;
    }
}