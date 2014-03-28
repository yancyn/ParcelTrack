package com.muje.parcel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yeang-shing.then on 3/17/14.
 */
public class TrackExpandableAdapter extends BaseExpandableListAdapter implements Filterable {
    private Context context;

    private List<Shipment> originalList;
    private List<Shipment> headerList;
    public void setGroupList(List<Shipment> shipments) {
        this.originalList = shipments;
        this.headerList = shipments;
    }

    private Map<Shipment, ArrayList<Track>> childList;
    public void setChildList(Map<Shipment, ArrayList<Track>> children) {
        this.childList = children;
    }

    public TrackExpandableAdapter(Context context, List<Shipment> headerList, Map<Shipment, ArrayList<Track>> childList) {
        this.context = context;
        this.originalList = headerList;
        this.headerList = headerList;
        this.childList = childList;
    }

    @Override
    public int getGroupCount() {
        return headerList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childList.get(headerList.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return headerList.get(i);
    }

    @Override
    public Object getChild(int i, int i2) {
        return childList.get(headerList.get(i)).get(i2);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View groupView = inflater.inflate(R.layout.list_parent, viewGroup, false);

        Shipment shipment = headerList.get(i);

        ImageView bookmark = (ImageView)groupView.findViewById(R.id.imageView2);
        int visible = (shipment.getLabel().length()>0) ? View.VISIBLE : View.INVISIBLE;
        bookmark.setVisibility(visible);

        // show history consignment number
        TextView textView = (TextView)groupView.findViewById(R.id.textView);
        textView.setText(shipment.getConsignmentNo());

        // show invalid if not found
        // if found show sent or delivered date instead
        String status = "";
        TextView statusText = (TextView)groupView.findViewById(R.id.statusText);
        if(shipment.getLabel().length()>0) {
            status += shipment.getLabel() + "\n";
        }
        if(shipment.getTracks().size() == 0) {
            status += context.getString(R.string.invalid);
        } else {
            if(shipment.getStatus() == Status.DELIVERED) {
                status += shipment.getDelivered().toLocaleString();
            } else {
                status += shipment.getSent().toLocaleString();
            }
        }
        statusText.setText(status);

        // set courier logo
        ImageView imageView = (ImageView)groupView.findViewById(R.id.imageView);
        imageView.setImageResource(shipment.getLogoId());

        // set background color based on delivery status
        if(shipment.getStatus().equals(Status.SENT)) {
            groupView.setBackgroundColor(Color.argb(255, 255, 199, 206)); //Color.RED);
        } else if(shipment.getStatus().equals(Status.WIP)) {
            groupView.setBackgroundColor(Color.argb(255, 255, 235, 156)); //.YELLOW);
        } else if(shipment.getStatus().equals(Status.DELIVERED)) {
            groupView.setBackgroundColor(Color.argb(255, 198, 239, 206));; //.GREEN
        }

        return groupView;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, viewGroup, false);

        Shipment shipment = headerList.get(i);
        int j = 0;
        for(Track track: shipment.getTracks()) {
            if(j == i2) {
                TextView textView1 = (TextView)rowView.findViewById(R.id.textView1);
                textView1.setText(track.getDate().toLocaleString());

                TextView textView2 = (TextView)rowView.findViewById(R.id.textView2);
                textView2.setText(track.getLocation());

                TextView textView3 = (TextView)rowView.findViewById(R.id.textView3);
                textView3.setText(track.getDescription());

                return rowView;
            }
            j++;
        }

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return false;
    }

    /**
     * Filtering implementation.
     * http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
     */
    private class ShipmentFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults result = new FilterResults();
            List<Shipment> filteredItems = new ArrayList<Shipment>();
            if(charSequence == null)
                filteredItems = originalList;// headerList;
            else if(charSequence.length() == 0)
                filteredItems = originalList;// headerList;
            else {
                String[] consignments = charSequence.toString().split(",");
                for(Shipment shipment: originalList) {
                    for(String consignment: consignments) {
                        consignment = consignment.trim();
                        if(shipment.getConsignmentNo().contains(consignment)) {
                            filteredItems.add(shipment);
                        }
                    }
                }
            }

            result.count = filteredItems.size();
            result.values = filteredItems;
            return result;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            headerList = (ArrayList<Shipment>)filterResults.values;
            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        ShipmentFilter filter = new ShipmentFilter();
        return filter;
    }
	
}
