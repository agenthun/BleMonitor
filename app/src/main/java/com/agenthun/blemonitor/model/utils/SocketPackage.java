package com.agenthun.blemonitor.model.utils;

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

    private static final byte SOCKET_DATA_TYPE_NORMAL = 0x40;
    private static final byte SOCKET_DATA_TYPE_LOCATION = 0x41;

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

    //接收北斗主板与操作面板通信协议数据帧: 接收数据
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
    public int packageExtraReceive(SocketPackage socketPackage, byte[] pdata, Queue<ByteBuffer> dataQueue) {
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
                        dataQueue.offer(buffer);
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

    //接收北斗主板与操作面板通信协议数据帧: 接收数据, 位置信息
    /*
    pdata:
        0x02, 0x31, 0x40, 0x16, 0x04, 0x14, 0x20, 0x00, 0x00, 0x12,
        0x13, 0x01, 0x11, 0x00, 0x12, 0x00, 0x13, 0x00, 0x4E, 0x0B,
        0x4E, 0x2A, 0x8D, 0xEF, 0x53, 0xE3, 0x53, 0xF3, 0x8F, 0x6C,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0xDF

    位置data:
        0x02, 0x0C, 0x41, 0xDD, 0xA8, 0xD9, 0x01, 0x82, 0x6B, 0x3D, 0x07, 0x4E, 0x45, 0x70

        0x02, 0x14, 0x41, 0xDD, 0xA8, 0xD9, 0x01, 0x00, 0x00, 0x00, 0x00, 0x82, 0x6B, 0x3D,
        0x07, 0x00, 0x00, 0x00, 0x00, 0x4E, 0x45, 0x78
    短信内容为“下个路口右转”，(UTF-8)
     */
    public int packageExtraReceive(SocketPackage socketPackage, byte[] pdata, Queue<ByteBuffer> dataQueue, Queue<ByteBuffer> locationQueue) {
        int res = 0;
        for (int i = 0; i < pdata.length; i++) {
            if ((pdata[i] == SOCKET_LEAD_EXTRA_BYTE) && (socketPackage.getFlag() == 0)) {
                socketPackage.setFlag(1);
                socketPackage.setCount(0);
            } else {
                if ((socketPackage.getFlag() != 0) && (socketPackage.getFlag() != 1)) {
                    socketPackage.setFlag(0);
                }
            }

            if (socketPackage.getFlag() == 1 && socketPackage.getCount() == 0 && i + 1 < pdata.length) {
                int len = 2 + pdata[i + 1];
                socketPackage.data = new byte[len];
            }

            if ((socketPackage.getFlag() == 1)) {
                socketPackage.setData(socketPackage.getCount(), pdata[i]);

                if (socketPackage.getCount() == socketPackage.getData().length - 1) {
                    if ((pdata[i] & 0xff) != Crc.simpleSumCRC(socketPackage.getData(), socketPackage.getCount() + 1)) {
                        socketPackage.setFlag(0);
                        continue;
                    } else {
//                        Log.d(TAG, "is OK");
                        byte[] data = socketPackage.getData();
                        ByteBuffer buffer = ByteBuffer.allocate(data.length - SOCKET_HEAD_EXTRA_NO_DATA_SIZE);
                        buffer.put(data, SOCKET_HEAD_EXTRA_NO_DATA_SIZE - 1, data.length - SOCKET_HEAD_EXTRA_NO_DATA_SIZE);
                        if (data[2] == SOCKET_DATA_TYPE_NORMAL) {
                            dataQueue.offer(buffer);
                        } else if (data[2] == SOCKET_DATA_TYPE_LOCATION) {
                            locationQueue.offer(buffer);
                        }
                    }
                }

                if (socketPackage.getCount() >= socketPackage.data.length - 1) {
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
