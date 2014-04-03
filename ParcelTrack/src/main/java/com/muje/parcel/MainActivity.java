package com.muje.parcel;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {

    private static final int RESULT_SETTINGS = 100;
    private static final int NOTIFICATION_ID = 1001;
    private String refreshNo = "";
    private String searchText = "";
    private int selectedIndex = -1;

    private TrackExpandableAdapter adapter = null;

    private EditText editText1;
    private ImageButton crossButton = null;
    private ImageButton searchButton = null;
    private ProgressDialog dialog = null;
    private ShipmentManager manager = null;
    private Runnable runnables;
    private AdView adView = null;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // dismiss notification after launch the app if any
        NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancelAll();

        // add clear all search text
        crossButton = (ImageButton)findViewById(R.id.button2);
        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText1.setText("");
            }
        });
        crossButton.setVisibility(View.GONE);

        editText1 = (EditText)findViewById(R.id.editText1);
        editText1.setOnKeyListener(editText1OnKey);
        editText1.addTextChangedListener(searchTextWatcher);

        searchButton = (ImageButton) findViewById(R.id.button1);
        searchButton.setOnClickListener(button1OnClick);
        searchButton.setEnabled(false); // Not allow to search if nothing provide in search text

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
        if(getPackageName().equals(getString(R.string.free_package_name))) {
            adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.ad_unit_id));

            LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
            layout.addView(adView);

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            adView.loadAd(adRequest);
        }

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
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {

            selectedIndex = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            ArrayList<Shipment> shipments = manager.getShipments();
            Shipment shipment = shipments.get(selectedIndex);

            switch(item.getItemId()) {
                //open link in browser
                case R.id.action_open_link:
                    //Log.d("DEBUG", "Open link in browser");
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(shipment.getUrl()));
                    startActivity(browser);
                    return true;

                // todo: Share link to other app
                case R.id.action_share:
                    return true;

                // Add annotation
                case R.id.action_annotate:
                    final Shipment shipment1 = shipments.get(selectedIndex);

                    AlertDialog.Builder prompt = new AlertDialog.Builder(this);
                    prompt.setTitle(getString(R.string.annotation_title));
                    prompt.setMessage(getString(R.string.annotation_summary));
                    final EditText input = new EditText(this);
                    prompt.setView(input);
                    prompt.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String value = input.getText().toString();
                            shipment1.setLabel(value);
                            //Log.d("DEBUG", "Add " + shipment1.getLabel() + " to " + shipment1.getConsignmentNo());
                            manager.update(shipment1);
                            rebind();
                        }
                    });
                    prompt.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    prompt.show();

                    return true;

                // save time to only refresh incomplete parcel
                case R.id.action_update:
                    if(shipments.get(selectedIndex).getStatus() != Status.DELIVERED) {
                        refreshNo = shipments.get(selectedIndex).getConsignmentNo();

                        runnables = new Runnable() {
                            @Override
                            public void run() {
                                manager.refresh(selectedIndex, refreshNo);
                                runOnUiThread(returnRes);
                            }
                        };

                        Thread thread = new Thread(null, runnables, getString(R.string.processing));
                        thread.start();

                        dialog = ProgressDialog.show(this, getString(R.string.wait_title), getString(R.string.processing_content), true);
                    }
                    return true;

                case R.id.action_delete:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.are_you_sure))
                            .setPositiveButton(getString(R.string.yes), deleteListener)
                            .setNegativeButton(getString(R.string.no), deleteListener)
                            .show();
                    return true;

                default:
                    return super.onContextItemSelected(item);
            }
        }

        return true;
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
            // export history into excel
            case R.id.action_export:
                if(getPackageName().equals(getString(R.string.free_package_name))) {
                    //Toast.makeText(this, getString(R.string.upgrade_prompt), Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder prompt = new AlertDialog.Builder(this);
                    prompt.setMessage(getString(R.string.upgrade_prompt))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // do nothing
                                }
                            })
                            .show();
                    return true;
                }

                exportAndSend();
                break;
            case R.id.action_update_all:
                // Refresh all pending parcel status
                //Log.d("DEBUG", "Refresh all pending parcel");
                runnables = new Runnable() {
                    @Override
                    public void run() {
                        manager.refreshAll();
                        runOnUiThread(returnRes);
                    }
                };

                Thread thread = new Thread(null, runnables, getString(R.string.processing));
                thread.start();

                dialog = ProgressDialog.show(this, getString(R.string.wait_title), getString(R.string.processing_content), true);
                return true;
            case R.id.action_clear_all:
                // Clear all history
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.are_you_sure))
                        .setPositiveButton(getString(R.string.yes), deleteAllListener)
                        .setNegativeButton(getString(R.string.no), deleteAllListener)
                        .show();
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch(i) {
                case DialogInterface.BUTTON_POSITIVE:
                    String consignmentNo = manager.getShipments().get(selectedIndex).getConsignmentNo();
                    manager.delete(selectedIndex, consignmentNo);
                    rebind();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener deleteAllListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch(i) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Log.d("DEBUG", "Remove all history");
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
    protected TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //Log.d("DEBUG", "Filtering: " + charSequence);
            if(charSequence.length() > 0) {
                crossButton.setVisibility(View.VISIBLE);
                searchButton.setEnabled(true);
            } else {
                crossButton.setVisibility(View.GONE);
                searchButton.setEnabled(false);
            }
            adapter.getFilter().filter(charSequence);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void begin() {
        runnables = new Runnable() {
            @Override
            public void run() {

                EditText editText1 = (EditText) findViewById(R.id.editText1);
                searchText = editText1.getText().toString();

                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                String carrier = spinner.getSelectedItem().toString().toLowerCase();

                if(carrier.length() == 0) {
                    trace(searchText);
                } else if(carrier.equals("poslaju")) {
                    trace(new Poslaju(), searchText);
                } else if(carrier.equals("fedex")) {
                    trace(new Fedex(), searchText);
                } else if(carrier.equals("citylink")) {
                    trace(new Citylink(), searchText);
                } else if(carrier.equals("gdex")) {
                    trace(new Gdex(), searchText);
                }
            }
        };

        Thread thread = new Thread(null, runnables, getString(R.string.processing));
        thread.start();

        dialog = ProgressDialog.show(this, getString(R.string.wait_title), getString(R.string.processing_content), true);
    }
    private void trace(String consignmentNo) {
        if(!manager.isExist(consignmentNo)) {
            manager.track(consignmentNo);
        }
        runOnUiThread(returnRes);
    }
    private void trace(Carrier carrier, String consignmentNo) {
        if(!manager.isExist(consignmentNo)) {
            manager.track(carrier, consignmentNo);
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

    /**
     * After track parcel number rebind adapter and show only one item.
     */
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            dialog.dismiss();
            rebind();
            adapter.getFilter().filter(searchText);
        }

    };

    private void setNotification() {

        if(timer != null) timer.cancel();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean enabled = sharedPreferences.getBoolean("prefNotificationEnabled", false);
        //Log.d("DEBUG", "Notification: " + enabled);
        if(!enabled) return;

        String hourStr = sharedPreferences.getString("prefNotificationHour", "0");
        int hour = Integer.parseInt(hourStr);
        if(hour > 0) {
            //Log.d("DEBUG", "Notify every " + hour + " hours");
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

    /**
     * Generate csv
     */
    private void exportAndSend() {

        try {
            String fileName = Environment.getExternalStorageDirectory() + "/" + "parcels.csv";
            manager.export(fileName);

            // send email
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("message/rfc822");//text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

            File output = new File(fileName);
            Uri uri = Uri.fromFile(output);

            intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, getString(R.string.email_title).toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
