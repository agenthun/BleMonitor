package com.agenthun.blemonitor.bean.base;

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
            synchronized (HistoryDataDBUtil.class) {
                if (instance == null) {
                    instance = new HistoryDataDBUtil(context);
                }
            }
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
        String userId = (String) UserData.getObjectByKey(mContext, "objectId");
        String where = NoteDBHelper.NoteTable.USER_ID + " = '" + userId
                + "' AND " + NoteDBHelper.NoteTable.OBJECT_ID + " = '" + noteInfo.getObjectId() + "'";
        cursor = noteDBHelper.query(NoteDBHelper.TABLE_NAME, null, where, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            noteDBHelper.delete(NoteDBHelper.TABLE_NAME, where, null);
            Log.i(TAG, "delete success");
        }
        if (cursor == null) {
            where = NoteDBHelper.NoteTable.USER_ID + " = '" + userId
                    + "' AND " + NoteDBHelper.NoteTable.NOTE_TITLE + " = '" + noteInfo.getNoteTitle()
                    + "' AND " + NoteDBHelper.NoteTable.NOTE_COMPOSE + " = '" + noteInfo.getNoteCompose()
                    + "' AND " + NoteDBHelper.NoteTable.NOTE_CREATE_TIME + " = '" + noteInfo.getNoteCreateTime()
                    + "' AND " + NoteDBHelper.NoteTable.NOTE_COLOR + " = '" + noteInfo.getNoteColor() + "'";
            cursor = noteDBHelper.query(NoteDBHelper.TABLE_NAME, null, where, null, null, null, null);
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
        String userId = (String) UserData.getObjectByKey(mContext, "objectId");
        String where = HistoryDataDBHelper.HistoryDataTable.USER_ID + " = '" + userId
                + "' AND " + HistoryDataDBHelper.HistoryDataTable.OBJECT_ID + " = '" + noteInfo.getObjectId() + "'";
        cursor = noteDBHelper.query(NoteDBHelper.TABLE_NAME, null, where, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            ContentValues contentValues = new ContentValues();
            contentValues.put(NoteDBHelper.NoteTable.NOTE_TITLE, noteInfo.getNoteTitle());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_COMPOSE, noteInfo.getNoteCompose());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_CREATE_TIME, noteInfo.getNoteCreateTime());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_COLOR, noteInfo.getNoteColor());
            noteDBHelper.update(NoteDBHelper.TABLE_NAME, contentValues, where, null);
            Log.i(TAG, "update");
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NoteDBHelper.NoteTable.USER_ID, userId);
            contentValues.put(NoteDBHelper.NoteTable.OBJECT_ID, noteInfo.getObjectId());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_TITLE, noteInfo.getNoteTitle());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_COMPOSE, noteInfo.getNoteCompose());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_CREATE_TIME, noteInfo.getNoteCreateTime());
            contentValues.put(NoteDBHelper.NoteTable.NOTE_COLOR, noteInfo.getNoteColor());
            uri = noteDBHelper.insert(NoteDBHelper.TABLE_NAME, null, contentValues);
            Log.i(TAG, "insert");
        }
        if (cursor != null) {
            cursor.close();
            noteDBHelper.close();
        }
        return uri;
    }

    public List<HistoryData> queryDatas() {
        List<HistoryData> historyDatas = null;
        Cursor cursor = historyDataDBHelper.query(HistoryDataDBHelper.TABLE_NAME, null, null, null, null, null, null);
        Log.i(TAG, cursor.getCount() + "");

        if (cursor == null) {
            return null;
        }
        historyDatas = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HistoryData historyData = new HistoryData();
            historyData.setObjectId(cursor.getString(cursor.getColumnIndex(NoteDBHelper.NoteTable.OBJECT_ID)));
            historyData.setNoteTitle(cursor.getString(3));
            historyData.setNoteCompose(cursor.getString(4));
            historyData.setNoteCreateTime(cursor.getString(5));
            historyData.setNoteColor(cursor.getInt(6));
            historyDatas.add(0, historyData);
        }
        if (cursor != null) {
            cursor.close();
        }
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
