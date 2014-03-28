package com.muje.parcel;

import com.muje.util.HtmlParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * Parser for Citylink shipment logic.
 * @author yeang-shing.then
 *
 */
public class Citylink extends Carrier {
	public Citylink() {
		this.name = "citylink";
        this.url = "http://www.citylinkexpress.com/shipmentTrack/index.php";
	}
    public Citylink(String consignmentNo) {
        this.name = "citylink";
        this.url = "http://www.citylinkexpress.com/shipmentTrack/index.php";
    }

	@Override
	public void trace(String consignmentNo) throws Exception {
		
		this.tracks.clear();

        List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
        pairs.add(new BasicNameValuePair("Submit.x","27"));
        pairs.add(new BasicNameValuePair("Submit.y","7"));
        pairs.add(new BasicNameValuePair("no",consignmentNo));
        pairs.add(new BasicNameValuePair("type","consignment"));

        HtmlParser parser = new HtmlParser("http://www.citylinkexpress.com/shipmentTrack/index.php", pairs);
        parser.request();
        ArrayList<String> lines = parser.getTableLines("<td.*class=\"(tabletitle|table_detail)\".*>.*</td>");
		
		//dump track information
		String dateHeader = "";
		String date = "";
		String location = "";
		String status = "";
		int marker = 0;//0 = status, 1 = time, 2 = location
		for(String l: lines) {
			if(l.contains("tabletitle")) {
				marker = 0;//reset				
				if(isDateHeader(l)) {
					dateHeader = HtmlParser.getCellValue(l);
				}
			} else if(l.contains("table_detail")) {
				
				switch(marker%3) {
					case 0:
						status = HtmlParser.getCellValue(l);
						break;
					case 1:
						date = dateHeader + " " + HtmlParser.getCellValue(l);
						break;
					case 2:
						location = HtmlParser.getCellValue(l);
						
						//insert a new track
						this.tracks.add(new Track(toDate(date),location,status));
						
						//reset
						date = "";
						location = "";
						status = "";
						break;
				}				
				
				marker ++;
			}
		}		
		
		//Log.d("DEBUG", "filtered:");
	}
	private boolean isDateHeader(String line) {
		
		String regex = ">\\w+?<";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);
		return !matcher.find();
	}
	/**
	 * Convert to correct date object based on English format.
     * http://tonysilvestri.com/blog/2010/09/27/android-converting-a-date-string-to-date-object/
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	private Date toDate(String date) throws ParseException {

		//convert Monday, March 28, 2011 08:48 PM
		Date output = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a", new Locale("en"));
		output = dateFormat.parse(date);
		
		return output;
	}
}