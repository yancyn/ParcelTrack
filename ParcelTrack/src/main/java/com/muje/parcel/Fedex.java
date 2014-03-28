package com.muje.parcel;

import com.muje.util.JsonHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yeang-shing.then on 3/21/14.
 */
public class Fedex extends Carrier {

    public Fedex() {
        this.name = "fedex";
        this.url = "https://www.fedex.com/fedextrack/index.html";
    }
    public Fedex(String consignmentNo) {
        this.name = "fedex";
        this.consignmentNo = consignmentNo;
        this.url = String.format("https://www.fedex.com/fedextrack/index.html?tracknumbers=%s", consignmentNo);
    }

    private Map<String, Object> composeDataJSON() {

        Map<String, Object> map = new HashMap<String, Object>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("anonymousTransaction", true);
        params.put("clientId", "WTRK");
        params.put("returnDetailedErrors", true);
        params.put("returnLocalizedDateTime", false);

        Map<String, Object> track = new HashMap<String, Object>();
        track.put("trackingNumber", this.consignmentNo);
        track.put("trackingQualifier", "");
        track.put("trackingCarrier", "");

        Map<String, Object> info = new HashMap<String, Object>();
        info.put("trackNumberInfo", track);
        ArrayList<Map> infoArray = new ArrayList<Map>();
        infoArray.add(info);

        Map<String, Object> packageRequest = new HashMap<String, Object>();
        packageRequest.put("appType", "wtrk");
        packageRequest.put("uniqueKey", "");
        packageRequest.put("processingParameters", params);
        packageRequest.put("trackingInfoList", infoArray);
        map.put("TrackPackagesRequest", packageRequest);

        //Log.d("DEBUG", map.toString());
        return map;
    }

    @Override
    public void trace(String consignmentNo) throws Exception {

        this.consignmentNo = consignmentNo;
        this.url = String.format("https://www.fedex.com/fedextrack/index.html?tracknumbers=%s", consignmentNo);
        this.tracks.clear();

        JSONObject holder = new JSONObject(composeDataJSON());
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "trackpackages"));
        params.add(new BasicNameValuePair("data", holder.toString()));
        params.add(new BasicNameValuePair("format", "json"));
        params.add(new BasicNameValuePair("locale", "en_US"));
        params.add(new BasicNameValuePair("version", "99"));

        HttpResponse response = JsonHelper.postJson("https://www.fedex.com/trackingCal/track", params);
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

        String result = "";
        String line = "";
        StringBuilder sb = new StringBuilder();
        while( (line = reader.readLine()) != null) {
            sb.append(line);
        }
        result = sb.toString();

        JSONObject resultJson = new JSONObject(result);
        JsonHelper jsonHelper = new JsonHelper(resultJson);

        String origin = jsonHelper.getValue("originCity") + ", " + jsonHelper.getValue("originStateCD") + " " + jsonHelper.getValue("originCntryCD");
        Object sent = jsonHelper.getValue("shipDt");
        this.tracks.add(new Track(toDate(sent.toString()), origin, ""));

        Object status = jsonHelper.getValue("keyStatusCD");
        Object destination = jsonHelper.getValue("destLocationCity") + ", " + jsonHelper.getValue("destLocationStateCD") + " " + jsonHelper.getValue("destLocationCntryCD");
        // status = delivered
        if(status != null && status.toString().equals("DL")) {
            Object deliveredDate = jsonHelper.getValue("actDeliveryDt");
            Object desc = jsonHelper.getValue("statusWithDetails");
            this.tracks.add(
                    new Track(toDate(deliveredDate.toString()), destination.toString(), desc.toString())
            );
        }
    }

    /**
     * Convert to correct date object.
     * http://tonysilvestri.com/blog/2010/09/27/android-converting-a-date-string-to-date-object/
     * @param date
     * @return
     * @throws java.text.ParseException
     */
    private Date toDate(String date) throws ParseException {

        //convert 2013-12-06T16:19:00+00:00
        Date output = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", new Locale("en"));
        output = dateFormat.parse(date);

        return output;
    }
	
}