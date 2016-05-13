package com.agenthun.blemonitor.activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.agenthun.blemonitor.R;
import com.agenthun.blemonitor.connectivity.ble.ACSUtility;
import com.agenthun.blemonitor.model.protocol.ESealOperation;
import com.agenthun.blemonitor.model.utils.SocketPackage;
import com.agenthun.blemonitor.model.utils.StateExtraType;
import com.agenthun.blemonitor.view.CheckableFab;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/9 下午7:22.
 */
public class DeviceOperationActivity extends AppCompatActivity {
    private static final String TAG = "DeviceOperationActivity";

    private static final int DEVICE_SETTING = 1;
    private static final long TIME_OUT = 30000;

    private ACSUtility.blePort mCurrentPort;
    private ACSUtility utility;
    private boolean utilEnable = false;
    private boolean isPortOpen = false;

    private AppCompatDialog mProgressDialog;

    @Bind(R.id.fab)
    CheckableFab fab;

    @Bind(R.id.current_time)
    AppCompatTextView textCurrentTime;
    @Bind(R.id.temperature)
    AppCompatTextView textTemperature;
    @Bind(R.id.humidity)
    AppCompatTextView textHumidity;
    @Bind(R.id.locked)
    AppCompatTextView textlocked;
    @Bind(R.id.shake_x)
    AppCompatTextView textShakeX;
    @Bind(R.id.shake_y)
    AppCompatTextView textShakeY;
    @Bind(R.id.shake_z)
    AppCompatTextView textShakeZ;
    @Bind(R.id.sms_message)
    AppCompatTextView textSmsMessage;

    @Bind(R.id.sms_edit_message)
    AppCompatEditText smsEditMessage;

    private Handler mHandler = new Handler();
    private Runnable mHideFabRunnable;
    private boolean mEdited = false;
    private boolean isChanged = false;

    private int id = 0x12345678;
    private int rn = 0xABABABAB;
    private int key = 0x87654321;

    private static final byte extraPackageBegin = 0x02;
    private static final byte extraPackageDataLength = 0x31;
    private static final byte extraPackageCommand = 0x40;
    private static final byte extraPackageTotalLength = 36 + 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_operation);
        ButterKnife.bind(this);

        allowSubmit(false);

        Bundle bundle = getIntent().getExtras();
        BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
        Log.d(TAG, "onCreate() returned: " + device.getAddress());

        utility = new ACSUtility(this, callback);
        mCurrentPort = utility.new blePort(device);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(device.getAddress());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getProgressDialog().show();

        smsEditMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(smsEditMessage.getText())) {
                    allowSubmit(true);
                } else {
                    allowSubmit(false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (utilEnable) {
            utilEnable = false;
            utility.closePort();
            isPortOpen = false;
            utility.closeACSUtility();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void adjustFab(final boolean settingCorrect) {
        fab.setChecked(settingCorrect);
        mHideFabRunnable = new Runnable() {
            @Override
            public void run() {
                fab.hide();
                if (!settingCorrect) {
//                    getConfigure();
//                    onBackPressed();
                }
            }
        };
        mHandler.postDelayed(mHideFabRunnable, 500);
    }

    protected void allowSubmit(boolean edited) {
        if (null != fab) {
            if (edited) {
                fab.show();
            } else {
                fab.hide();
            }
            mEdited = edited;
        }
    }

    public boolean ismEdited() {
        return mEdited;
    }

    @OnClick(R.id.fab)
    public void onSendSmgDataBtnClick() {
        Log.d(TAG, "onSendSmgDataBtnClick() returned: ");
        //发送短信内容数据报文

        if (ismEdited()) {
            Log.d(TAG, "ismEdited onClick() returned: ");
        }

        String smg = smsEditMessage.getText().toString().trim();
        byte[] smgData = ESealOperation.operationSendSmgData(smg, (short) (smg.length() << 1));
        sendData(smgData);

        adjustFab(false);
    }

/*    @OnClick(R.id.card_seting)
    public void onSettingBtnClick() {
        //配置信息
        Intent intent = new Intent(DeviceOperationActivity.this, DeviceSettingActivity.class);
        startActivityForResult(intent, DEVICE_SETTING);
    }

    @OnClick(R.id.card_lock)
    public void onLockBtnClick() {
        Log.d(TAG, "onLockBtnClick() returned: ");
        //发送上封操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.put(ESealOperation.operationOperation(id, rn, key,
                ESealOperation.POWER_ON,
                ESealOperation.SAFE_LOCK)
        );

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION,
                buffer.array()
        );
        sendData(data);
    }

    @OnClick(R.id.card_unlock)
    public void onUnlockBtnClick() {
        Log.d(TAG, "onUnlockBtnClick() returned: ");
        //发送解封操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.put(ESealOperation.operationOperation(id, rn, key,
                ESealOperation.POWER_ON,
                ESealOperation.SAFE_UNLOCK)
        );

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION,
                buffer.array()
        );
        sendData(data);
    }

    @OnClick(R.id.card_query_status)
    public void onQueryStatusBtnClick() {
        Log.d(TAG, "onQueryStatusBtnClick() returned: ");
        //发送查询操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.put(ESealOperation.operationQuery(id, rn, key));

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY,
                buffer.array()
        );
        sendData(data);
    }

    @OnClick(R.id.card_query_info)
    public void onQueryInfoBtnClick() {
        Log.d(TAG, "onQueryInfoBtnClick() returned: ");
        //发送位置请求信息操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        buffer.put(ESealOperation.operationInfo(id, rn, key));

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO,
                buffer.array()
        );
        sendData(data);
    }*/

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_SETTING && resultCode == RESULT_OK) {
            DetailParcelable detail = data.getExtras().getParcelable(DetailParcelable.EXTRA_DEVICE);
            Log.d(TAG, "onActivityResult() returned: " + detail.toString());

            int period = ESealOperation.PERIOD_DEFAULT;
            if (detail.getFrequency() != null && detail.getFrequency().length() != 0) {
                period = Integer.parseInt(detail.getFrequency());
            }
            //发送配置操作报文
            ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.putInt(id);
            buffer.putInt(rn);
            buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.put(ESealOperation.operationConfig(id, rn, key,
                    period,
                    ESealOperation.WINDOW_DEFAULT,
                    ESealOperation.CHANNEL_DEFAULT,
                    new SensorType())
            );

            SocketPackage socketPackage = new SocketPackage();
            byte[] settingData = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                    10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG,
                    buffer.array()
            );
            sendData(settingData);
        }
    }*/

    private ACSUtility.IACSUtilityCallback callback = new ACSUtility.IACSUtilityCallback() {
        @Override
        public void utilReadyForUse() {
            Log.d(TAG, "utilReadyForUse() returned:");
            utilEnable = true;
            utility.openPort(mCurrentPort);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isPortOpen) {
                        getProgressDialog().cancel();
                        new AlertDialog.Builder(DeviceOperationActivity.this)
                                .setTitle(mCurrentPort._device.getName())
                                .setMessage(R.string.time_out_device_connection)
                                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onBackPressed();
                                    }
                                }).show();
                    }
                }
            }, TIME_OUT);
        }

        @Override
        public void didFoundPort(final ACSUtility.blePort newPort, final int rssi) {

        }

        @Override
        public void didFinishedEnumPorts() {
        }

        @Override
        public void didOpenPort(final ACSUtility.blePort port, Boolean bSuccess) {
            Log.d(TAG, "didOpenPort() returned: " + bSuccess);
            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
            isPortOpen = bSuccess;
            if (bSuccess) {
                getProgressDialog().cancel();
                builder.setTitle(port._device.getName())
                        .setMessage(R.string.success_device_connection)
                        .setPositiveButton(R.string.text_ok, null).show();
            } else {
                getProgressDialog().cancel();
                builder.setTitle(port._device.getName())
                        .setMessage(R.string.fail_device_connection)
                        .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        }).show();
            }
        }

        @Override
        public void didClosePort(ACSUtility.blePort port) {
            Log.d(TAG, "didClosePort() returned: " + port._device.getAddress());
        }

        @Override
        public void didPackageSended(boolean succeed) {
            Log.d(TAG, "didPackageSended() returned: " + succeed);
            if (succeed) {
                mHideFabRunnable = new Runnable() {
                    @Override
                    public void run() {
                        fab.setChecked(true);
                        fab.show();
                    }
                };
                mHandler.postDelayed(mHideFabRunnable, 2000);

//                Snackbar.make(fab, getString(R.string.success_device_send_data), Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
            } else {
                Snackbar.make(fab, getString(R.string.fail_device_send_data), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }

        @Override
        public void didPackageReceived(ACSUtility.blePort port, byte[] packageToSend) {
/*            StringBuffer sb = new StringBuffer();
            for (byte b : packageToSend) {
                if ((b & 0xff) <= 0x0f) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(b & 0xff) + " ");
            }
            Log.d(TAG, sb.toString());*/

            socketPackageReceived.packageExtraReceive(socketPackageReceived, packageToSend, queue);

            while (!queue.isEmpty()) {
                Log.d(TAG, "is OK");

                final ByteBuffer buffer = queue.poll();

                //开始解析额外数据报文
                StateExtraType stateExtraType = new StateExtraType();
                ESealOperation.operationGetStateExtraData(buffer, stateExtraType);

                Calendar calendar = stateExtraType.getCalendar();
                StringBuffer time = new StringBuffer();
                time.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(calendar.getTime()));

                String isLockStringQuery = stateExtraType.isLocked() ?
                        getString(R.string.device_reply_lock) : getString(R.string.device_reply_unlock);

                textCurrentTime.setText(time.toString());
                textTemperature.setText(String.valueOf(stateExtraType.getTemperature()));
                textHumidity.setText(String.valueOf(stateExtraType.getHumidity()));
                textlocked.setText(String.valueOf(isLockStringQuery));
                textShakeX.setText(String.valueOf(stateExtraType.getShakeX() / 256f));
                textShakeY.setText(String.valueOf(stateExtraType.getShakeY() / 256f));
                textShakeZ.setText(String.valueOf(stateExtraType.getShakeZ() / 256f));
//                textShakeX.setText(Integer.toHexString(stateExtraType.getShakeX()));
//                textShakeY.setText(Integer.toHexString(stateExtraType.getShakeY()));
//                textShakeZ.setText(Integer.toHexString(stateExtraType.getShakeZ()));
                textSmsMessage.setText(stateExtraType.getSmsMessage());

                if (stateExtraType.isTemperatureAlarm() || stateExtraType.isHumidityAlarm()) {
                    StringBuilder msg = new StringBuilder(time.toString()
                            + "\r\n\r\n锁状态 " + isLockStringQuery);
                    if (stateExtraType.isTemperatureAlarm()) msg.append("\r\n\r\n温度指数超标");
                    if (stateExtraType.isHumidityAlarm()) msg.append("\r\n\r\n湿度指数超标");

                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                    builder.setTitle(R.string.device_alarm_title)
                            .setMessage(msg.toString())
                            .setPositiveButton(R.string.text_ok, null).show();
                }

//                //握手响应
//                byte[] settingData = {0x02, 0x03, 0x30, 0x00, 0x35};
//                sendData(settingData);
            }
        }

        @Override
        public void heartbeatDebug() {

        }
    };

    private SocketPackage socketPackageReceived = new SocketPackage();
    private Queue<ByteBuffer> queue = new LinkedList<>();

    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(DeviceOperationActivity.this, AppCompatDelegate.MODE_NIGHT_AUTO);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_device_connecting);
        mProgressDialog.setTitle(getString(R.string.device_connecting));
        return mProgressDialog;
    }

    private void sendData(byte[] data) {
        utility.writePort(data);
    }
}