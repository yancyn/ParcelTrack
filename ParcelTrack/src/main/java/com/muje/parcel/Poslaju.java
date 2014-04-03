package com.muje.parcel;

import com.muje.util.HtmlParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//import android.util.Log;

/**
 * Parser for Poslaju shipment logic.
 * @author yeang-shing.then
 *
 */
public class Poslaju extends Carrier {
	public Poslaju() {
		this.name = "poslaju";
        this.url = "http://www.poslaju.com.my/track.aspx";
	}
    public Poslaju(String consignmentNo) {
        this.name = "poslaju";
        this.consignmentNo = consignmentNo;
        this.url = String.format("http://www.poslaju.com.my/track.aspx?connoteno=%s", consignmentNo);
    }

	// TODO: json http://www.pos.com.my/emstrack/TrackService.asmx   {"connoteno":"EF197274475MY"}
	@Override
	public void trace(String consignmentNo) throws Exception {
		this.tracks.clear();

		//this.url = String.format("http://www.poslaju.com.my/track.aspx?connoteno=%s", consignmentNo);
        //parser.getTable(String.format("<TABLE.*id=\"%s\".*?>(.*?)</table>", consignmentNo));

        this.url = String.format("http://www.pos.com.my/emstrack/viewdetail.asp?parcelno=%s", consignmentNo);
        HtmlParser parser = new HtmlParser(this.url);
        parser.getTable(String.format("<table class=\"login\".*?>(.*?)</table>", consignmentNo));
        ArrayList<String> lines = parser.getTableLines("<td.*?>(^\\s|.*?)</td>");
		
		String date = "";
		String location = "";
		String desc = "";
        // ignore header lines
		for (int i = 4; i < lines.size(); i++) {
			switch (i % 4) {
			case 0:
				date = HtmlParser.getCellValue(lines.get(i));
				break;
			case 1:
                date += " " + HtmlParser.getCellValue(lines.get(i));
                break;
            case 2:
				desc = HtmlParser.getCellValue(lines.get(i));
				break;
			case 3:
				location = HtmlParser.getCellValue(lines.get(i));
                if (location.length() > 0) {
                    this.tracks.add(new Track(toDate(date), location, desc));
                    date = "";
                    location = "";
                    desc = "";
                }
				break;
			}
		}
	}
	/**
	 * Convert Malaysia local date string to date object.
	 * 
	 * @param date
	 * @return
	 */
    private Date toDate(String date) throws ParseException {

        //convert 05-Mar-2014 16:34:00
        Date output = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", new Locale("en"));
        output = dateFormat.parse(date);

        return output;
    }
//    private Date toDate(String dateString) {
//        Date date = new Date();
//
//        try {
//            String[] pieces = dateString.split(" ");
//            String[] ddmmyy = pieces[0].split("/");
//            String[] ttmmss = pieces[1].split(":");
//
//            int minute = 0;
//            if(ttmmss.length > 1) {
//                minute = Integer.parseInt(ttmmss[1].trim());
//            }
//            int second = 0;
//            if(ttmmss.length > 2) {
//                second = Integer.parseInt(ttmmss[2].trim());
//            }
//            date = new Date(Integer.parseInt(ddmmyy[2].trim()) - 1900,
//                    Integer.parseInt(ddmmyy[1].trim()) - 1,
//                    Integer.parseInt(ddmmyy[0].trim()),
//
//                    Integer.parseInt(ttmmss[0].trim()),
//                    minute,
//                    second);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return date;
//    }
	
}