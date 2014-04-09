package com.muje.parcel;

import android.util.Log;

import com.muje.util.HtmlParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yeang-shing.then on 4/9/14.
 */
public class Skynet extends Carrier {
    public Skynet() {
        this.name = "skynet";
        this.url = "http://www.skynet.com.my/";
    }
    public Skynet(String consignmentNo) {
        this.consignmentNo = consignmentNo;
        this.name = "skynet";
        this.url = String.format("http://www.courierworld.com/scripts/webcourier1.dll/TrackingResultwoheader?nid=1&uffid=&type=4&hawbno=%s", consignmentNo);
    }
    @Override
    public void trace(String consignmentNo) throws Exception {
        this.tracks.clear();
        this.url = String.format("http://www.courierworld.com/scripts/webcourier1.dll/TrackingResultwoheader?nid=1&uffid=&type=4&hawbno=%s", consignmentNo);

        int counter = -1;
        boolean isStart = false;
        boolean isEnd = false;
        String dateString = "";
        Date date = new Date();
        String location = "";
        String desc = "";

        HtmlParser parser = new HtmlParser(this.url);
        parser.getTable("<table bgcolor=\"#dddddd\" width=90%.*</table>");
        ArrayList<String> lines = parser.getTableLines("<td.*?>(^\\s|.*?)</td>");
        for(String line:lines) {
            Log.d("DEBUG", line);
            if(getDateString(line).length()>0) {
                dateString = getDateString(line); // always on hold previous date string
                counter = -1;
                isStart = true;
            }
            counter++;

            if(!isStart) continue;
            int i = (counter-3)%3;
            switch(i) {
                case 0:
                    desc = HtmlParser.getCellValue(line);
                    if(desc.equals("&nbsp;"))
                        break;
                    continue;
                case 1:
                    String dateTimeString = dateString + " " + HtmlParser.getCellValue(line);
                    date = toDate(dateTimeString);
                    continue;
                case 2:
                    location = HtmlParser.getCellValue(line);
                    this.tracks.add(new Track(date, location, desc));
                    continue;
            }
        }
    }

    private String getDateString(String source) {
        String output = "";
        Pattern p = Pattern.compile("[a-zA-Z]{3}, [0-9]{2} [a-zA-Z]{3} [0-9]{4}");
        Matcher m = p.matcher(source);
        if(m.find()) output = m.group();
        return output;
    }

    private Date toDate(String dateString) throws ParseException {
        Date output = new Date();
        // Wed, 09 Apr 2014
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy h:mm a", new Locale("en"));
        output = dateFormat.parse(dateString);
        return output;
    }
}
