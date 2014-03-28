package com.muje.parcel;

//import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Facade pattern for Carrier class.
 */
public class Shipment {

    private final String[] DELIVERED_KEYWORDS = new String[]{ "success", "delivered"};

    private Carrier carrier;
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
        return this.carrier.getProviderName();
    }
    /**
     * Return provider logo image resource id.
     * @return
     */
    public int getLogoId() {
        if(carrier == null) {
            return 0;
        } else {
            return this.carrier.getLogoId();
        }
    }

    /**
     * Return website url.
     * @return
     */
    public String getUrl() {
        if(this.carrier == null) {
            return "";
        } else {
            return this.carrier.getUrl();
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
    public Shipment(Carrier carrier) {
        this.status = Status.INVALID;
        this.label = "";
        this.tracks = new ArrayList<Track>();
        this.carrier = carrier;
    }
    protected void locateCourier() throws Exception  {

        //determine which Carrier to be use
        //check is poslaju's parcel ie. EM046999084MY
        if(consignmentNo.matches("[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}")) {
            carrier = new Poslaju(this.consignmentNo);
        }
        //check is citylink's parcel ie. 060301203057634
        else if(consignmentNo.matches("[0-9]{15}")) {
            carrier = new Citylink(this.consignmentNo);
        }
        //check is gdex's parcel ie. 4340560475
        else if(consignmentNo.matches("[0-9]{10}")) {
            carrier = new Gdex(this.consignmentNo);
        }
        //check is FedEx's parcel ie. 797337230186
        else if(consignmentNo.matches("[0-9]{12}")) {
            carrier = new Fedex(this.consignmentNo);
        }
    }

    /**
     * Track on chosen carrier only.
     * @param consignmentNo
     */
    public void trace(String consignmentNo) {
        this.consignmentNo = consignmentNo;
        trace();
    }
    /**
     * Trace by consignment no.
     *
     * @throws Exception
     */
    public void trace() {
        //Log.d("DEBUG", "Consignment No: " + consignmentNo);

        try {
            this.carrier.trace(consignmentNo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(carrier == null) return;

        this.tracks = this.carrier.getTracks();
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
