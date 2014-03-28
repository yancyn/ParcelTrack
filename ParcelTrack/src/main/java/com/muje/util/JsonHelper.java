package com.muje.util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by yeang-shing.then on 3/22/14.
 */
public class JsonHelper {

    private JSONObject json;
    private Object value;
    public JsonHelper(JSONObject json) {
        this.json = json;
        this.value = null;
    }
    public Object getValue(String key) {

        try {
            getValueFromJSON(this.json, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.value;
    }

    private void getValueFromJSON(JSONObject json, String key) throws Exception {

        Iterator iter = json.keys();
        while(iter.hasNext()) {
            String k = (String)iter.next();
            Object obj = json.get(k);
            //Log.d("DEBUG", k + ": " + obj);
            if(k.equals(key)) {
                this.value = obj;
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

    public static HttpResponse postJson(String path, ArrayList<NameValuePair> params) throws Exception {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(path);
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        return httpClient.execute(httpPost);
    }
}
