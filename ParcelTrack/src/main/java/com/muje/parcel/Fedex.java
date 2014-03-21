package com.muje.parcel;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

        //Log.d("DEBUG", map.toString());
        return map;
    }
    private HttpResponse postJson(String path) throws Exception {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(path);

        JSONObject holder = new JSONObject(composeDataJSON());

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "trackpackages"));
        params.add(new BasicNameValuePair("data", holder.toString()));
        params.add(new BasicNameValuePair("format", "json"));
        params.add(new BasicNameValuePair("locale", "en_US"));
        params.add(new BasicNameValuePair("version", "99"));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        return httpClient.execute(httpPost);
    }
    private HttpResponse postJson(String path, Map params) throws Exception {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(path);

        JSONObject holder = new JSONObject(params);
        StringEntity se = new StringEntity(holder.toString());
        httpPost.setEntity(se);

        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        return httpClient.execute(httpPost);
    }
    private void getValueFromJSON(JSONObject json, String key) throws Exception {

        Iterator iter = json.keys();
        while(iter.hasNext()) {
            String k = (String)iter.next();
            Object obj = json.get(k);
            Log.d("DEBUG", k + ": " + obj);
            if(k.equals(key)) {
                holder = obj;
                return;
            }

            if(obj instanceof JSONObject) {
                getValueFromJSON((JSONObject)obj, key);
            } else if(obj instanceof JSONArray) {
                getValueFromJSONArray((JSONArray)obj, key);
            }
        }
    }
    private void getValueFromJSONArray(JSONArray array, String key) throws Exception {

        for(int i=0;i<array.length();i++) {
            Object obj = array.get(i);
            if(obj instanceof JSONObject) {
                getValueFromJSON((JSONObject)obj, key);
            } else if(obj instanceof JSONArray) {
                getValueFromJSONArray((JSONArray)obj, key);
            }
        }
    }

    private Object holder = null;
    @Override
    public void trace(String consignmentNo) throws Exception {
        this.consignmentNo = consignmentNo;
        this.tracks.clear();

        // TODO: Fedex trace logic
        HttpResponse response = postJson("https://www.fedex.com/trackingCal/track");
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
        getValueFromJSON(resultJson, "shipperCity");
        Log.d("DEBUG", "Shipper City: " + holder);
    }

}
