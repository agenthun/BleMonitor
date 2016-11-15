package com.agenthun.blemonitor.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.agenthun.blemonitor.R;
import com.agenthun.blemonitor.fragment.ScanDeviceFragment;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final int SDK_PERMISSION_REQUEST = 127;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnFABClickListener != null) {
                    mOnFABClickListener.OnFABClickListener(view);
                }
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ScanDeviceFragment.newInstance()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(getResources().getString(R.string.device_alarm_title))
                    .setMessage(R.string.error_ble_not_supported)
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "AlertDialog ble not supported");
                            finish();
                        }
                    }).show();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(getResources().getString(R.string.device_alarm_title))
                    .setMessage(R.string.error_ble_not_supported)
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "AlertDialog ble not supported");
                            finish();
                            return;
                        }
                    }).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPermissions(MainActivity.this);
    }

    private void getPermissions(Context context) {
        List<String> permissions = new ArrayList<>();

        // 蓝牙权限必须开启ACCESS_COARSE_LOCATIO或ACCESS_FINE_LOCATION权限，用户如果禁止，则每次进入都会申请
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // 读写外设为必须权限，用户如果禁止，则每次进入都会申请
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions((Activity) context, permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
        }
    }

    private boolean addPermission(Context context, List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //FABClick interface
    public interface OnFABClickListener {
        void OnFABClickListener(View view);
    }

    private OnFABClickListener mOnFABClickListener;

    public void setOnItemClickListener(OnFABClickListener mOnFABClickListener) {
        this.mOnFABClickListener = mOnFABClickListener;
    }
}
