package com.agenthun.blemonitor.model.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Queue;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/11 下午6:13.
 */
public class SocketPackage {
    private static final String TAG = "SocketPackage";

    private static final short SOCKET_HEAD_SIZE = 10;
    private static final int SOCKET_LEAD = 0xFFFFFFFF;

    private static final byte SOCKET_LEAD_BYTE = (byte) 0xFF;
    private static final byte SOCKET_LEAD_EXTRA_BYTE = (byte) 0x02;
    private static final int SOCKET_LEAD_EXTRA_COUNT = 0;
    private static final short SOCKET_HEAD_EXTRA_NO_DATA_SIZE = 4;
    private static final int SOCKET_EXTRA_COUNT = SOCKET_HEAD_EXTRA_NO_DATA_SIZE + 47;

    int flag;
    int count;
    boolean ok;
    byte[] data;

    public SocketPackage() {
        this.flag = 0;
        this.count = 0;
        this.ok = false;
    }

    public SocketPackage(int flag, int count, byte[] data) {
        this.flag = flag;
        this.count = count;
        this.data = data;
    }

    public byte[] packageAddHeader(int port, int len, byte[] pdata) {
        ByteBuffer buffer = ByteBuffer.allocate(SOCKET_HEAD_SIZE + len);
        buffer.putInt(SOCKET_LEAD);
        short crc = Crc.getCRC16((short) (port & 0xffff), (short) (len & 0xffff));
        buffer.putShort(crc);
        buffer.putShort((short) port);
        buffer.putShort((short) len);
        buffer.put(pdata);
        return buffer.array();
    }

    public int packageReceive(SocketPackage socketPackage, byte[] pdata) {
        int res = 0;
        for (int i = 0; i < pdata.length; i++) {
            if ((pdata[i] == SOCKET_LEAD_BYTE) && (socketPackage.getFlag() == 0)) {
                socketPackage.setFlag(1);
                socketPackage.setCount(0);
            } else {
                if ((socketPackage.getFlag() != 0) && (socketPackage.getFlag() != 1)) {
                    socketPackage.setFlag(0);
                }
            }

            if ((socketPackage.getFlag() == 1) && (socketPackage.getCount() == 0)) {
                int len = (int) SOCKET_HEAD_SIZE + (((int) pdata[i + 8]) << 4) + (int) pdata[i + 9];
                socketPackage.data = new byte[len];
            }
            if (socketPackage.getFlag() == 1) {
                socketPackage.setData(socketPackage.getCount(), pdata[i]);
                socketPackage.setCount(socketPackage.getCount() + 1);
            }
        }
        if (socketPackage.getCount() == socketPackage.getData().length) {
            res = 1;
        } else {
            res = 0;
        }
        return res;
    }

    //接收北斗主板与操作面板通信协议数据帧
    public int packageExtraReceive(SocketPackage socketPackage, byte[] pdata, Queue<ByteBuffer> queue) {
        int res = 0;
        for (int i = 0; i < pdata.length; i++) {
            if ((pdata[i] == SOCKET_LEAD_EXTRA_BYTE) && (socketPackage.getFlag() == 0)) {
                socketPackage.setFlag(1);
                socketPackage.setCount(0);

                int len = SOCKET_EXTRA_COUNT;
                socketPackage.data = new byte[len];
            } else {
                if ((socketPackage.getFlag() != 0) && (socketPackage.getFlag() != 1)) {
                    socketPackage.setFlag(0);
                }
            }

            if ((socketPackage.getFlag() == 1)) {
                socketPackage.setData(socketPackage.getCount(), pdata[i]);

                if (socketPackage.getCount() == SOCKET_LEAD_EXTRA_COUNT) {
                    if (pdata[i] != SOCKET_LEAD_EXTRA_BYTE) {
                        socketPackage.setFlag(0);
                        continue;
                    }
                }

                if (socketPackage.getCount() == SOCKET_EXTRA_COUNT - 1) {
                    if ((pdata[i] & 0xff) != Crc.simpleSumCRC(socketPackage.getData(), socketPackage.getCount() + 1)) {
                        socketPackage.setFlag(0);
                        continue;
                    } else {
//                        Log.d(TAG, "is OK");
                        ByteBuffer buffer = ByteBuffer.allocate(SOCKET_EXTRA_COUNT - SOCKET_HEAD_EXTRA_NO_DATA_SIZE);
                        buffer.put(socketPackage.getData(), SOCKET_HEAD_EXTRA_NO_DATA_SIZE - 1, SOCKET_EXTRA_COUNT - SOCKET_HEAD_EXTRA_NO_DATA_SIZE);
                        queue.offer(buffer);
                    }
                }

                if (socketPackage.getCount() >= SOCKET_EXTRA_COUNT - 1) {
                    socketPackage.setFlag(0);
                } else {
                    socketPackage.setCount(socketPackage.getCount() + 1);
                }
            }
        }
        return res;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setData(int position, byte pdata) {
        this.data[position] = pdata;
    }
}
