package com.su.sample.component;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TestSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = TestSQLiteHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "test.db";
    private static final int DATABASE_VERSION = 8;

    TestSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableSql = "CREATE TABLE IF NOT EXISTS student (\n"
                + "_id INTEGER primary key AUTOINCREMENT,\n"
                + "name text NOT NULL,\n"
                + "age integer NOT NULL,\n"
                + "sex integer NOT NULL);";
        String indexSql = "CREATE UNIQUE INDEX response_I" +
                " ON response(url, method, contentType, requestHeaders, requestBody, auto);";
        db.execSQL(tableSql);
        db.execSQL(indexSql);

        String viewSql = "CREATE VIEW COMPANY_VIEW AS\n" +
                "SELECT _id, url, auto\n" +
                "FROM response;";
        db.execSQL(viewSql);

        String table2Sql = "CREATE TABLE IF NOT EXISTS response2 (\n"
                + "_id INTEGER primary key AUTOINCREMENT,\n"
                + "url text NOT NULL)\n";
        db.execSQL(table2Sql);
        db.execSQL("INSERT INTO response2 (url) VALUES ('b')");

        String triggerSql = "CREATE TRIGGER raw_contacts_marked_deleted" +
                "    AFTER UPDATE ON response " +
                "BEGIN" +
                " UPDATE response2 SET url='a';" +
                " END";
        db.execSQL(triggerSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
