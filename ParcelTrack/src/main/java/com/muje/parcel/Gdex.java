package com.muje.parcel;

import com.muje.util.HtmlParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yeang-shing.then on 3/10/14.
 */
public class Gdex extends Carrier {
    public Gdex() {
        this.name = "gdex";
        this.url = "http://intranet.gdexpress.com/official/etracking.php";
    }
    public Gdex(String consignmentNo) {
        this.name = "gdex";
        this.consignmentNo = consignmentNo;
        this.url = String.format("http://intranet.gdexpress.com/official/etracking.php?capture=%s&Submit=Track", consignmentNo);
    }
    @Override
    public void trace(String consignmentNo) throws Exception {

        this.tracks.clear();

        this.url = String.format("http://intranet.gdexpress.com/official/etracking.php?capture=%s&Submit=Track", consignmentNo);

        HtmlParser parser = new HtmlParser(this.url);
        parser.getTable("<table\\swidth='90%'\\sclass='content[0-9]{0,2}.*?>(.*?)</table>");
        ArrayList<String> lines = parser.getTableLines("<td.*?>(^\\s|.*?)</td>");

        String date = "";
        String desc = "";
        for (int i = 0; i < lines.size(); i++) {
            switch (i % 4) {
                case 2:
                    String line = HtmlParser.getCellValue(lines.get(i));
                    Pattern p = Pattern.compile("[0-9]");
                    Matcher m = p.matcher(line);
                    while(m.find()) {
                        date = line;
                        break;
                    }
                    break;
                case 3:
                    desc = HtmlParser.getCellValue(lines.get(i));
                    if(date.length() > 0) {
                        this.tracks.add(new Track(toDate(date), "", desc));
                        date = "";
                        desc = "";
                    }
                    break;
            }
        }

    }

    private Date toDate(String dateString) {
        Date date = new Date();

        try {
            String[] pieces = dateString.split(" ");
            String[] ddmmyy = pieces[0].split("/");
            String[] ttmmss = pieces[1].split(":");

            int minute = 0;
            if(ttmmss.length > 1) {
                minute = Integer.parseInt(ttmmss[1].trim());
            }
            int second = 0;
            if(ttmmss.length > 2) {
                second = Integer.parseInt(ttmmss[2].trim());
            }
            date = new Date(Integer.parseInt(ddmmyy[2].trim()) - 1900,
                    Integer.parseInt(ddmmyy[1].trim()) - 1,
                    Integer.parseInt(ddmmyy[0].trim()),

                    Integer.parseInt(ttmmss[0].trim()),
                    minute,
                    second);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return date;
    }
	
}
