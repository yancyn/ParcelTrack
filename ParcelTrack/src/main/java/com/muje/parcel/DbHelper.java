package com.muje.parcel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by yeang-shing.then on 3/17/14.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tracks.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE_SHIPMENTS = "CREATE TABLE IF NOT EXISTS Shipments("
            + "Id integer NOT NULL PRIMARY KEY autoincrement,"
            + "Number text NOT NULL,"
            + "Status integer DEFAULT 0)";
    private static final String CREATE_TABLE_TRACKS = "CREATE TABLE IF NOT EXISTS Tracks("
            + "Id integer NOT NULL PRIMARY KEY autoincrement,"
            + "ShipmentId integer NOT NULL,"
            + "Date datetime,"
            + "Location text,"
            + "Description text,"
            + "FOREIGN KEY(ShipmentId) REFERENCES Shipments(Id))";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DEBUG", "Creating table...");
        sqLiteDatabase.execSQL(CREATE_TABLE_SHIPMENTS);
        sqLiteDatabase.execSQL(CREATE_TABLE_TRACKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        Log.w(DbHelper.class.getName(), "Upgrading database from version " + i + " to " + i2 + " which will destroy all old data.");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Tracks");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Shipments");
        onCreate(sqLiteDatabase);
    }
}
