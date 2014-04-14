package com.muje.parcel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
    private static final String SQL_EXPORT_TRACKS = "SELECT Shipments.Number, Shipments.Label, Tracks.Date, Tracks.Location, Tracks.Description FROM Tracks JOIN Shipments ON Tracks.ShipmentId=Shipments.Id";
    private static final String DELIMITER = ",";
    private static final String DOUBLE_QUOTES = "\"";

    private Shipment shipment;
    private ArrayList<Shipment> shipments;
    public ArrayList<Shipment> getShipments() { return this.shipments; }

	public ShipmentManager(Context context) {
        this.context = context;
        this.shipments = new ArrayList<Shipment>();
        initialize();
    }
    // todo: failed to retrieve carrier provider for Skynet and FedEx.
    private void initialize() {
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
    public String getProviderName() {
        return this.shipment.getProviderName();
    }
    public Carrier getCarrier(String consignmentNo) throws Exception  {

        Carrier carrier = null;
        //determine which Carrier to be use
        //check is poslaju's parcel ie. EM046999084MY
        if(consignmentNo.matches("[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}")) {
            carrier = new Poslaju(consignmentNo);
        }
        //check is citylink's parcel ie. 060301203057634
        else if(consignmentNo.matches("[0-9]{15}")) {
            carrier = new Citylink(consignmentNo);
        }
        //check is gdex's parcel ie. 4340560475
        else if(consignmentNo.matches("[0-9]{10}")) {
            carrier = new Gdex(consignmentNo);
        }
        //TODO: Skynet no pattern conflict with FedEx
        // check is Skynet's parcel ie. 238074386631
        else if(consignmentNo.matches("[0-9]{12}")) {
            carrier = new Skynet(consignmentNo);
        }
        //check is FedEx's parcel ie. 797337230186
        else if(consignmentNo.matches("[0-9]{12}")) {
            carrier = new Fedex(consignmentNo);
        }
        // check if UPS no. 1Z71EY050499423570
        else if(consignmentNo.matches("[1][Z][A-Z0-9]{6}[0-9]{2}[0-9]{8}")) {
            carrier = new Ups(consignmentNo);
        }

        return carrier;
    }

    public void track(String consignmentNo) {
        track(consignmentNo, "");
    }
    public void track(String consignmentNo, String label) {

        shipment = new Shipment(consignmentNo);

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

        Shipment shipment = new Shipment(carrier);

        try {
            shipment.trace(consignmentNo);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    /**
     * Export to local csv file.
     * @param path
     */
    public void export(String path) {

        String content = "";

        // add csv header
        content += addDoubleQuote("Number");
        content += DELIMITER + addDoubleQuote("Label");
        content += DELIMITER + addDoubleQuote("Date");
        content += DELIMITER + addDoubleQuote("Location");
        content += DELIMITER + addDoubleQuote("Description");
        content += "\n";

        Cursor cursor = database.rawQuery(SQL_EXPORT_TRACKS, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String consignmentNo = cursor.getString(0);
            String label = cursor.getString(1);
            String date = cursor.getString(2);
            String location = cursor.getString(3);
            String desc = cursor.getString(4);

            content += addDoubleQuote(consignmentNo);
            content += DELIMITER + addDoubleQuote(label);
            content += DELIMITER + addDoubleQuote(date);//dateFormat.format(toDate(date)));
            content += DELIMITER + addDoubleQuote(location);
            content += DELIMITER + addDoubleQuote(desc);
            content += "\n";

            cursor.moveToNext();
        }
        cursor.close();

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(path);
            outputStream.write(content.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();

            try {
                if(outputStream != null) {
                    outputStream.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
    private String addDoubleQuote(String value) {
        return DOUBLE_QUOTES + value + DOUBLE_QUOTES;
    }

    private Date toDate(String format) {
        Date date = null;
        try {
            SimpleDateFormat dt = new SimpleDateFormat("M/dd/yyyy hh:mm:ss a");
            date = dateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
}