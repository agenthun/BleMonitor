package com.agenthun.blemonitor.bean;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @project BleMonitor
 * @authors agenthun
 * @date 16/6/3 22:00.
 */
public class HistoryDataDBHelper extends DBHelper {
    public static final String TABLE_NAME = "history";

    interface HistoryDataTable {
        String _ID = "_id";
        String ACTION_TYPE = "actionType";
        String CREATE_DATE_TIME = "createDatetime";
        String CONTENT = "content";
    }

    public HistoryDataDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
                .append(TABLE_NAME)
                .append(" ( ").append(HistoryDataTable._ID).append(" INTEGER PRIMARY KEY,")
                .append(HistoryDataTable.ACTION_TYPE).append(" Integer,")
                .append(HistoryDataTable.CREATE_DATE_TIME).append(" varchar(20),")
                .append(HistoryDataTable.CONTENT).append(" varchar(100));");
        db.execSQL(sb.toString());
    }
}
