package com.heimann.andreas.visualvault;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 03.04.2017.
 */

public class Database extends SQLiteOpenHelper {

    private static final String POINTS_TABLE = "POINTS";
    private static final String COL_ID = "ID";
    private static final String COL_X = "X";
    private static final String COL_Y = "Y";
    private static final String MESSAGE_TABLE = "MESSAGE";
    private static final String MESSAGE_ID = "ID";
    private static final String MESSAGE_TITLE = "TITLE";

    public Database(Context context) {
        super(context, "notes.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s INTEGER NOT NULL)", POINTS_TABLE, COL_ID, COL_X, COL_Y);
        db.execSQL(sql);
        Log.d(MainActivity.DEBUGTAG, "POINT TABLE CREATED");
        String sqlMessage = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s VARCHAR(255))", MESSAGE_TABLE, MESSAGE_ID, MESSAGE_TITLE);
        db.execSQL(sqlMessage);
        Log.d(MainActivity.DEBUGTAG, "MESSAGE TABLE CREATED");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void storePoints(List<Point> points) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(POINTS_TABLE, null, null);

        int i = 0;

        for (Point point : points) {
            ContentValues values = new ContentValues();
            values.put(COL_ID, i);
            values.put(COL_X, point.x);
            values.put(COL_Y, point.y);
            db.insert(POINTS_TABLE, null, values);
            i++;
        }

        db.close();
    }

    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sql = String.format("SELECT %s, %s FROM %s ORDER BY %s", COL_X, COL_Y, POINTS_TABLE, COL_ID);
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            int x = cursor.getInt(0);
            int y = cursor.getInt(1);

            points.add(new Point(x, y));
        }
        db.close();

        return points;
    }

    public void deletePoints() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(POINTS_TABLE, null, null);
    }

    public void storeMessage(Message message) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MESSAGE_TITLE, message.getTitle());
        db.insert(MESSAGE_TABLE, null, values);

        db.close();
    }

    public void deleteAllMessages() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(MESSAGE_TABLE, null, null);
    }

    public void deleteMessage(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String sqlDelete = String.format("DELETE FROM %s WHERE %s='%s'", MESSAGE_TABLE, MESSAGE_ID, String.valueOf(id));
        db.execSQL(sqlDelete);
        db.close();
    }

    public void updateMessage(int id, String title) {
        SQLiteDatabase db = getWritableDatabase();
        String sqlUpdate = String.format("UPDATE %s SET %s='%s' WHERE %s='%s'", MESSAGE_TABLE, MESSAGE_TITLE, title, MESSAGE_ID, String.valueOf(id));
        db.execSQL(sqlUpdate);
        db.close();
    }

    public Message getMessage(int id) {
        Message message = new Message("Empty");
        SQLiteDatabase db = getReadableDatabase();

        String sql = String.format("SELECT %s, %s FROM %s WHERE %s='%s'", MESSAGE_ID, MESSAGE_TITLE, MESSAGE_TABLE, MESSAGE_ID, String.valueOf(id));
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        int messageId = cursor.getInt(0);
        String messageTitle = cursor.getString(1);
        message = new Message(messageTitle, messageId);

        db.close();
        return message;
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sql = String.format("SELECT %s, %s FROM %s", MESSAGE_ID, MESSAGE_TITLE, MESSAGE_TABLE);
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            int messageId = cursor.getInt(0);
            String messageTitle = cursor.getString(1);
            messages.add(new Message(messageTitle, messageId));
        }
        db.close();

        return messages;
    }
}
