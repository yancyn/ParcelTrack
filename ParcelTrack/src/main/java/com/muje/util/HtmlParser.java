package com.muje.util;

import android.text.Html;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML parser helper.
 * Created by yeang-shing.then on 3/24/14.
 */
public class HtmlParser {

    private String url;
    private HttpPost post = null;

    /**
     * Recommended constructor.
     * <quote>
     *     HtmlParser parser = new HtmlParser(this.url);
     *     parser.getTable(String.format("<TABLE.*id=\"%s\".*?>(.*?)</table>", consignmentNo));
     *     ArrayList<String> lines = parser.getTableLines("<td.*?>(^\\s|.*?)</td>");
     * </quote>
     * @param url
     */
    public HtmlParser(String url) {
        this.url = url;
        this.tableHtml = "";
        this.params = new ArrayList<NameValuePair>();
    }

    private List<NameValuePair> params;

    /**
     * Constructor for direct get table lines.
     * <quote>
     *      HtmlParser parser = new HtmlParser("http://www.citylinkexpress.com/shipmentTrack/index.php", pairs);
     *      parser.request();
     *      ArrayList<String> lines = parser.getTableLines("<td.*class=\"(tabletitle|table_detail)\".*>.*</td>");
     * </quote>
     * @param url
     * @param params
     */
    public HtmlParser(String url, List<NameValuePair> params) {
        this.url = url;
        this.tableHtml = "";
        this.params = params;
    }

    public void request() throws Exception {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(this.url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity entity = httpResponse.getEntity();

        String response = "";
        String line = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
        while ((line = reader.readLine()) != null) {
            response += line + "\n";// HACK: required for CitiLink. The rest not required otherwise fail
        }
        reader.close();

        // extract delivery html table
        tableHtml = response;
    }



    private String tableHtml;
    public void getTable(String regex) throws Exception {

        //URL webpage = new URL(url);
        //URLConnection connection = webpage.openConnection();

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(this.url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity entity = httpResponse.getEntity();

        String response = "";
        String line = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
        while ((line = reader.readLine()) != null) {
            response += line;
        }
        reader.close();

        //response = EntityUtils.toString(entity, HTTP.UTF_8);
        //response = Html.escapeHtml(response); // require min sdk

        // extract delivery html table
        tableHtml = "";
        if(regex == null) {
            tableHtml = response;
        } else if(regex.length() == 0) {
            tableHtml = response;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(response);
            while (m.find()) {
                tableHtml += m.group();
            }
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

    /**
     * Return inner html for table's cell.
     *
     * @param td Html
     * @return
     */
    public static String getCellValue(String td) {

        String inner = "";
        String regex = ">(.*?)<";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(td);
        while (m.find()) {
            inner += m.group();
        }
        inner = inner.replaceAll(">","").replaceAll("<","").trim();

        return inner;
    }

}
