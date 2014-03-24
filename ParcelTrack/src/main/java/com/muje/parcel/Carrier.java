package com.muje.parcel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Carrier(String consignmentNo) {
        this.name = "";
        this.consignmentNo = consignmentNo;
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
	/**
	 * Return inner html for table's cell.
	 * 
	 * @param html
	 * @return
	 */
	protected String getCellValue(String html) {
		
		String inner = "";		
		String regex = ">(.*?)<";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(html);		
		while (m.find()) {
			inner += m.group();
		}
		inner = inner.replaceAll(">","").replaceAll("<","").trim();
		
		return inner;
	}
}