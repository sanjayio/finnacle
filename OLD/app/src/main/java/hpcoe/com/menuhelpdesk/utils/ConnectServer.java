package hpcoe.com.menuhelpdesk.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Messi10 on 20-May-15.
 */
public class ConnectServer {
    StringBuilder postData;
    BufferedReader reader;
    public JSONArray connect(String url,Map<String,String> params){
        postData=new StringBuilder();

        try {
            for(Map.Entry<String,String> param : params.entrySet())
            {
                if(postData.length()!=0)
                postData.append("&");
                postData.append(URLEncoder.encode(param.getKey(),"UTF-8"));
                postData.append("=");
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()),"UTF-8"));
            }

            byte[] postDataBytes=postData.toString().getBytes();

            URL url1=new URL(url);
            Log.d("Connect Server","Connecting to: "+url);
            HttpURLConnection conn= (HttpURLConnection) url1.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type","application/x-www-form-urlencoded");
            conn.setRequestProperty("content-length",String.valueOf(postDataBytes.length));
            conn.getOutputStream().write(postDataBytes);

            reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            StringBuilder sb=new StringBuilder();
            for(int c=reader.read();c!=-1;c=reader.read())
                sb.append((char)c);

            Log.d("ConnectServer", "Contents are: " + sb.toString());

            assert !sb.toString().isEmpty():"The fetched data is empty";

            return new JSONArray(sb.toString());
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
