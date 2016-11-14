package com.agenthun.blemonitor;

import com.agenthun.blemonitor.model.protocol.ESealOperation;
import com.agenthun.blemonitor.model.utils.LocationType;
import com.agenthun.blemonitor.model.utils.SocketPackage;
import com.agenthun.blemonitor.model.utils.StateExtraType;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private static final String TAG = "ExampleUnitTest";

    private SocketPackage socketPackageReceived = new SocketPackage();
    private Queue<ByteBuffer> mDataQueue = new LinkedList<>();
    private Queue<ByteBuffer> mLocationQueue = new LinkedList<>();

    /*
    pdata:
        0x02, 0x31, 0x40, 0x16, 0x04, 0x14, 0x20, 0x00, 0x00, 0x12,
        0x13, 0x01, 0x11, 0x00, 0x12, 0x00, 0x13, 0x00, 0x4E, 0x0B,
        0x4E, 0x2A, 0x8D, 0xEF, 0x53, 0xE3, 0x53, 0xF3, 0x8F, 0x6C,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0xDF
    短信内容为“下个路口右转”，(UTF-8)
     */
    @Test
    public void doPackageReceive() {
        byte[] data = new byte[]{
                0x02, 0x14, 0x41, (byte) 0xDD, (byte) 0xA8, (byte) 0xD9, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x82, 0x6B, 0x3D,
                0x07, 0x00, 0x00, 0x00, 0x00, 0x4E, 0x45, 0x78,

                0x02, 0x0C, 0x41, (byte) 0xDD, (byte) 0xA8, (byte) 0xD9, 0x01, (byte) 0x82, 0x6B, 0x3D, 0x07, 0x4E, 0x45, 0x70,

                0x02, 0x31, 0x40, 0x16, 0x04, 0x14, 0x20, 0x00, 0x00, 0x12,
                0x13, 0x01, 0x11, 0x00, 0x12, 0x00, 0x13, 0x00, 0x4E, 0x0B,
                0x4E, 0x2A, (byte) 0x8D, (byte) 0xEF, 0x53, (byte) 0xE3, 0x53, (byte) 0xF3, (byte) 0x8F, 0x6C,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte) 0xDF
        };

        socketPackageReceived.packageExtraReceive(socketPackageReceived, data, mDataQueue, mLocationQueue);

        while (!mDataQueue.isEmpty()) {
            final ByteBuffer buffer = mDataQueue.poll();

            //开始解析额外数据报文
            StateExtraType stateExtraType = new StateExtraType();
            ESealOperation.operationGetStateExtraData(buffer, stateExtraType);

            Calendar calendar = stateExtraType.getCalendar();
            final StringBuffer time = new StringBuffer();
            time.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(calendar.getTime()));
        }

        while (!mLocationQueue.isEmpty()) {
            ByteBuffer buffer = mLocationQueue.poll();

            LocationType locationType = new LocationType();
            ESealOperation.operationGetLocationData(buffer, locationType);
            String location = locationType.getLatitude() / 1000000.0 + " " + locationType.getLatitudeType() + " " +
                    locationType.getLontitude() / 1000000.0 + " " + locationType.getLontitudeType();
        }
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}