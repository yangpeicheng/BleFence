package com.ypc.blefence.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ypc.blefence.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/** Adapter for holding devices found through scanning.
 *  Created by steven on 9/5/13.
 */
public class BleDevicesAdapter extends BaseAdapter {
    private final LayoutInflater inflater;

    private final ArrayList<BluetoothDevice> leDevices;
    private final HashMap<BluetoothDevice, Integer> rssiMap = new HashMap<BluetoothDevice, Integer>();
    private final HashMap<BluetoothDevice,Long> timeMap=new HashMap<>();

    public BleDevicesAdapter(Context context) {
        leDevices = new ArrayList<BluetoothDevice>();
        inflater = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        if (!leDevices.contains(device)) {
            leDevices.add(device);
        }
        rssiMap.put(device, rssi);
        timeMap.put(device,System.currentTimeMillis());
    }

    public BluetoothDevice getDevice(int position) {
        return leDevices.get(position);
    }

    public void clear() {
        leDevices.clear();
    }

    @Override
    public int getCount() {
        return leDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return leDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = inflater.inflate(R.layout.li_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = leDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText(""+rssiMap.get(device)+" dBm");

        return view;
    }

    public void removeExpiredDevice(){
        Log.i("adapter","remove");
        long current=System.currentTimeMillis();
        List<BluetoothDevice> expiredList=new LinkedList<>();
        Iterator<Map.Entry<BluetoothDevice, Long>> entries=timeMap.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<BluetoothDevice,Long> e=entries.next();
            if(current-e.getValue()>10000){
                expiredList.add(e.getKey());
            }
        }
        for(BluetoothDevice b:expiredList){
            leDevices.remove(b);
            timeMap.remove(b);
            rssiMap.remove(b);
        }
    }

    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
    }
}
