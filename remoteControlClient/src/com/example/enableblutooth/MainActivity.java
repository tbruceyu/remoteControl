package com.example.enableblutooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public final static String TAG = "MainActivity";
	public static String ErrorMessage;
	private static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_TOAST = 5;
	Button btnSearch, btnExit;
	ListView lvBTDevices;
	ArrayAdapter<String> adtDevices;
	List<String> lstDevices = new ArrayList<String>();
	BluetoothAdapter btAdapt;
	String key = "123456";
	BluetoothDevice paireDevice;
	public static final String TOAST = "toast";
	public static BluetoothSocket btSocket;
	BluetoothService mBluetoothService = null;
	BluetoothDevice mTargetDevice = null;

	// ---------------------------------------------------
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBluetoothService = ((BluetoothService.LocalBinder)service).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBluetoothService = null;
		}
    };
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// if(!ListBluetoothDevice())finish();
		ErrorMessage = "";
		Boolean ret;
		// ---------------------------------------------------
		btnSearch = (Button) this.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new ClickEvent());
		btnExit = (Button) this.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new ClickEvent());
		// ListView及其数据源 适配器
		lvBTDevices = (ListView) this.findViewById(R.id.listView1);
		adtDevices = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, lstDevices);
		lvBTDevices.setAdapter(adtDevices);
		lvBTDevices.setOnItemClickListener(new ItemClickEvent());

		btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能

		// 注册Receiver来获取蓝牙设备相关的结果
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(ACTION_PAIRING_REQUEST);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(searchDevices, intent);
	    this.bindService(new Intent(this, BluetoothService.class),
                this.serviceConnection, BIND_AUTO_CREATE);
	}

	// ---------------------------------------------------

	private BroadcastReceiver searchDevices = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = null;
			// 搜索设备时，取得设备的MAC地址
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() == BluetoothDevice.BOND_NONE) {
					String str = "未配对|" + device.getName() + "|"
							+ device.getAddress() + "|Class:"
							+ device.getBluetoothClass().getDeviceClass();
					if (lstDevices.indexOf(str) == -1)// 防止重复添加
						lstDevices.add(str); // 获取设备名称和mac地址
					adtDevices.notifyDataSetChanged();
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (device.getBondState()) {
				case BluetoothDevice.BOND_BONDING:

					Log.d("123", "正在配对......");
					break;
				case BluetoothDevice.BOND_BONDED:
					Log.d("234", "完成配对");
					break;
				case BluetoothDevice.BOND_NONE:
					Log.d("345", "取消配对");
				default:
					break;
				}
			} else if (intent.getAction().equals(ACTION_PAIRING_REQUEST)) {
				Log.v("MainActivity", "recieve pairing request");
				try {
					BluetoothDevice device1 = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Boolean ret = (Boolean) device1.getClass()
							.getMethod("cancelPairingUserInput")
							.invoke(device1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	class ItemClickEvent implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(btAdapt.isDiscovering())btAdapt.cancelDiscovery();
			String str = lstDevices.get(arg2);
			String[] values = str.split("\\|");
			String address = values[2];
			// Log.e("address", values[2]);
			mTargetDevice = btAdapt.getRemoteDevice(address);
			mBluetoothService.connect(mTargetDevice);
			Intent intent = new Intent(MainActivity.this, ControlActivity.class); 
			Bundle bundle = new Bundle();
			bundle.putString("device_name", mTargetDevice.getName());
			intent.putExtras(bundle);
			MainActivity.this.startActivity(intent);
			// try {
			// if (paireDevice.getBondState() == BluetoothDevice.BOND_NONE) {
			// //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice
			// remoteDevice);
			// Method createBondMethod =
			// BluetoothDevice.class.getMethod("createBond");
			// Log.d("BlueToothTestActivity", "开始配对");
			// createBondMethod.invoke(paireDevice);
			//
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
	}

	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnSearch)// 搜索蓝牙设备，在BroadcastReceiver显示结果
			{
				if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// 如果蓝牙还没开启
					Toast.makeText(MainActivity.this, "请先打开蓝牙", 1000).show();
					return;
				}
				if (btAdapt.isDiscovering())
					btAdapt.cancelDiscovery();
				lstDevices.clear();
				Object[] lstDevice = btAdapt.getBondedDevices().toArray();
				for (int i = 0; i < lstDevice.length; i++) {
					BluetoothDevice device = (BluetoothDevice) lstDevice[i];
					String str = "已配对|" + device.getName() + "|"
							+ device.getAddress();
					lstDevices.add(str); // 获取设备名称和mac地址
					adtDevices.notifyDataSetChanged();
				}
				setTitle("本机蓝牙地址：" + btAdapt.getAddress());
				btAdapt.startDiscovery();
			} else if (v == btnExit) {
				try {
					if (btSocket != null)
						btSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				MainActivity.this.finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(searchDevices);
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void showMessage(String str) {
		Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	}

}