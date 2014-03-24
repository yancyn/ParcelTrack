package com.muje.parcel;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facade pattern for Courier class.
 */
public class Shipment {

    private final String[] DELIVERED_KEYWORDS = new String[]{ "success", "delivered"};

    private Courier courier;
    private String consignmentNo;
    public String getConsignmentNo() { return this.consignmentNo; }
    private Status status;
    public Status getStatus() { return this.status; }
    private String label;
    public String getLabel() { return this.label;}
    public void setLabel(String label) { this.label = label;}
    private Date sent;
    public Date getSent() { return this.sent; }
    private Date delivered;
    public Date getDelivered() { return this.delivered; }
    private ArrayList<Track> tracks;
    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
        setProperties();}
    /**
     * Return tracks collection.
     * @return
     */
    public ArrayList<Track> getTracks() { return this.tracks; }
    /**
     * Return provider name of the shipment.
     * @return
     */
    public String getProviderName() {
        return this.courier.getProviderName();
    }
    /**
     * Return provider logo image resource id.
     * @return
     */
    public int getLogoId() {
        if(courier == null) {
            return 0;
        } else {
            return this.courier.getLogoId();
        }
    }

    /**
     * Return website url.
     * @return
     */
    public String getUrl() {
        if(this.courier == null) {
            return "";
        } else {
            return this.courier.getUrl();
        }
    }

    public Shipment(String consignmentNo) {

        this.status = Status.INVALID;
        this.label = "";
        this.tracks = new ArrayList<Track>();

        this.consignmentNo = consignmentNo;
        try {
            locateCourier();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void locateCourier() throws Exception  {

        //determine which Courier to be use
        //check is poslaju's parcel ie. EM046999084MY
        if(consignmentNo.matches("[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}")) {
            courier = new Poslaju(this.consignmentNo);
        }
        //check is citylink's parcel ie. 060301203057634
        else if(consignmentNo.matches("[0-9]{15}")) {
            courier = new Citylink(this.consignmentNo);
        }
        //check is gdex's parcel ie. 4340560475
        else if(consignmentNo.matches("[0-9]{10}")) {
            courier = new Gdex(this.consignmentNo);
        }
        //check is FedEx's parcel ie. 797337230186
        else if(consignmentNo.matches("[0-9]{12}")) {
            courier = new Fedex(this.consignmentNo);
        }
    }
    /**
     * Trace by consignment no.
     * Currently supported Malaysia Poslaju parcel system.
     *
     * @throws Exception
     */
    public void trace() {
        Log.d("DEBUG", "Consignment No: " + consignmentNo);

        try {
            this.courier.trace(consignmentNo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(courier == null) return;

        this.tracks = this.courier.getTracks();
        setProperties();
    }

    /**
     * Set status, send and delivered date based on tracks collection.
     */
    private void setProperties() {
        if(this.tracks == null) return;

        // determine status, sent and delivered date
        if(tracks.size() == 1) {
            this.status = Status.SENT;
            sent = tracks.get(0).getDate();
        } else if(tracks.size() > 1) {
            this.status = Status.WIP;
            for(Track track: tracks) {
                if(sent == null) sent = track.getDate();
                if(track.getDate().compareTo(sent) < 0) sent = track.getDate();

                for(String keyword: DELIVERED_KEYWORDS) {
                    if(track.getDescription().toLowerCase().contains(keyword)) {
                        this.status = Status.DELIVERED;
                        this.delivered = track.getDate();
                    }
                }
            }
        }
    }
}
