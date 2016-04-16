package com.agenthun.eseal.model.utils;

import java.nio.ByteBuffer;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/11 下午6:13.
 */
public class SocketPackage {
    private static final short SOCKET_HEAD_SIZE = 10;
    private static final int SOCKET_LEAD = 0xFFFFFFFF;
    private static final byte SOCKET_LEAD_BYTE = (byte) 0xFF;
    private static final byte SOCKET_LEAD_EXTRA_BYTE = (byte) 0x02;

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

    public int packageExtraReceive(SocketPackage socketPackage, byte[] pdata) {
        int res = 0;
        if (socketPackage.isOk() == false) {
            for (int i = 0; i < pdata.length; i++) {
                if ((pdata[i] == SOCKET_LEAD_EXTRA_BYTE) && (socketPackage.getFlag() == 0) && (socketPackage.isOk() == false)) {
                    socketPackage.setFlag(1);
                    socketPackage.setCount(0);
                } else {
                    if ((socketPackage.getFlag() != 0) && (socketPackage.getFlag() != 1)) {
                        socketPackage.setFlag(0);
                        socketPackage.setOk(false);
                    }
                }

                if ((socketPackage.getFlag() == 1) && (socketPackage.getCount() == 0)) {
                    int len = 3 + 32 + 1 + 15;
                    socketPackage.data = new byte[len];
                }
                if ((socketPackage.isOk() == false) && (socketPackage.getFlag() == 1) && (socketPackage.getCount() < socketPackage.getData().length)) {
                    socketPackage.setData(socketPackage.getCount(), pdata[i]);
                    socketPackage.setCount(socketPackage.getCount() + 1);
                }
            }

            if (socketPackage.getCount() == socketPackage.getData().length) {
                socketPackage.setCount(0);
                res = 1;
                socketPackage.setOk(true);
            } else {
                res = 0;
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
