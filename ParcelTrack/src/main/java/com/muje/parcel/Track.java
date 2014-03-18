package com.muje.parcel;

public class Track {
	private java.util.Date date;
    public java.util.Date getDate() {return this.date;}

    private String location;
    public String getLocation() {return this.location;}

	private String description;
    public String getDescription() {return this.description;}

	public Track(java.util.Date date, String location, String description) {
		this.date = date;
		this.location = location;
        this.description = description;
	}
	@Override
	public String toString() {
		String output = "";
		output += getDate().toLocaleString();
		output += " ";
		output += getLocation();
		output += ": ";
		output += getDescription();

		return output;
	}
}