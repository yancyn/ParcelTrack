package com.muje.parcel;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yeang-shing.then on 3/21/14.
 */
public class Fedex extends Courier {

    public Fedex() {
        this.name = "fedex";
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

        Log.d("DEBUG", map.toString());
        return map;
    }

    private HttpResponse postJson(String path, Map params) throws Exception {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(path);

        JSONObject holder = new JSONObject(params);
        StringEntity se = new StringEntity(holder.toString());
        httpPost.setEntity(se);

        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        //httpPost.setHeader("Content-Type", "application/json");
        //httpPost.setHeader("Accept-Encoding", "application/json");

        return httpClient.execute(httpPost);//, responseHandler);
    }

    @Override
    public void trace(String consignmentNo) throws Exception {

        // TODO: Fedex trace logic
        this.consignmentNo = consignmentNo;

        this.tracks.clear();

        // send http request
        /*String queryString = String.format(
                "https://www.fedex.com/fedextrack/index.html?tracknumbers=%s",
                consignmentNo);
        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
        HttpPost httpPost = new HttpPost(queryString);
        httpPost.setHeader("Content-type", "application/json");
        */

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("action", "trackpackages");
        map.put("data", composeDataJSON());
        map.put("format", "json");
        map.put("locale", "en_MY");
        map.put("version", 99);

        HttpResponse response = postJson("https://www.fedex.com/trackingCal/track", map);// httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

        String result = "";
        String line = "";
        StringBuilder sb = new StringBuilder();
        while( (line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        result = sb.toString(); // http result
        Log.d("DEBUG", result);

        JSONObject resultJson = new JSONObject(result);
        Log.d("DEBUG", resultJson.toString());
    }

}
