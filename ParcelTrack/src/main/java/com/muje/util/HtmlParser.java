package com.muje.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: HTML parser helper.
 * Created by yeang-shing.then on 3/24/14.
 */
public class HtmlParser {

    private String url;
    private HttpPost post = null;

    public HtmlParser(String url) {
        this.url = url;
        this.tableHtml = "";
    }
    public HtmlParser(String url, List<NameValuePair> params ) {
        this.url = url;
        this.tableHtml = "";
    }

    private String tableHtml;
    public void getTable(String regex) throws Exception {

        String response = "";
        URL webpage = new URL(url);
        URLConnection connection = webpage.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            response += line;
        }
        reader.close();

        // extract delivery html table
        tableHtml = "";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(response);
        while (m.find()) {
            tableHtml += m.group();
        }
    }

    private ArrayList<String> lines;
    public ArrayList<String> getTableLines(String regex) {

        // dump into collection
        lines = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(tableHtml);
        while (m.find()) {
            lines.add(m.group());
        }

        return lines;
    }

}
