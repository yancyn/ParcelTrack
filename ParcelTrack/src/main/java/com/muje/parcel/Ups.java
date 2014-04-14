package com.muje.parcel;

import com.muje.util.HtmlParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yeang-shing.then on 4/14/14.
 */
public class Ups extends Carrier {

    public Ups() {
        this.name = "ups";
        this.url = "http://wwwapps.ups.com/WebTracking/track";
    }
    public Ups(String consignmentNo) {
        this.consignmentNo = consignmentNo;
        this.name = "ups";
        this.url = String.format("http://wwwapps.ups.com/WebTracking/track?track=yes&trackNums=%s", consignmentNo);
    }
    @Override
    public void trace(String consignmentNo) throws Exception {
        this.tracks.clear();
        // todo: Take too long for UPS trace
        this.url = String.format("http://wwwapps.ups.com/WebTracking/track?track=yes&trackNums=%s", consignmentNo);
        //this.url = String.format("https://m.ups.com/mobile/track?trackingNumber=%s&t=t", consignmentNo);

        HtmlParser parser = new HtmlParser(this.url);
        parser.getTable("<table(.*)class=\"dataTable\">(.*)</table>");
        ArrayList<String> lines = parser.getTableLines("<td.*?>(^\\s|.*?)</td>");

        String location = "";
        String date = "";
        String time = "";
        String desc = "";
        for(int i=0;i<lines.size();i++) {
            //Log.d("DEBUG", line);
            switch(i%4) {
                case 0:
                    location = parser.getCellValue(lines.get(i));
                    break;
                case 1:
                    date = parser.getCellValue(lines.get(i));
                    break;
                case 2:
                    time = parser.getCellValue(lines.get(i));
                    break;
                case 3:
                    desc = parser.getCellValue(lines.get(i));
                    // todo: Add signed by in desc when it is delivered.
                    this.tracks.add(new Track(toDate(date+ " " + time), location, desc));
                    break;
            }
        }
    }

    private Date toDate(String dateString) throws ParseException {
        Date output = new Date();
        // ie. 30/11/2013 15:21
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("en"));
        output = dateFormat.parse(dateString);
        return output;
    }
}
