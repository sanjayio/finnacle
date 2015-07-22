package hpcoe.com.menuhelpdesk;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import hpcoe.com.menuhelpdesk.receivers.UpdateAlarmReceiver;
import hpcoe.com.menuhelpdesk.utils.ConnectServer;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;
import hpcoe.com.menuhelpdesk.utils.Validators;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This activity will first contact the server and download the required data .
 * The data is then stored locally in the database.
 * The Modules fetched will be displayed in a list.
 * This activity also uses SharedPreferences to perform login/logout functionality.
 *
 * @see hpcoe.com.menuhelpdesk.utils.ConnectServer for more details on connecting to server
 * @see DatabaseHandler for more details on storing data locally in a database.
 */
public class Modules extends ActionBarActivity {

    SharedPreferences settings,storePrefs,servicePrefs;
    ListView modules_list;
    ArrayAdapter<String> arrayAdapter;
    DatabaseHandler db = new DatabaseHandler(this);
    boolean isReceiverCalled;

    /**
     * onCreate Method to initialize UI and populate the adapter for listview
     * @param savedInstanceState : Bundle which is sued to retrieve data which is stored
     *                             when activity is destroyed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        //Initialize preferences
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        storePrefs=getSharedPreferences("dataStorePrefs",Context.MODE_PRIVATE);
        servicePrefs=getSharedPreferences("servicePrefs",Context.MODE_PRIVATE);
        //Initialize database
        db = new DatabaseHandler(getApplicationContext());
        //Initialize and setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        //initialize ListView and adapter.
        modules_list = (ListView) findViewById(R.id.modules_list);
        arrayAdapter = new ArrayAdapter<>(this,
                R.layout.simple_list_item);

        /**
         * If the data is not already stored, fetch it from online database. Else populate the listview
         * from the local database.
         */
        boolean isDataStored=storePrefs.getBoolean("isDataStored",false);
        Log.d("Modules","isDataStored: "+isDataStored);
        if(!isDataStored){
            if(new Validators(this).isOnline())
            new NetCheck().execute();
            db.addLog("\nModules: Offline data not available.");
        }
        else{
            HashMap<String, String> f=null;
            try {
                 f = db.getModuleNames();
            }catch (CursorIndexOutOfBoundsException e){
                AlertDialog.Builder alert=new AlertDialog.Builder(this);
                alert.setTitle("Error");
                alert.setMessage("Cannot Fetch data from local database. Do you want to download the data from server?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(new Validators(Modules.this).isOnline()) {
                            db.resetMenuOptionsTable();
                            new NetCheck().execute();
                        }
                        else{
                            Toast.makeText(Modules.this,"Device is offline. Cannot contact server.",Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
                alert.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.show();
            }
            if(f!=null) {
                Set s = f.entrySet();
                for (Object value : s) {
                    Map.Entry me = (Map.Entry) value;
                    arrayAdapter.add((String) me.getValue());
                }
                db.addLog("\nModules: Data fetched from local db and stored.");
            }else{
                SharedPreferences.Editor editor= storePrefs.edit();
                editor.clear();
                editor.putBoolean("isDataStored",false);
                editor.apply();
            }
        }

        View header=getLayoutInflater().inflate(R.layout.submodule_list_header,null);
        TextView headerTv=(TextView) header.findViewById(R.id.listView_header_tv);
        headerTv.setText("Modules");
        modules_list.addHeaderView(header,null,false);
        modules_list.setAdapter(arrayAdapter);

        /**
         * onClickListener for each element in the listview. Call the submodule intent and also pass the module number as
         * an argument with the intent.
         */
        modules_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Modules.this, SubModules.class);
                intent.putExtra("module_no", arrayAdapter.getItem(position-1));
                Log.d("Module Activity", "Module clicked: " + position);
                startActivity(intent);
            }
        });

        modules_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            //int LastFirstVisibleItem=0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(view.getId()==modules_list.getId()){
                    int currentFirstVisibleItem = modules_list.getFirstVisiblePosition();

                    if(currentFirstVisibleItem > 0){
                        getSupportActionBar().setTitle("Modules");
                    }else if(currentFirstVisibleItem==0){
                        getSupportActionBar().setTitle("");
                    }
                   //LastFirstVisibleItem=currentFirstVisibleItem;
                }
            }
        });

      /**
       * get the storeprefs shared preferences value of isReceiverCalled. See if it is true.
       * If false, call the alarm Intent. Else do nothing. Also set the repeating alarm interval here.
       */
        isReceiverCalled=servicePrefs.getBoolean("isReceiverCalled",false);
        if(!isReceiverCalled){
            Intent alarmIntent=new Intent(this, UpdateAlarmReceiver.class);
            PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,alarmIntent,0);
            AlarmManager alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            int interval=30000;
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+interval,interval,pendingIntent);
            Log.d("Module Activity","Started Alarm");
            SharedPreferences.Editor editor = servicePrefs.edit();
            editor.clear();
            editor.putBoolean("isReceiverCalled", true);
            editor.apply();
            db.addLog("\nModules: Update Service Intent called.");
        } else {
            db.addLog("\nModules: Update Service Intent already running.");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modules_, menu);
        return true;
    }

    /**
     * Menu items handler.
     * 3 options in menu button - Search, Suggest and logout.
     * @param item: Item which is selected in the menu
     * @return : True if selected , else false.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_settings) {
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
            alert.setTitle("Caution!");
            alert.setMessage("Use these settings under the supervision of HP technical support team. Do you want to proceed?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Modules.this, Settings.class);
                    startActivity(i);
                }
            });
            alert.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
            return true;
        }

        if(id == R.id.action_search) {
            //call search functionality
            search_from_action_bar();
            return true;
        }

        if(id == R.id.action_suggest) {
            //call suggest intent
            Intent i = new Intent(Modules.this, Suggest.class);
            startActivity(i);
            return true;
        }

        if(id==R.id.action_logout){
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
            alert.setTitle("Logout");
            alert.setMessage("Do you want to logout?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.clear();
                    editor.putBoolean("isLoggedIn", false);
                    editor.apply();
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    //clear all recent values...
                    db.resetUserTable();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();
                }
            });
            alert.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Search from action bar button when clicked will call the MenuOptions class
     * with the intent argument of fetch_all.This button is used to search all the
     * menu options not caring about the modules and submodules.
     */
    private void search_from_action_bar() {
        Intent i = new Intent(Modules.this, MenuOptions.class);
        i.putExtra("sub_module_no", "fetch_all");
        startActivity(i);
    }

    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(Modules.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        /**
         * Gets current device state and checks for working internet connection by trying Google.
         */
        @Override
        protected Boolean doInBackground(String... args) {


            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            HttpURLConnection urlc = null;
            if (netInfo != null && netInfo.isConnected()) {
                try {

                    URL url = new URL("http://www.google.com");
                    urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(5000);
                    urlc.setRequestProperty("User-Agent", "test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    assert urlc != null : "The URLConnection is null";
                    urlc.disconnect();
                }
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean th) {

            if (th) {
                nDialog.dismiss();
                new FetchTask().execute();
            } else {
                nDialog.dismiss();
                Toast.makeText(Modules.this, "Cannot connect to the net.Please try again",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class FetchTask extends AsyncTask<String, String, JSONArray> {

        ProgressDialog pDialog;
        //Important: Replace this URL with the final server URL.
        public static final String FETCH_URL = "http://192.168.43.252:6060/HPCOE/fetchAll.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;
        DatabaseHandler db = new DatabaseHandler(Modules.this);

        /**
         * Override onPreExecute to start progress dialogue and indicate that thread is
         * running.
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Modules.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Call the connectserver along with the postdata. Here we have no postdata.
         * @param params : The parameters of the task.
         * @return : A JSONArray of data fetched from the server.
         */

        @Override
        protected JSONArray doInBackground(String... params) {
            postData = new HashMap<>();
            return new ConnectServer(Modules.this).connect(FETCH_URL, postData);
        }

        /**
         * Overridden onPostExecute method to handle the fetched data-Store it locally
         * and update the adapter of the ListView.
         * @param result: The data returned by doInBackground
         */
        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            String moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion;

            if(result==null){
                db.addLog("\nModules: Data fetched from server is null.");
                Toast.makeText(Modules.this,"Cannot fetch data.Please try again later",Toast.LENGTH_LONG).show();
            }else {
                try {

                    JSONObject resultObj = result.getJSONObject(0);
                    mStatus = resultObj.getInt("success");
                    message = resultObj.getString("message");
                    //assert (mStatus == 0 || mStatus == 1) : "The success value is not 0 or 1";

                    Log.d("Sub Modules", "Status code: " + mStatus);
                    Log.d("Sub Modules", "Message: " + message);

                    if (mStatus == 0) {
                        Toast.makeText(getApplicationContext(), "Data fetched Successfully.", Toast.LENGTH_LONG)
                                .show();
                        pDialog = new ProgressDialog(Modules.this);
                        pDialog.setTitle("Downloading data");
                        pDialog.setMessage("This may take a few minutes...");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(true);
                        pDialog.show();


//                        for (int index = 1; index < result.length(); index++) {
//                            JSONObject jsonObject = result.getJSONObject(index);
//                            moduleName = jsonObject.getString("MODULE_NAME");
//                            submoduleName = jsonObject.getString("SUB_MODULE");
//                            menuOption = jsonObject.getString("MENU_OPTION");
//                            shortDescription = jsonObject.getString("MENU_OPTION_SHORT_DESC");
//                            longDescription = jsonObject.getString("MENU_LONG_DESC");
//                            dataState = jsonObject.getString("DATA_STATE");
//                            dbVersion = jsonObject.getString("DB_VER");
//                            //Log.d("mname", moduleName);
//                            //Log.d("sname", submoduleName);
//                            //Log.d("menuoption", menuOption);
//                            //Log.d("sd",shortDescription);
//                            //Log.d("ld",longDescription);
//                            db.addMenuOption(moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion);
//
//                        }
//
//                        JSONObject[] jsonObjects=new JSONObject[result.length()];
//                        for(int index=1;index<result.length()-1;index++){
//                            jsonObjects[index-1]=result.getJSONObject(index);
//                        }

                        /**
                         * Fetching transposed JSON data.(Optimized data transfer)
                         */
                        JSONObject jsonObject=result.getJSONObject(1);
                        JSONArray moduleArray=jsonObject.getJSONArray("MODULE_NAME");
                        JSONArray subModuleArray=jsonObject.getJSONArray("SUB_MODULE");
                        JSONArray menuOptionArray=jsonObject.getJSONArray("MENU_OPTION");
                        JSONArray shortDescArray=jsonObject.getJSONArray("MENU_OPTION_SHORT_DESC");
                        JSONArray longDescArray=jsonObject.getJSONArray("MENU_LONG_DESC");
                        JSONArray dataStateArray=jsonObject.getJSONArray("DATA_STATE");
                        JSONArray dbVersionArray=jsonObject.getJSONArray("DB_VER");

                        for(int i=0;i<moduleArray.length();i++){
                            moduleName=moduleArray.getString(i);
                            submoduleName=subModuleArray.getString(i);
                            menuOption=menuOptionArray.getString(i);
                            shortDescription=shortDescArray.getString(i);
                            longDescription=longDescArray.getString(i);
                            dataState=dataStateArray.getString(i);
                            dbVersion=dbVersionArray.getString(i);
                            db.addMenuOption(moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion);
                        }

//                        JSONArray[] jsonArrays=new JSONArray[jsonObjects.length];
//                        for(int i=0;i<jsonObjects.length;i++){
//                            jsonArrays[i]=jsonObjects[i].get(i);
//                        }

                        db.addLog("\nModules: Data fetched and stored in database.");

                        SharedPreferences.Editor editor = storePrefs.edit();
                        editor.clear();
                        editor.putBoolean("isDataStored", true);
                        editor.apply();
                        pDialog.dismiss();
                        pDialog = new ProgressDialog(Modules.this);
                        pDialog.setTitle("Populating data");
                        pDialog.setMessage("This may take a few seconds...");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(true);
                        pDialog.show();
                        HashMap<String, String> f = db.getModuleNames();
                        Set s = f.entrySet();
                        for (Object value : s) {
                            Map.Entry me = (Map.Entry) value;
                            arrayAdapter.add((String) me.getValue());
                        }
                        pDialog.dismiss();
                    } else {
                        db.addLog("\nModules: Success code from server is not 0.");
                        Toast.makeText(getApplicationContext(), "Data could not be fetched.Please try again later.", Toast.LENGTH_LONG)
                                .show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    db.addLog("\nModules: Exception caught: "+e.getMessage());
                }
            }
        }
    }
}
