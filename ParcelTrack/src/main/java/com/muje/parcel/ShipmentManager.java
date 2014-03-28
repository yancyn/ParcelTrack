package com.muje.parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;

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
    private static final String SQL_SELECT_ALL_SHIPMENTS = "SELECT * FROM Shipments";
    private static final String SQL_SELECT_SHIPMENT_ID = "SELECT Id FROM Shipments WHERE Number = ?";
    private static final String SQL_SELECT_TRACKS = "SELECT Tracks.*, Shipments.Number FROM Tracks JOIN Shipments ON Tracks.ShipmentId=Shipments.Id WHERE Shipments.Number = ?";
    private static final String SQL_DELETE_ALL_TRACKS = "DELETE FROM Tracks";
    private static final String SQL_DELETE_ALL_SHIPMENTS = "DELETE FROM Shipments";

    private ArrayList<Shipment> shipments;
    public ArrayList<Shipment> getShipments() { return this.shipments; }

	public ShipmentManager(Context context) {
        this.context = context;
        this.shipments = new ArrayList<Shipment>();
        Initialize();
    }
    private void Initialize() {
        //Log.d("DEBUG", "Initializing manager");
        dbHelper = new DbHelper(context);
        database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(SQL_SELECT_ALL_SHIPMENTS, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String consignmentNo = cursor.getString(1);
            Shipment shipment = new Shipment(consignmentNo);
            String label = cursor.getString(2);
            shipment.setLabel(label);
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
                    //Log.d("DEBUG", "Date: " + dateString);
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
        track(consignmentNo, "");
    }
    public void track(String consignmentNo, String label) {

        //Log.d("DEBUG", "Not exist in database");
        Shipment shipment = new Shipment(consignmentNo);

        try {
            shipment.trace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        shipment.setLabel(label);

        // insert shipment header into database
        ContentValues values = new ContentValues();
        values.put("Number", shipment.getConsignmentNo());
        values.put("Label", shipment.getLabel());
        long id = database.insert(TABLE_SHIPMENTS, null, values);

        // Dump tracks details into database
        for(Track track: shipment.getTracks()) {
            values = new ContentValues();
            values.put("ShipmentId", id);
            String date = dateFormat.format(track.getDate());
            //Log.d("DEBUG", "Date: " + date);
            values.put("Date", date);
            values.put("Location", track.getLocation());
            values.put("Description", track.getDescription());
            database.insert(TABLE_TRACKS, null, values);
        }
        this.shipments.add(0,shipment);
    }

    /**
     * Track with carrier provided.
     * @param carrier
     * @param consignmentNo
     */
    public void track(Carrier carrier, String consignmentNo) {

        //Log.d("DEBUG", "Not exist in database");
        Shipment shipment = new Shipment(carrier);

        try {
            shipment.trace(consignmentNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //shipment.setLabel(label);

        // insert shipment header into database
        ContentValues values = new ContentValues();
        values.put("Number", shipment.getConsignmentNo());
        values.put("Label", shipment.getLabel());
        long id = database.insert(TABLE_SHIPMENTS, null, values);

        // Dump tracks details into database
        for(Track track: shipment.getTracks()) {
            values = new ContentValues();
            values.put("ShipmentId", id);
            String date = dateFormat.format(track.getDate());
            //Log.d("DEBUG", "Date: " + date);
            values.put("Date", date);
            values.put("Location", track.getLocation());
            values.put("Description", track.getDescription());
            database.insert(TABLE_TRACKS, null, values);
        }
        this.shipments.add(0,shipment);
    }

    /**
     * Update specific shipment in collection and database when add annotation.
     * @param shipment
     */
    public void update(Shipment shipment) {
        for(Shipment s: this.shipments) {
            if(s.getConsignmentNo().equals(shipment.getConsignmentNo())) {
                s = shipment;
                break;
            }
        }

        ContentValues values = new ContentValues();
        values.put("Label", shipment.getLabel());
        database.update(TABLE_SHIPMENTS, values, "Number = ?", new String[] {shipment.getConsignmentNo()});
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
        //Log.d("DEBUG", consignmentNo + " deleted");
    }
    public void deleteAll() {
        database.execSQL(SQL_DELETE_ALL_TRACKS);
        database.execSQL(SQL_DELETE_ALL_SHIPMENTS);
        this.shipments.clear();
    }

    /**
     * Refresh all pending consignment no.
     */
    public void refresh(int i, String consignmentNo) {
        String label = shipments.get(i).getLabel();
        delete(i, consignmentNo);
        track(consignmentNo, label);
    }
    public void refreshAll() {
        int last = this.shipments.size() - 1;
        for(int i=this.shipments.size()-1;i>=0;i--) {
            Shipment shipment = this.shipments.get(last);
            String consignmentNo = shipment.getConsignmentNo();
            if(shipment.getStatus() == Status.DELIVERED) {
                last --;
            } else {
                refresh(last, consignmentNo);
            }
        }
    }

    /**
     * Update status of pending items and only if had delivered.
     * @return
     */
    public ArrayList<Shipment> getUpdates() {

        ArrayList<Shipment> updates = new ArrayList<Shipment>();
        int last = this.shipments.size() - 1;
        for(int i=this.shipments.size()-1;i>=0;i--) {
            Shipment shipment = this.shipments.get(last);
            String consignmentNo = shipment.getConsignmentNo();
            if(shipment.getStatus() == Status.DELIVERED) {
                last --;
            } else {
                refresh(last, consignmentNo);
                Shipment updated = shipments.get(0);
                if(updated.getStatus() == Status.DELIVERED) {
                    updates.add(updated);
                }
            }
        }

        return updates;
    }

}