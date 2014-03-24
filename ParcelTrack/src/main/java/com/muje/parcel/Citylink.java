package com.muje.parcel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

/**
 * Parser for Citylink shipment logic.
 * @author yeang-shing.then
 *
 */
public class Citylink extends Courier {
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
		
		//get response content
		String response = "";
		String line = "";
		String webpage = "http://www.citylinkexpress.com/shipmentTrack/index.php";
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(webpage);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
		pairs.add(new BasicNameValuePair("Submit.x","27"));
		pairs.add(new BasicNameValuePair("Submit.y","7"));
		pairs.add(new BasicNameValuePair("no",consignmentNo));
		pairs.add(new BasicNameValuePair("type","consignment"));
		httpPost.setEntity(new UrlEncodedFormEntity(pairs));
		
		HttpResponse httpResponse = httpClient.execute(httpPost);
		HttpEntity entity = httpResponse.getEntity();
		 
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
		while ((line = reader.readLine()) != null) {
			response += line+"\n";//for regex convenient
			//Log.d("DEBUG",line);
		} reader.close();
		

		//collect table row innerHTML
		ArrayList<String> lines = new ArrayList<String>();
		String regex = "<td.*class=\"(tabletitle|table_detail)\".*>.*</td>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(response);
		while (matcher.find()) {
			lines.add(matcher.group());
		}
		
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
					dateHeader = getCellValue(l);
				}
			} else if(l.contains("table_detail")) {
				
				switch(marker%3) {
					case 0:
						status = getCellValue(l);
						break;
					case 1:
						date = dateHeader + " " + getCellValue(l);
						break;
					case 2:
						location = getCellValue(l);
						
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
	 * Convert to correct date object.
     * http://tonysilvestri.com/blog/2010/09/27/android-converting-a-date-string-to-date-object/
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	private Date toDate(String date) throws ParseException {
		//convert Monday, March 28, 2011 08:48 PM
		Date output = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a");
		output = dateFormat.parse(date);
		
		return output;
	}

}