/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 4:20 PM.
 */

package com.xxworkshop.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public abstract class XXDataManagerBase extends SQLiteOpenHelper {
    public XXDataManagerBase(Context context, String name, int version) {
        super(context, name, null, version);
    }

    public List<Hashtable<String, String>> query(String sql, String[] selectionArgs) {
        List<Hashtable<String, String>> results = new ArrayList<Hashtable<String, String>>();
        SQLiteDatabase dbr = this.getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, selectionArgs);
        String[] columnsNames = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            Hashtable<String, String> result = new Hashtable<String, String>();
            for (int i = 0; i <= columnsNames.length - 1; i++) {
                result.put(columnsNames[i], cursor.getString(i));
            }
            results.add(result);
        }

        cursor.close();
        dbr.close();
        return results;
    }

    public List<Hashtable<String, String>> query(String sql) {
        return this.query(sql, null);
    }

    public Hashtable<String, String> fetch(String sql, String[] selectionArgs) {
        SQLiteDatabase dbr = this.getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, selectionArgs);
        Hashtable<String, String> result = null;
        if (cursor.moveToNext()) {
            result = new Hashtable<String, String>();
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                result.put(columnName, cursor.getString(columnIndex));
            }
        }
        cursor.close();
        dbr.close();
        return result;
    }

    public Hashtable<String, String> fetch(String sql) {
        return this.fetch(sql, null);
    }

    public String scalar(String sql, String[] selectionArgs) {
        SQLiteDatabase dbr = this.getReadableDatabase();
        Cursor cursor = dbr.rawQuery(sql, selectionArgs);
        String obj = null;
        if (cursor.moveToNext()) {
            obj = cursor.getString(0);
        }
        cursor.close();
        dbr.close();
        return obj;
    }

    public String scalar(String sql) {
        return this.scalar(sql, null);
    }

    public void execute(String sql, String[] args) {
        SQLiteDatabase dbw = this.getWritableDatabase();
        dbw.execSQL(sql, args);
        dbw.close();
    }

    public void execute(String sql) {
        SQLiteDatabase dbw = this.getWritableDatabase();
        dbw.execSQL(sql);
        dbw.close();
    }

    public void executeBatch(List<String> sqls, List<String[]> args) {
        SQLiteDatabase dbw = this.getWritableDatabase();
        dbw.beginTransaction();
        for (int i = 0; i <= sqls.size() - 1; i++) {
            String sql = sqls.get(i);
            String[] arg = args.get(i);
            if (arg != null) {
                dbw.execSQL(sql, arg);
            } else {
                dbw.execSQL(sql);
            }
        }
        dbw.setTransactionSuccessful();
        dbw.endTransaction();
        dbw.close();
    }
}
