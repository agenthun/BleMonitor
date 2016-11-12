package com.agenthun.blemonitor.bean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @project BleMonitor
 * @authors agenthun
 * @date 16/6/3 22:05.
 */
public class HistoryDataDBUtil {
    private static final String TAG = "HistoryDataDBUtil";
    public static final String DATABASE_NAME = "history_data.db";
    public static final int DATABASE_VERSION = 1;

    private static HistoryDataDBUtil instance;
    private HistoryDataDBHelper historyDataDBHelper;
    private Context mContext;

    private HistoryDataDBUtil(Context context) {
        historyDataDBHelper = new HistoryDataDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static HistoryDataDBUtil getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryDataDBUtil(context);
        }
        return instance;
    }

    public static void destory() {
        if (instance != null) {
            instance.onDestory();
        }
    }

    public void onDestory() {
        instance = null;
        if (historyDataDBHelper != null) {
            historyDataDBHelper.close();
            historyDataDBHelper = null;
        }
    }

    public void deleteData(HistoryData historyData) {
        Cursor cursor = null;
        String where = HistoryDataDBHelper.HistoryDataTable._ID + " = '" + historyData.getId() + "'";
        cursor = historyDataDBHelper.query(HistoryDataDBHelper.TABLE_NAME, null, where, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            historyDataDBHelper.delete(HistoryDataDBHelper.TABLE_NAME, where, null);
            Log.i(TAG, "delete success");
        }
        if (cursor == null) {
            where = HistoryDataDBHelper.HistoryDataTable._ID + " = '" + historyData.getId()
                    + "' AND " + HistoryDataDBHelper.HistoryDataTable.ACTION_TYPE + " = '" + historyData.getActionType()
                    + "' AND " + HistoryDataDBHelper.HistoryDataTable.CREATE_DATE_TIME + " = '" + historyData.getCreateDatetime()
                    + "' AND " + HistoryDataDBHelper.HistoryDataTable.CONTENT + " = '" + historyData.getContent() + "'";
            cursor = historyDataDBHelper.query(HistoryDataDBHelper.TABLE_NAME, null, where, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                historyDataDBHelper.delete(historyDataDBHelper.TABLE_NAME, where, null);
                Log.i(TAG, "delete success");
            }
        }

        if (cursor != null) {
            cursor.close();
            historyDataDBHelper.close();
        }
    }

    public long insertData(HistoryData historyData) {
        long uri = 0;
        Cursor cursor = null;
        String where = HistoryDataDBHelper.HistoryDataTable._ID + " = '" + historyData.getId() + "'";
        cursor = historyDataDBHelper.query(HistoryDataDBHelper.TABLE_NAME, null, where, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            ContentValues contentValues = new ContentValues();
            contentValues.put(HistoryDataDBHelper.HistoryDataTable._ID, historyData.getId());
            contentValues.put(HistoryDataDBHelper.HistoryDataTable.ACTION_TYPE, historyData.getActionType());
            contentValues.put(HistoryDataDBHelper.HistoryDataTable.CREATE_DATE_TIME, historyData.getCreateDatetime());
            contentValues.put(HistoryDataDBHelper.HistoryDataTable.CONTENT, historyData.getContent());
            historyDataDBHelper.update(HistoryDataDBHelper.TABLE_NAME, contentValues, where, null);
            Log.i(TAG, "update");
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(HistoryDataDBHelper.HistoryDataTable._ID, historyData.getId());
            contentValues.put(HistoryDataDBHelper.HistoryDataTable.ACTION_TYPE, historyData.getActionType());
            contentValues.put(HistoryDataDBHelper.HistoryDataTable.CREATE_DATE_TIME, historyData.getCreateDatetime());
            contentValues.put(HistoryDataDBHelper.HistoryDataTable.CONTENT, historyData.getContent());
            uri = historyDataDBHelper.insert(HistoryDataDBHelper.TABLE_NAME, null, contentValues);
            Log.i(TAG, "insert");
        }
        if (cursor != null) {
            cursor.close();
        }
        historyDataDBHelper.close();
        return uri;
    }

    public List<HistoryData> getDatas() {
        List<HistoryData> historyDatas = null;
        Cursor cursor = historyDataDBHelper.query(HistoryDataDBHelper.TABLE_NAME, null, null, null, null, null, null);
        Log.i(TAG, cursor.getCount() + "");

        if (cursor == null) {
            return null;
        }
        historyDatas = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                HistoryData historyData = new HistoryData();
                historyData.setId(cursor.getString(cursor.getColumnIndexOrThrow(HistoryDataDBHelper.HistoryDataTable._ID)));
                historyData.setActionType(cursor.getInt(cursor.getColumnIndexOrThrow(HistoryDataDBHelper.HistoryDataTable.ACTION_TYPE)));
                historyData.setCreateDatetime(cursor.getString(cursor.getColumnIndexOrThrow(HistoryDataDBHelper.HistoryDataTable.CREATE_DATE_TIME)));
                historyData.setContent(cursor.getString(cursor.getColumnIndexOrThrow(HistoryDataDBHelper.HistoryDataTable.CONTENT)));
                historyDatas.add(0, historyData);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        historyDataDBHelper.close();
        return historyDatas;
    }

    public List<HistoryData> setDatas(List<HistoryData> lists) {
        Cursor cursor = null;
        if (lists != null && lists.size() > 0) {
            for (Iterator iterator = lists.iterator(); iterator.hasNext(); ) {
                HistoryData historyData = (HistoryData) iterator.next();
                insertData(historyData);
            }
        }
        if (cursor != null) {
            cursor.close();
            historyDataDBHelper.close();
        }
        return lists;
    }
}
