package hpcoe.com.menuhelpdesk.utils;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import hpcoe.com.menuhelpdesk.SignIn;

/**
 * Created by: Abhijith gururaj and Sanjay Kumar.
 *
 *
 * This is helper class used to establish a connection between the device
 * and the server.
 */
public class ConnectServer {
    private Context context;
    StringBuilder postData;
    BufferedReader reader;

    public ConnectServer(Context context) {
        this.context = context;
    }

    /**
     * This method uses HTTPURLConnection to connect to the server.
     * Connection Timeout is 5 seconds.
     * The data is sent to the server in POST Method.
     * Encoding for send/receive is UTF-8.
     * Response from the server is in JSON Format.
     *
     * @param url    : The url of the server
     * @param params : The data to be sent to the server(in POST Method)
     * @return : The data fetched for the server in JSON format.
     */
    public JSONArray connect(String url, Map<String, String> params) {
        DatabaseHandler db = new DatabaseHandler(context);
        postData = new StringBuilder();
        db.addLog("\nConnectServer: Connecting to Server.");
        try {
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0)
                    postData.append("&");
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append("=");
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }

            byte[] postDataBytes = postData.toString().getBytes();

            URL url1 = new URL(url);
            Log.d("Connect Server", "Connecting to: " + url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("content-length", String.valueOf(postDataBytes.length));
            conn.getOutputStream().write(postDataBytes);

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c = reader.read(); c != -1; c = reader.read())
                sb.append((char) c);

            Log.d("ConnectServer", "Contents are: " + sb.toString());
            db.addLog("\nConnectServer: Successfully Connected to the server.");
            reader.close();
            return new JSONArray(sb.toString());
        } catch (SocketTimeoutException ste) {
            db.addLog("\nConnect Server: Exception caught: " + ste.getMessage());
            ste.printStackTrace();
        } catch (JSONException e) {
            db.addLog("\nConnect Server: Exception caught: " + e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            db.addLog("\nConnect Server: Exception caught: " + e.getMessage());
            e.printStackTrace();

        }

        return null;
    }
}
