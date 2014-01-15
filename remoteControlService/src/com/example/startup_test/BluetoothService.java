package com.example.startup_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class BluetoothService extends Service {
	public static final String TAG = "MainActivity";
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	BluetoothAdapter mAdapter;
	BluetoothServerSocket mBluetoothServerSocket = null;
	BluetoothSocket mSocket;
	private int mState;
	private ConnectedThread mConnectedThread;
	AcceptThread mAcceptThread = null;
	InputManager mInputManager = null;
	Method mInjectInputEventMethod = null;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "TestService");
		mAcceptThread = new AcceptThread();
		mAcceptThread.start();
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mInputManager = (InputManager) this.getSystemService(INPUT_SERVICE);
		try {
			mInjectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[] {
					InputEvent.class,
					int.class
			});
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
    private void injectKeyEvent(KeyEvent event) {
        try {
			mInjectInputEventMethod.invoke(mInputManager, event, 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private void sendKeyEvent(int keyCode) {
        long now = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
    }
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private synchronized void setState(int state) {
		Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
	}

	public synchronized void start() {
		Log.d(TAG, "start");
		// Cancel any thread attempting to make a connection
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		setState(STATE_LISTEN);
		// Start the thread to listen on a BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
	}

	private class AcceptThread extends Thread {
		public void run() {
			// Create a new listening server socket
			try {
				mBluetoothServerSocket = mAdapter
						.listenUsingInsecureRfcommWithServiceRecord(
								"remote_ctrl", MY_UUID_INSECURE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (mState != STATE_CONNECTED) {
				try {
					Log.d(TAG, "start accept!");
					mSocket = mBluetoothServerSocket.accept();
					mConnectedThread = new ConnectedThread(mSocket, null);
					mConnectedThread.start();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		public void cancel() {
			try {
				mBluetoothServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private final BluetoothSocket mmSocket;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			BufferedReader is=new BufferedReader(new InputStreamReader(mmInStream));
			int bytes;
			while (true) {
				try {
					String line=is.readLine();
					Log.d(TAG, "receive:"+line);
					String[] values = line.split(":");
					String key = values[1];
					int event = Integer.valueOf(key);
					sendKeyEvent(event);
				} catch (IOException e) {
					BluetoothService.this.start();
					break;
				}
			}
		}
	}
}
