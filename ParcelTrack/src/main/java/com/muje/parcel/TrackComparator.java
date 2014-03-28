package com.muje.parcel;

import java.util.Comparator;

/**
 * Implement sorting for track collection.
 * @author yeang-shing.then
 *
 */
public class TrackComparator implements Comparator<Track> {

	@Override
	public int compare(Track track1, Track track2) {
		int result = track1.getDate().compareTo(track2.getDate());
		if(result == 0) result = track1.getLocation().compareTo(track2.getLocation());
		if(result == 0) result = track1.getDescription().compareTo(track2.getDescription());
		return result;
	}

}