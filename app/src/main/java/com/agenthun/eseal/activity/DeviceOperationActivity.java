package com.agenthun.eseal.activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.agenthun.eseal.R;
import com.agenthun.eseal.connectivity.ble.ACSUtility;
import com.agenthun.eseal.model.protocol.ESealOperation;
import com.agenthun.eseal.model.utils.Crc;
import com.agenthun.eseal.model.utils.SocketPackage;
import com.agenthun.eseal.model.utils.StateExtraType;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

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

    @Bind(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

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

    private int id = 0x12345678;
    private int rn = 0xABABABAB;
    private int key = 0x87654321;

    private static final byte extraPackageBegin = 0x02;
    private static final byte extraPackageDataLength = 0x32;
    private static final byte extraPackageCommand = 0x30;
    private static final byte extraPackageTotalLength = 36;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_operation);
        ButterKnife.bind(this);

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
                Snackbar.make(nestedScrollView, getString(R.string.success_device_send_data), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(nestedScrollView, getString(R.string.fail_device_send_data), Snackbar.LENGTH_SHORT)
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

            if (socketPackageReceived.packageExtraReceive(socketPackageReceived, packageToSend) == 1) {
                Log.d(TAG, "didPackageReceived() returned: ok");
                socketPackageReceived.setFlag(0);
                socketPackageReceived.setCount(0);
                byte[] receiveData = socketPackageReceived.getData();
                int lenTotal = receiveData.length;
                Log.d(TAG, "getCount() returned: " + lenTotal);

                if ((receiveData[0] == extraPackageBegin) &&
                        (receiveData[1] == extraPackageDataLength) &&
                        (receiveData[2] == extraPackageCommand)) {
                    byte crc = (byte) Crc.simpleSumCRC(receiveData, extraPackageTotalLength - 1);
                    if (crc == (receiveData[extraPackageTotalLength - 1])) {
                        ByteBuffer buffer = ByteBuffer.allocate(extraPackageTotalLength - 4);
                        buffer.put(receiveData, 3, extraPackageTotalLength - 4);
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
                        textShakeX.setText(String.valueOf(stateExtraType.getShakeX()));
                        textShakeY.setText(String.valueOf(stateExtraType.getShakeY()));
                        textShakeZ.setText(String.valueOf(stateExtraType.getShakeZ()));
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

                        //握手响应
                        byte[] settingData = {0x02, 0x03, 0x30, 0x00, 0x35};
                        sendData(settingData);
                    }
                }
            }

            /*if (socketPackageReceived.packageReceive(socketPackageReceived, packageToSend) == 1) {
                Log.d(TAG, "didPackageReceived() returned: ok");
                socketPackageReceived.setFlag(0);
                socketPackageReceived.setCount(0);
                byte[] receiveData = socketPackageReceived.getData();
                int lenTotal = receiveData.length;
                Log.d(TAG, "getCount() returned: " + lenTotal);
                Encrypt.decrypt(id, rn, key, receiveData,
                        ESealOperation.ESEALBD_PROTOCOL_CMD_DATA_OFFSET,
                        lenTotal - ESealOperation.ESEALBD_PROTOCOL_CMD_DATA_OFFSET);

                ByteBuffer buffer = ByteBuffer.allocate(lenTotal);
                buffer.put(receiveData);


                short prococolPort = buffer.getShort(6);
                short type = buffer.getShort(ESealOperation.ESEALBD_PROTOCOL_CMD_DATA_OFFSET + 2);

                if ((prococolPort & 0xffff) == ESealOperation.ESEALBD_OPERATION_PORT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                    switch (type) {
                        case ESealOperation.ESEALBD_OPERATION_TYPE_REPLAY_QUERY:
                            Log.d(TAG, "ESEALBD_OPERATION_TYPE_REPLAY_QUERY");
                            StateType stateType = new StateType();
                            ESealOperation.operationQueryReplay(buffer, stateType);

                            String safeStringQuery = (stateType.getSafe() == 0 ?
                                    getString(R.string.device_reply_safe_0) :
                                    (stateType.getSafe() == 1 ?
                                            getString(R.string.device_reply_safe_1) : getString(R.string.device_reply_safe_2)));
                            String isLockStringQuery = stateType.isLocked() ?
                                    getString(R.string.device_reply_lock) : getString(R.string.device_reply_unlock);
                            builder.setTitle(R.string.device_reply_query_title)
                                    .setMessage("上传周期 " + stateType.getPeriod()
                                            + " s\r\n\r\n" + safeStringQuery
                                            + "\r\n\r\n锁状态 " + isLockStringQuery)
                                    .setPositiveButton(R.string.text_ok, null).show();
                            break;
                        case ESealOperation.ESEALBD_OPERATION_TYPE_REPLAY_INFO:
                            Log.d(TAG, "ESEALBD_OPERATION_TYPE_REPLAY_INFO");
                            PositionType positionType = new PositionType();
                            ESealOperation.operationInfoReplay(buffer, positionType);
                            Calendar calendar = positionType.getCalendar();
                            StringBuffer time = new StringBuffer();
                            if (positionType.getPosition() != null) {
                                time.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                        .format(calendar.getTime()));
                            } else {
                                time.append(getString(R.string.device_reply_info_time_error));
                            }
                            String safeStringInfo = (positionType.getSafe() == 0 ?
                                    getString(R.string.device_reply_safe_0) :
                                    (positionType.getSafe() == 1 ?
                                            getString(R.string.device_reply_safe_1) : getString(R.string.device_reply_safe_2)));
                            String isLockStringInfo = positionType.isLocked() ?
                                    getString(R.string.device_reply_lock) : getString(R.string.device_reply_unlock);
                            builder.setTitle(R.string.device_reply_info_title)
                                    .setMessage(time.toString()
                                            + "\r\n\r\n当前位置 " + positionType.getPosition()
                                            + "\r\n\r\n" + safeStringInfo
                                            + "\r\n\r\n锁状态 " + isLockStringInfo)
                                    .setPositiveButton(R.string.text_ok, null).show();
                            break;
                    }
                }
            }*/
        }

        @Override
        public void heartbeatDebug() {

        }
    };

    private SocketPackage socketPackageReceived = new SocketPackage();


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
