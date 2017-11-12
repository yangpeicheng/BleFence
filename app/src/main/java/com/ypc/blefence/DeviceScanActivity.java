package com.ypc.blefence;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.ypc.blefence.ble.BleDevicesAdapter;
import com.ypc.blefence.ble.BleDevicesScanner;
import com.ypc.blefence.ble.BleUtils;
import com.ypc.blefence.dialog.EnableBluetoothDialog;
import com.ypc.blefence.dialog.ErrorDialog;


public class DeviceScanActivity extends ListActivity implements ErrorDialog.ErrorDialogListener, EnableBluetoothDialog.EnableBluetoothDialogListener {

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 500;
	
	private BluetoothAdapter bluetoothAdapter;
	private BleDevicesScanner scanner;
	private BleDevicesAdapter bleDevicesListAdapter;
	private Handler clearHandler=new Handler();
	private Runnable mRunnable=new Runnable() {
		@Override
		public void run() {
			bleDevicesListAdapter.removeExpiredDevice();
			clearHandler.postDelayed(this,10000);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayShowHomeEnabled(false);

		setContentView(R.layout.device_scan_activity);
		
		final View emptyView = findViewById(R.id.empty_view);
		getListView().setEmptyView(emptyView);
		final int bleStatus = BleUtils.getBleStatus(getBaseContext());
		switch (bleStatus) {
		case BleUtils.STATUS_BLE_NOT_AVAILABLE:
			ErrorDialog.newInstance(R.string.dialog_error_no_ble).show(getFragmentManager(), ErrorDialog.TAG);
			return;
		case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
			ErrorDialog.newInstance(R.string.dialog_error_no_bluetooth).show(getFragmentManager(), ErrorDialog.TAG);
			return;
		default:
			bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
		}
		//初始化scanner
		scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				bleDevicesListAdapter.addDevice(device, rssi);
				bleDevicesListAdapter.notifyDataSetChanged();
			}
		});
		scanner.setScanPeriod(SCAN_PERIOD);
		clearHandler.postDelayed(mRunnable,10000);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (bluetoothAdapter == null) {
			return;
		}
		if (!bluetoothAdapter.isEnabled()) {
			final Fragment f = getFragmentManager().findFragmentByTag(EnableBluetoothDialog.TAG);
			if (f == null) {
				new EnableBluetoothDialog().show(getFragmentManager(), EnableBluetoothDialog.TAG);
			}
			return;
		}
		init();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//停止扫描
		if (scanner != null) {
			scanner.stop();
		}
		clearHandler.removeCallbacks(mRunnable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_scan, menu);
		
		if (scanner == null || !scanner.isScanning()) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_scan:
            bleDevicesListAdapter.clear();
            if (scanner != null)
                scanner.start();
            invalidateOptionsMenu();
            break;
        case R.id.menu_stop:
            if (scanner != null)
                scanner.stop();
            invalidateOptionsMenu();
            break;
        }
        return true;
	}
	
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//获取点击的Device
    	final BluetoothDevice device = bleDevicesListAdapter.getDevice(position);
		if (device == null) {
			return;
		}		
		//跳转到BluetoothBarrierActivity
		/*final Intent intent = new Intent(this, BluetoothBarrierActivity.class);
		intent.putExtra(BluetoothBarrierActivity.EXTRAS_DEVICE_NAME, device.getName());
		intent.putExtra(BluetoothBarrierActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
		startActivity(intent);*/
	}
    

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else {
                init();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	private void init() {
		//初始化bleDevicesListAdapter,开始扫描，初始化菜单
		if (bleDevicesListAdapter == null) {
			bleDevicesListAdapter = new BleDevicesAdapter(getBaseContext());
			setListAdapter(bleDevicesListAdapter);
		}
		scanner.start();
		invalidateOptionsMenu();
	}
	@Override
	public void onEnableBluetooth(EnableBluetoothDialog f) {
		bluetoothAdapter.enable();
		init();
	}

	@Override
	public void onCancel(EnableBluetoothDialog f) {
		finish();
	}

	@Override
	public void onDismiss(ErrorDialog f) {
		finish();
	}
	
}
