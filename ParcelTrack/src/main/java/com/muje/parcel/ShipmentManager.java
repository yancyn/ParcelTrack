package com.muje.parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Manager class for shipments.
 */
public class ShipmentManager {

    private Context context;
    private DbHelper dbHelper;
    private SQLiteDatabase database;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TABLE_SHIPMENTS = "Shipments";
    private static final String TABLE_TRACKS = "Tracks";
    private static final String SQL_SELECT_ALL_SHIPMENTS = "SELECT * FROM Shipments;";
    private static final String SQL_SELECT_SHIPMENT_ID = "SELECT Id FROM Shipments WHERE Number = ?";
    private static final String SQL_SELECT_TRACKS = "SELECT Tracks.*, Shipments.Number FROM Tracks JOIN Shipments ON Tracks.ShipmentId=Shipments.Id WHERE Shipments.Number = ?;";

    private ArrayList<Shipment> shipments;
    public ArrayList<Shipment> getShipments() { return this.shipments; }

	public ShipmentManager(Context context) {
        this.context = context;
        this.shipments = new ArrayList<Shipment>();
        Initialize();
    }
    private void Initialize() {
        Log.d("DEBUG", "Initializing manager");
        dbHelper = new DbHelper(context);
        database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(SQL_SELECT_ALL_SHIPMENTS, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String consignmentNo = cursor.getString(1);
            Status status = Status.INVALID;
            int i = cursor.getInt(2);
            switch(i) {
                case 1:
                    status = Status.SENT;
                    break;
                case 2:
                    status = Status.WIP;
                    break;
                case 3:
                    status = Status.DELIVERED;
                    break;
            }

            Shipment shipment = new Shipment(consignmentNo, status);
            shipments.add(0,shipment);

            cursor.moveToNext();
        }
        cursor.close();

        for(Shipment shipment: shipments) {
            ArrayList<Track> tracks = new ArrayList<Track>();
            cursor = database.rawQuery(SQL_SELECT_TRACKS, new String[] {shipment.getConsignmentNo()});
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                Date date = null;
                try {
                    String dateString = cursor.getString(2);
                    Log.d("DEBUG", "Date: " + dateString);
                    SimpleDateFormat dt = new SimpleDateFormat("M/dd/yyyy hh:mm:ss a");
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String location = cursor.getString(3);
                String description = cursor.getString(4);
                tracks.add(new Track(date, location, description));

                cursor.moveToNext();
            }
            cursor.close();
            shipment.setTracks(tracks);
        }
    }
    public boolean isExist(String consignmentNo) {
        for(Shipment shipment: this.shipments) {
            if(shipment.getConsignmentNo().equals(consignmentNo)) {
                return true;
            }
        }
        return false;
    }
    public void track(String consignmentNo) {

        Log.d("DEBUG", "Not exist in database");
        Shipment shipment = new Shipment(consignmentNo);
        try {
            shipment.trace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = 0;
        if(shipment.getStatus().equals(Status.SENT)) {
            i = 1;
        } else if(shipment.getStatus().equals(Status.WIP)) {
            i = 2;
        } else if(shipment.getStatus().equals(Status.DELIVERED)) {
            i = 3;
        }

        // insert shipment header into database
        ContentValues values = new ContentValues();
        values.put("Number", shipment.getConsignmentNo());
        values.put("Status", i);
        long id = database.insert(TABLE_SHIPMENTS, null, values);

        // Dump tracks details into database
        for(Track track: shipment.getTracks()) {
            values = new ContentValues();
            values.put("ShipmentId", id);
            String date = dateFormat.format(track.getDate());
            Log.d("DEBUG", "Date: " + date);
            values.put("Date", date);
            values.put("Location", track.getLocation());
            values.put("Description", track.getDescription());
            database.insert(TABLE_TRACKS, null, values);
        }
        this.shipments.add(0,shipment);
    }
    public void delete(int i, String consignmentNo) {

        this.shipments.remove(i);

        int id = 0;
        Cursor cursor = database.rawQuery(SQL_SELECT_SHIPMENT_ID, new String[] {consignmentNo});
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            id = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();

        database.delete(TABLE_TRACKS, "ShipmentId = " + id, null);
        database.delete(TABLE_SHIPMENTS, "Id = " + id, null);
        Log.d("DEBUG", consignmentNo + " deleted");
    }

    /**
     * Refresh all pending consignment no.
     */
    public void refresh(int i, String consignmentNo) {
        delete(i, consignmentNo);
        track(consignmentNo);
    }

}