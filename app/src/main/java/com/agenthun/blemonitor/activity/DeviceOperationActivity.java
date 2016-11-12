package com.agenthun.blemonitor.activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.agenthun.blemonitor.R;
import com.agenthun.blemonitor.bean.base.HistoryData;
import com.agenthun.blemonitor.bean.base.HistoryDataDBUtil;
import com.agenthun.blemonitor.connectivity.ble.ACSUtility;
import com.agenthun.blemonitor.model.protocol.ESealOperation;
import com.agenthun.blemonitor.model.utils.LocationType;
import com.agenthun.blemonitor.model.utils.SocketPackage;
import com.agenthun.blemonitor.model.utils.StateExtraType;
import com.agenthun.blemonitor.utils.DataLogUtils;
import com.agenthun.blemonitor.view.BottomSheetDialogView;
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
    private static final short SHAKE_ALARM = 256 * 5; //振动量阈值

    private static final int ACTION_TYPE_UNLOCK = 0;
    private static final int ACTION_TYPE_TEMPERATURE = 1;
    private static final int ACTION_TYPE_HUMIDITY = 2;
    private static final int ACTION_TYPE_SHAKE = 3;
    private static final int ACTION_TYPE_SEND_MESSAGE = 4;
    private static final int ACTION_TYPE_RECEIVE_MESSAGE = 5;

    private ACSUtility.blePort mCurrentPort;
    private ACSUtility utility;
    private boolean utilEnable = false;
    private boolean isPortOpen = false;

    private boolean isSlowShow = false;
    private boolean isFastShow = false;
    private boolean isRecording = false;

    private AppCompatDialog mProgressDialog;

    @Bind(R.id.fab)
    CheckableFab fab;

    @Bind(R.id.queryFab)
    FloatingActionButton queryFab;

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
    private boolean isReconnect = false;

    private int id = 0x12345678;
    private int rn = 0xABABABAB;
    private int key = 0x87654321;

    private static final byte extraPackageBegin = 0x02;
    private static final byte extraPackageDataLength = 0x31;
    private static final byte extraPackageCommand = 0x40;
    private static final byte extraPackageTotalLength = 36 + 15;

    private HistoryDataDBUtil historyDataDBUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_operation);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_device_operation_menu, menu);
        if (!isRecording) {
            menu.findItem(R.id.menu_record_start).setVisible(true);
            menu.findItem(R.id.menu_record_stop).setVisible(false);
        } else {
            menu.findItem(R.id.menu_record_start).setVisible(false);
            menu.findItem(R.id.menu_record_stop).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_record_start:
                allowRecord(true);
                break;
            case R.id.menu_record_stop:
                allowRecord(false);
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupDatabase();
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

        //关闭文件保存
        if (isRecording) {
            allowRecord(false);
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        utilEnable = false;

        //关闭文件保存
        if (isRecording) {
            allowRecord(false);
        }
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

    protected void allowRecord(boolean enable) {
        if (enable) {
            DataLogUtils.logToFileInit();
            isRecording = true;
            Log.d(TAG, "已开启本地保存功能");
        } else {
            DataLogUtils.logToFileFinish();
            isRecording = false;
            Log.d(TAG, "已关闭本地保存功能");
        }
        invalidateOptionsMenu();
    }

    @OnClick(R.id.fab)
    public void onSendSmgDataBtnClick() {
        Log.d(TAG, "onSendSmgDataBtnClick() returned: ");
        //发送短信内容数据报文

        if (ismEdited()) {
            Log.d(TAG, "ismEdited onClick() returned: ");
        }

        String smg = smsEditMessage.getText().toString().trim();

        if (smg.length() <= 15) {
            byte[] smgData = ESealOperation.operationSendSmgData(smg, (short) (smg.length() << 1));
            sendData(smgData);
            adjustFab(false);
        } else if (smg.length() > 15) {
            Snackbar.make(fab, getString(R.string.fail_device_send_data_overlength), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    @OnClick(R.id.queryFab)
    public void onQueryHistoryBtnClick() {
//        Log.d(TAG, "onQueryHistoryBtnClick() returned: ");
        if (historyDataDBUtil.getDatas().size() > 0) {
            showDataListByBottomSheet();
        } else {
            Snackbar.make(queryFab, getString(R.string.notify_no_history_data), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    private ACSUtility.IACSUtilityCallback callback = new ACSUtility.IACSUtilityCallback() {
        @Override
        public void utilReadyForUse() {
            Log.d(TAG, "utilReadyForUse() returned:");
            isReconnect = false;
            utilEnable = true;
            utility.openPort(mCurrentPort);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isPortOpen && utilEnable) {
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
            isPortOpen = bSuccess;
            if (!isReconnect) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
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
            } else {
                //尝试重新连接
                if (bSuccess) {
                    Snackbar.make(fab, getString(R.string.success_device_reconnection), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
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
        }

        @Override
        public void didClosePort(ACSUtility.blePort port) {
            Log.d(TAG, "didClosePort() returned: " + port._device.getAddress());
            isReconnect = true;
            utilEnable = true;
            utility.openPort(port);
            Log.d(TAG, "didClosePort() returned: trying to reconnect");
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

                Calendar calendar = Calendar.getInstance();
                StringBuffer time = new StringBuffer();
                time.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(calendar.getTime()));
                historyDataDBUtil.insertData(new HistoryData(ACTION_TYPE_SEND_MESSAGE, time.toString(), smsEditMessage.getText().toString().trim()));
                Log.d(TAG, "historyDataDBUtil insertData: " + historyDataDBUtil.getDatas().get(0).toString());

//                Snackbar.make(fab, getString(R.string.success_device_send_data), Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
            } else {
                Snackbar.make(fab, getResources().getString(R.string.fail_device_send_data), Snackbar.LENGTH_SHORT)
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

            socketPackageReceived.packageExtraReceive(socketPackageReceived, packageToSend, mDataQueue, mLocationQueue);

            while (!mDataQueue.isEmpty()) {
                Log.d(TAG, "is OK");

                final ByteBuffer buffer = mDataQueue.poll();

                //开始解析额外数据报文
                final StateExtraType stateExtraType = new StateExtraType();
                ESealOperation.operationGetStateExtraData(buffer, stateExtraType);

                Calendar calendar = stateExtraType.getCalendar();
                final StringBuffer time = new StringBuffer();
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
                if (!TextUtils.isEmpty(stateExtraType.getSmsMessage())
                        && !TextUtils.equals(textSmsMessage.getText(), stateExtraType.getSmsMessage())) {
                    historyDataDBUtil.insertData(new HistoryData(ACTION_TYPE_RECEIVE_MESSAGE, textCurrentTime.getText().toString(), stateExtraType.getSmsMessage()));
                    Log.d(TAG, "historyDataDBUtil insertData: " + historyDataDBUtil.getDatas().get(0).toString());
                }

                textSmsMessage.setText(stateExtraType.getSmsMessage());

                if (!isSlowShow) {
                    isSlowShow = true;
                    if (stateExtraType.isTemperatureAlarm() || stateExtraType.isHumidityAlarm()) {
                        StringBuilder msg = new StringBuilder(time.toString()
                                + "\r\n\r\n锁状态 " + isLockStringQuery);
                        if (stateExtraType.isTemperatureAlarm()) msg.append("\r\n\r\n温度指数超标");
                        if (stateExtraType.isHumidityAlarm()) msg.append("\r\n\r\n湿度指数超标");

                        Log.d(TAG, "AlertDialog.Builder");
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                        builder.setTitle(getResources().getString(R.string.device_alarm_title))
                                .setMessage(msg.toString())
                                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "AlertDialog onClick()");

                                        if (stateExtraType.isTemperatureAlarm()) {
                                            historyDataDBUtil.insertData(new HistoryData(ACTION_TYPE_TEMPERATURE, textCurrentTime.getText().toString(), ""));
                                            Log.d(TAG, "historyDataDBUtil insertData: " + historyDataDBUtil.getDatas().get(0).toString());
                                        }
                                        if (stateExtraType.isHumidityAlarm()) {
                                            historyDataDBUtil.insertData(new HistoryData(ACTION_TYPE_HUMIDITY, textCurrentTime.getText().toString(), ""));
                                            Log.d(TAG, "historyDataDBUtil insertData: " + historyDataDBUtil.getDatas().get(0).toString());
                                        }
                                    }
                                }).show();
                    }
                }

                if (!isFastShow) {
                    isFastShow = true;
                    boolean isNotifyDialog = false;
                    StringBuilder msg = new StringBuilder();

                    if (!stateExtraType.isLocked()) {
//                        isNotifyDialog = true;

                        historyDataDBUtil.insertData(new HistoryData(ACTION_TYPE_UNLOCK, textCurrentTime.getText().toString(), ""));
                        Log.d(TAG, "historyDataDBUtil insertData: " + historyDataDBUtil.getDatas().get(0).toString());
                    }
                    if (stateExtraType.getShakeX() >= SHAKE_ALARM
                            || stateExtraType.getShakeY() >= SHAKE_ALARM
                            || stateExtraType.getShakeZ() >= SHAKE_ALARM) {
                        isNotifyDialog = true;
                        msg.append("振动量超标");

                        historyDataDBUtil.insertData(new HistoryData(ACTION_TYPE_SHAKE, textCurrentTime.getText().toString(), ""));
                        Log.d(TAG, "historyDataDBUtil insertData: " + historyDataDBUtil.getDatas().get(0).toString());
                    }

                    if (isNotifyDialog) {
                        isNotifyDialog = false;

                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                        builder.setTitle(getResources().getString(R.string.device_alarm_title))
                                .setMessage(msg.toString())
                                .setPositiveButton(R.string.text_ok, null).show();
                    }

                    //5 minutes
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isFastShow = false;
                        }
                    }, 300000);
                }

//                //握手响应
//                byte[] settingData = {0x02, 0x03, 0x30, 0x00, 0x35};
//                sendData(settingData);
            }

            while (!mLocationQueue.isEmpty()) {
                ByteBuffer buffer = mLocationQueue.poll();

                LocationType locationType = new LocationType();
                ESealOperation.operationGetLocationData(buffer, locationType);
                String location = locationType.getLatitude() / 1000000.0 + " " + locationType.getLatitudeType() + " " +
                        locationType.getLontitude() / 1000000.0 + " " + locationType.getLontitudeType();
                Log.d(TAG, "didPackageReceived() returned: " + location);

                if (isRecording) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(textTemperature.getText() + " ");
                    sb.append(location + " ");
                    sb.append(textHumidity.getText() + " ");
                    sb.append(textlocked.getText() + " ");
                    sb.append(textShakeX.getText() + " ");
                    sb.append(textShakeY.getText() + " ");
                    sb.append(textShakeZ.getText());
                    Log.d(TAG, "location string=" + sb.toString());
                    DataLogUtils.logToFile(DataLogUtils.LOCATION_TYPE, sb.toString());
                }
            }
        }

        @Override
        public void heartbeatDebug() {

        }
    };

    private SocketPackage socketPackageReceived = new SocketPackage();
    private Queue<ByteBuffer> mDataQueue = new LinkedList<>();
    private Queue<ByteBuffer> mLocationQueue = new LinkedList<>();

    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(DeviceOperationActivity.this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_device_connecting);
        mProgressDialog.setTitle(getResources().getString(R.string.device_connecting));
        mProgressDialog.setCancelable(false);
        return mProgressDialog;
    }

    private void sendData(byte[] data) {
        utility.writePort(data);
    }

    private void showDataListByBottomSheet() {
        BottomSheetDialogView.show(DeviceOperationActivity.this, historyDataDBUtil.getDatas());
    }


    private void setupDatabase() {
        historyDataDBUtil = HistoryDataDBUtil.getInstance(this);
    }
}
