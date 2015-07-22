package hpcoe.com.menuhelpdesk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import hpcoe.com.menuhelpdesk.utils.ConnectServer;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 *
 * This is an IntentService which will request the server whenever it is fired.
 * This will check for the updates(if any) made in the database residing in the server.
 *
 * @see hpcoe.com.menuhelpdesk.receivers.UpdateAlarmReceiver to see how it is called.
 * @see ConnectServer for details on connecting to server.
 */
public class UpdateService extends IntentService{

    HashMap<String, String> postData;
    String moduleName,menuOption,submoduleName,dataState,dbVersion,shortDescription,longDescription;
    JSONArray result;
    int mStatus;
    String message;
    String url = "http://192.168.43.252:6060/HPCOE/updateDb.jsp";
    DatabaseHandler db= new DatabaseHandler(this);
    public UpdateService() {
        super(UpdateService.class.getName());
    }

    /**
     * Override the onHandleIntent to implement connection to the server.
     * If there are any updates in the server, this will perform the same updates
     * in the local db.
     * Else, do nothing.
     *
     * @param intent : The intent being passed. (In this case, from the alarm receiver).
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("UpdateService", "Started");
        db.addLog("\nUpdateService: Started");
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        if(!TextUtils.isEmpty(url)) {
            postData = new HashMap<>();
            try {
                postData.put("client_max_db_ver", db.getMaxDbVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }
            result=fetchUpdates();
            if(result!=null) {
                try {
                    JSONObject resultObj = result.getJSONObject(0);
                    mStatus = resultObj.getInt("success");
                    message = resultObj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //assert (mStatus == 0 || mStatus == 1) : "The success value is not 0 or 1";
                Log.d("UpdateService", "Status code: " + mStatus);
                Log.d("UpdateService", "Message: " + message);
                db.addLog("\nUpdateService: Status code: "+mStatus);
                db.addLog("\nUpdateService: Message: "+message);
                if (mStatus == 0) {
                    for (int index = 1; index < result.length(); index++) {
                        try {
                            JSONObject jsonObject = result.getJSONObject(index);
                            moduleName = jsonObject.getString("MODULE_NAME");
                            submoduleName = jsonObject.getString("SUB_MODULE");
                            menuOption = jsonObject.getString("MENU_OPTION");
                            shortDescription = jsonObject.getString("MENU_OPTION_SHORT_DESC");
                            longDescription = jsonObject.getString("MENU_LONG_DESC");
                            dataState = jsonObject.getString("DATA_STATE");
                            dbVersion = jsonObject.getString("DB_VER");
                        } catch (Exception e) {
                            db.addLog("\nUpdateService: caught exception: "+e.getMessage());
                            e.printStackTrace();
                        }
                        if (dataState.equalsIgnoreCase("N")) {
                            db.addMenuOption(moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion);
                        } else {
                            db.updateMenuOption(moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion);
                        }
                    }
                } else {
                    Log.d("UpdateService", "data is already updated");
                }
            }
        }
    }

    /*
        Connection to the server using ConnectServer helper class.
     */
    public JSONArray fetchUpdates(){
        JSONArray temp=null;
        Log.d("UpdateService","Checking for updates");
        db.addLog("\nUpdateService: Checking for updates.");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                temp = new ConnectServer(this).connect(url, postData);
            }catch (Exception e){
                db.addLog("\nUpdateService: Something's wrong");
                Log.d("UpdateService","Something's wrong");
            }
        }else{
            Log.d("UpdateService","Device is not online");
            db.addLog("\nUpdateService: Device is not online.");
        }
        return temp;
    }
}
