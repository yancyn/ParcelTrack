package com.muje.parcel;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {

    private static final int RESULT_SETTINGS = 100;
    private static final int NOTIFICATION_ID = 1001;
    private String refreshNo = "";
    private int selectedIndex = -1;

    private TrackExpandableAdapter adapter = null;

    private EditText editText1;
    private ProgressDialog dialog = null;
    private ShipmentManager manager = null;
    private Runnable runnables;
    private AdView adView;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // dismiss notification if any after launch the app
        NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancelAll();

        editText1 = (EditText)findViewById(R.id.editText1);
        editText1.setOnKeyListener(editText1OnKey);

        // add clear all search text
        ImageButton button2 = (ImageButton)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText1.setText("");
            }
        });

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

        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.ad_unit_id));

        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

        // create timer for launch notification
        setNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onPause() {
        if(adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

//    @Override
//    protected void onStop() {
//        timer = new Timer();
//        timer.schedule(new TimerTask(){
//
//            @Override
//            public void run() {
//                notification();
//            }
//        }, 5*60*1000, 5*60*1000);
//        super.onStop();
//    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        switch(item.getItemId()) {
            case R.id.action_update:
                if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    selectedIndex = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                    ArrayList<Shipment> shipments = manager.getShipments();
                    refreshNo = shipments.get(selectedIndex).getConsignmentNo();

                    runnables = new Runnable() {
                        @Override
                        public void run() {
                            manager.refresh(selectedIndex, refreshNo);
                            runOnUiThread(returnRes);
                        }
                    };

                    Thread thread = new Thread(null, runnables, "Processing");
                    thread.start();

                    dialog = ProgressDialog.show(this, "Please wait", "Retrieving data...", true);
                }
                return true;
            case R.id.action_delete:
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
            case R.id.action_update_all:
                // Refresh all pending parcel status
                Log.d("DEBUG", "Refresh all pending parcel");
                runnables = new Runnable() {
                    @Override
                    public void run() {
                        manager.refreshAll();
                        runOnUiThread(returnRes);
                    }
                };

                Thread thread = new Thread(null, runnables, "Processing");
                thread.start();

                dialog = ProgressDialog.show(this, "Please wait", "Retrieving data...", true);
                return true;
            case R.id.action_clear_all:
                // Clear all history
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.are_you_sure))
                        .setPositiveButton(getString(R.string.yes), dialogListener)
                        .setNegativeButton(getString(R.string.no), dialogListener)
                        .show();
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch(i) {
                case DialogInterface.BUTTON_POSITIVE:
                    Log.d("DEBUG", "Remove all history");
                    manager.deleteAll();
                    rebind();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    return;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RESULT_SETTINGS:
                setNotification();
                break;
        }
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

        dialog = ProgressDialog.show(this, "Please wait", "Retrieving data...", true);
    }
    private void trace(String consignmentNo) {
        if(!manager.isExist(consignmentNo)) {
            manager.track(consignmentNo);
        }
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
            dialog.dismiss();
            rebind();
        }

    };

    private void setNotification() {

        if(timer != null) timer.cancel();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean enabled = sharedPreferences.getBoolean("prefNotificationEnabled", false);
        Log.d("DEBUG", "Notification: " + enabled);
        if(!enabled) return;

        String hourStr = sharedPreferences.getString("prefNotificationHour", "0");
        int hour = Integer.parseInt(hourStr);
        if(hour > 0) {
            Log.d("DEBUG", "Notify every " + hour + " hours");
            timer = new Timer();
            timer.schedule(new TimerTask(){
                @Override
                public void run() {
                    notification();
                }
            }, hour*60*60*1000, hour*60*60*1000);
        }
    }

    /**
     * Show up in notification bar
     * http://developer.android.com/guide/topics/ui/notifiers/notifications.html
     */
    private void notification() {

        ArrayList<Shipment> updates = manager.getUpdates();
        if(updates.size() == 0) return;

        String pendingNo = "";
        for(Shipment shipment: updates) {
            if(pendingNo.length()>0) pendingNo += getString(R.string.comma);
            pendingNo += shipment.getConsignmentNo();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.parcel)
                .setContentTitle(updates.size() + getString(R.string.item_delivered))
                .setContentText(pendingNo);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

}
