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
            //determine which Courier to be use
            //check is poslaju's parcel ie. EM046999084MY
            String regex = "[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(consignmentNo);
            if(matcher.find()) {
                return R.drawable.poslaju;
            }

            //check is citylink's parcel ie. 060301203057634
            regex = "[0-9]{15}";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(consignmentNo);
            if(matcher.find()) {
                return R.drawable.citylink;
            }

            //check is gdex's parcel ie. 4340560475
            regex = "[0-9]{10}";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(consignmentNo);
            if(matcher.find()) {
                return R.drawable.gdex;
            }

            return 0;
        } else {
            return this.courier.getLogoId();
        }
    }

    public Shipment(String consignmentNo) {
        this.consignmentNo = consignmentNo;
        this.status = Status.INVALID;
        this.label = "";
        this.tracks = new ArrayList<Track>();
    }
    private void locateCourier() throws Exception  {
        //determine which Courier to be use
        //check is poslaju's parcel ie. EM046999084MY
        String regex = "[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(consignmentNo);
        if(matcher.find()) {
            courier = new Poslaju();
            courier.trace(consignmentNo);
            return;
        }

        //check is citylink's parcel ie. 060301203057634
        regex = "[0-9]{15}";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(consignmentNo);
        if(matcher.find()) {
            courier = new Citylink();
            courier.trace(consignmentNo);
            return;
        }

        //check is gdex's parcel ie. 4340560475
        regex = "[0-9]{10}";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(consignmentNo);
        if(matcher.find()) {
            courier = new Gdex();
            courier.trace(consignmentNo);
            return;
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
            locateCourier();
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

                if(track.getDescription().toLowerCase().contains("success")) {
                    this.status = Status.DELIVERED;
                    this.delivered = track.getDate();
                } else if(track.getDescription().toLowerCase().contains("delivered")) {
                    this.status = Status.DELIVERED;
                    this.delivered = track.getDate();
                }
            }
        }
    }
}
