package com.muje.util;

import org.json.JSONArray;
import org.json.JSONObject;

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
}
