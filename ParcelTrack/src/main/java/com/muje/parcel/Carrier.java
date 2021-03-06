package com.muje.parcel;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Carrier abstract class
 * TODO: sort track collection in descending for convenient reading.
 * @author yeang-shing.then
 *
 */
public abstract class Carrier {
	public Carrier() {
        this.name = "";
        this.consignmentNo = "";
        this.tracks = new ArrayList<Track>();
    }
	protected String name;
	/**
	 * Return name of the provider after trace the consignment no.
	 * @return
	 */
	public String getProviderName() { return this.name;}
    protected String consignmentNo;
	/**
	 * Return resource id for logo image.
	 * @return
	 */
	public int getLogoId() {
		if(this.name.equals("poslaju")) {
			return R.drawable.poslaju;
		} else if(this.name.equals("citylink")) {
			return R.drawable.citylink;
		} else if(this.name.equals("gdex")) {
            return R.drawable.gdex;
        } else if(this.name.equals("fedex")) {
            return R.drawable.fedex;
        } else if(this.name.equals("skynet")) {
            return R.drawable.skynet;
        } else if(this.name.equals("ups")) {
            return R.drawable.ups;// source http://upload.wikimedia.org/wikipedia/en/9/9f/United_Parcel_Service_logo.svg
        }
		
		return -1;//null
	}
	
//	protected String origin;
//	public void setOrigin(String origin) {
//		this.origin = origin;
//	}
//	public String getOrigin() {return this.origin;}
//	
//	protected String destination;
//	public void setDestination(String destination) {
//		this.destination = destination;
//	}
//	public String getDestination() {return this.destination;}

    protected String url;

    /**
     * Return tracking number in original courier website.
     * @return
     */
    public String getUrl() {
        return this.url;
    }
	
	protected ArrayList<Track> tracks;
	/**
	 * Return track collection in descending order of time.
	 * @return
	 */
	public ArrayList<Track> getTracks() {
		
		Collections.sort(this.tracks, new TrackComparator());
		//HACK: Collections.sort(this.tracks, Collections.reverseOrder());//fail
		return this.tracks;
	}
	/**
	 * Trace by consignment no.
	 * @param consignmentNo
	 * @throws Exception
	 */
	public abstract void trace(String consignmentNo) throws Exception;
	
}