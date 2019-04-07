package com.su.workbox.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;

public class DbInfoProvider {

    private SQLiteDatabase mDb;
    private String mDatabaseName;
    private static DbInfoProvider sDbInfoProvider;

    private DbInfoProvider(@NonNull Context context, @NonNull String databaseName) {
        initDb(context, databaseName);
    }

    public static DbInfoProvider getInstance(@NonNull Context context, @NonNull String databaseName) {
        if (sDbInfoProvider == null) {
            sDbInfoProvider = new DbInfoProvider(context, databaseName);
        }
        if (sDbInfoProvider.mDb == null) {
            sDbInfoProvider.resetDb(context, databaseName);
        }
        return sDbInfoProvider;
    }

    public void resetDb(@NonNull Context context, @NonNull String databaseName) {
        if (TextUtils.equals(mDatabaseName, databaseName) && mDb != null) {
            return;
        }
        closeDb();
        initDb(context, databaseName);
    }

    public void closeDb() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    private void initDb(@NonNull Context context, @NonNull String databaseName) {
        mDatabaseName = databaseName;
        File filePath = context.getDatabasePath(databaseName);
        mDb = SQLiteDatabase.openDatabase(filePath.getPath(), null, SQLiteDatabase.OPEN_READONLY, null);
    }

    //获取库中所有表与视图
    public Cursor getAllTablesAndViews() {
        return mDb.query("sqlite_master",
                        new String[]{"name", "sql", "type"},
                        "(type='table' or type='view') and name<>'android_metadata' and name<>'sqlite_sequence'",
                        null,
                        null,
                        null,
                        null);
    }

    //获取库中所有触发器
    public Cursor getAllTriggers() {
        return mDb.query("sqlite_master",
                new String[]{"name", "tbl_name", "sql", "type"},
                "type='trigger'",
                null,
                null,
                null,
                null);
    }

    //获取数据库版本
    public int getDatabaseVersion() {
        Cursor cursor = mDb.rawQuery("PRAGMA user_version", null);
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int version = cursor.getInt(0);
        cursor.close();
        return version;
    }

    //获取表信息
    public Cursor getTableInfo(@NonNull String tableName) {
        return mDb.rawQuery("PRAGMA table_info('" + tableName + "')", null);
    }

    //获取表中index列表
    public Cursor getTableIndexList(@NonNull String tableName) {
        return mDb.rawQuery("PRAGMA index_list('" + tableName + "')", null);
    }

    //获取index信息
    public Cursor getIndexInfo(@NonNull String index) {
        return mDb.rawQuery("PRAGMA index_info('" + index + "')", null);
    }
}
