package com.muje.parcel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    public static final int RESULT_SETTINGS = 100;
    private ProgressDialog dialog = null;
    private ShipmentManager manager = null;
    private TrackExpandableAdapter adapter = null;
    private Runnable runnables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText1 = (EditText)findViewById(R.id.editText1);
        editText1.setOnKeyListener(editText1OnKey);

        ImageButton button1 = (ImageButton) findViewById(R.id.button1);
        button1.setOnClickListener(button1OnClick);

        //only need to declare once
        manager = new ShipmentManager(this);
        Map<Shipment, ArrayList<Track>> children = new HashMap<Shipment, ArrayList<Track>>();
        for(Shipment shipment: manager.getShipments()) {
            children.put(shipment, shipment.getTracks());
        }

        adapter = new TrackExpandableAdapter(this, manager.getShipments(), children);
        ExpandableListView listView = (ExpandableListView)findViewById(R.id.expandableListView);
        registerForContextMenu(listView);// do not delegate longClick event anymore otherwise contextmenu will fail
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete:
                ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
                int type = ExpandableListView.getPackedPositionType(info.packedPosition);
                if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {

                    int position = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                    ArrayList<Shipment> shipments = manager.getShipments();
                    String consignmentNo = shipments.get(position).getConsignmentNo();

                    manager.delete(position, consignmentNo);
                    rebind();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_update:
                // TODO: Update pending parcel
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected View.OnKeyListener editText1OnKey = new View.OnKeyListener() {

        @Override
        public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
            if(arg2.getAction() == KeyEvent.ACTION_DOWN) {
                switch(arg1) {
                    case KeyEvent.KEYCODE_ENTER:
                        begin();
                        return true;
                    default:
                        return false;
                }
            }

            return false;
        }
    };
    protected View.OnClickListener button1OnClick = new View.OnClickListener() {
        public void onClick(View view) {
            begin();
        }
    };

    private void begin() {
        runnables = new Runnable() {
            @Override
            public void run() {
                EditText editText1 = (EditText) findViewById(R.id.editText1);
                trace(editText1.getText().toString());
            }
        };

        Thread thread = new Thread(null, runnables, "Processing");
        thread.start();

        TextView textView4 = (TextView)findViewById(R.id.textView4);
        textView4.setText("");

        dialog = ProgressDialog.show(this, "Please wait", "Retrieving data...", true);
    }
    private void trace(String consignmentNo) {
        manager.track(consignmentNo);
        runOnUiThread(returnRes);
    }
    private void rebind() {
        Map<Shipment, ArrayList<Track>> children = new HashMap<Shipment, ArrayList<Track>>();
        for(Shipment shipment: manager.getShipments()) {
            children.put(shipment, shipment.getTracks());
        }
        adapter.setGroupList(manager.getShipments());
        adapter.setChildList(children);
        adapter.notifyDataSetChanged();
    }
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {

            // TODO: Handle no result found
            dialog.dismiss();
            rebind();
        }

    };

}
