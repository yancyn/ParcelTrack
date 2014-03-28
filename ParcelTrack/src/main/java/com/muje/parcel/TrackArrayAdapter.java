package com.muje.parcel;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Obsoleted. Replaced by TrackExpandableAdapter.
 */
public class TrackArrayAdapter extends ArrayAdapter<Track> {
	
	private List<Track> tracks;
	private Context context;

	public TrackArrayAdapter(Context context, int textViewResourceId, List<Track> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.tracks = objects;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//key
		convertView = vi.inflate(R.layout.list_item,null);
		if(convertView == null) return convertView;
		
		TextView textView1 = (TextView)convertView.findViewById(R.id.textView1);
		TextView textView2 = (TextView)convertView.findViewById(R.id.textView2);
		TextView textView3 = (TextView)convertView.findViewById(R.id.textView3);
		
		Track track = tracks.get(position);
		textView1.setText(track.getDate().toLocaleString());
		textView2.setText(track.getLocation());
		textView3.setText(track.getDescription());
		
		return convertView;
	}
	
}