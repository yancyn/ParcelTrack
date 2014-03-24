package com.muje.parcel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.util.Log;

/**
 * Parser for Poslaju shipment logic.
 * @author yeang-shing.then
 *
 */
public class Poslaju extends Courier {
	public Poslaju() {
		this.name = "poslaju";
        this.url = "http://www.poslaju.com.my/track.aspx";
	}
    public Poslaju(String consignmentNo) {
        this.name = "poslaju";
        this.consignmentNo = consignmentNo;
        this.url = String.format("http://www.poslaju.com.my/track.aspx?connoteno=%s", consignmentNo);
    }

	@Override
	public void trace(String consignmentNo) throws Exception {
		this.tracks.clear();

		// send http request
		String response = "";
		String line = "";
		this.url = String.format(
				"http://www.poslaju.com.my/track.aspx?connoteno=%s",
				consignmentNo);
		URL webpage = new URL(this.url);
		URLConnection connection = webpage.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			response += line;
			// Log.d("DEBUG",line);
		}
		reader.close();
		

		// extract delivery html table
		String table = "";
		String regex = String.format("<TABLE.*id=\"%s\".*?>(.*?)</table>", consignmentNo);
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(response);
		while (m.find()) {
			table += m.group();
		}
		//Log.d("DEBUG", "filtered:" + table);

		// dump into collection
		ArrayList<String> lines = new ArrayList<String>();
		regex = "<td.*?>(^\\s|.*?)</td>";
		p = Pattern.compile(regex);
		m = p.matcher(table);
		while (m.find()) {
			lines.add(m.group());
		}
		
		String date = "";
		String location = "";
		String desc = "";
		for (int i = 0; i < lines.size(); i++) {
			//Log.d("DEBUG", lines.get(i));
			switch (i % 3) {
			case 0:
				if (location.length() > 0) {
					this.tracks.add(new Track(toDate(date), location, desc));
					date = "";
					location = "";
					desc = "";
				}
				date = getCellValue(lines.get(i));
				break;
			case 1:
				desc = getCellValue(lines.get(i));
				break;
			case 2:
				location = getCellValue(lines.get(i));
				break;
			}
		}

		// this.tracks.add(new Track(new Date(2012, 1, 1), "BM", "Send"));
		// this.tracks.add(new Track(new Date(2012, 1, 2), "Ipoh", "Collect"));
		// this.tracks.add(new Track(new Date(2012, 1, 3), "Hutan Melintang",
		// "Delivered"));
	}
	/**
	 * Convert Malaysia local date string to date object.
	 * 
	 * @param dateString
	 * @return
	 */
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