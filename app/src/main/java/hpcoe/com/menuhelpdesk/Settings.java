package hpcoe.com.menuhelpdesk;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hpcoe.com.menuhelpdesk.utils.ConnectServer;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;
import hpcoe.com.menuhelpdesk.utils.ThreadUncaughtExceptionHandler;
import hpcoe.com.menuhelpdesk.utils.Validators;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This is a settings activity.
 *
 * The user can perform the following operations:
 * 1. Clear the local data.
 * 2. Send app logs along with device data.
 * 3. Manually download data from the server.
 */
public class Settings extends ActionBarActivity {

    SharedPreferences settings, storePrefs;
    DatabaseHandler db;
    Button manual_update, clear_local, logcat_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        storePrefs=getSharedPreferences("dataStorePrefs",Context.MODE_PRIVATE);
        boolean isDataStored=storePrefs.getBoolean("isDataStored",false);

        /**
         * Initialize the toolbar and the database handler.
         */

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        db = new DatabaseHandler(getApplicationContext());
        final Validators validators = new Validators(getApplicationContext());

        initUI();

        manual_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validators.isOnline()) {
                    new NetCheck().execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Device is offline.", Toast.LENGTH_SHORT)
                            .show();
                    db.addLog("\nSettings: Device is offline.");
                }
            }
        });

        clear_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.resetMenuOptionsTable();
//                SharedPreferences.Editor editor= storePrefs.edit();
//                editor.clear();
//                editor.putBoolean("isDataStored",false);
//                editor.apply();
                Toast.makeText(getApplicationContext(), "Local databases cleared.", Toast.LENGTH_SHORT)
                        .show();
                db.addLog("\nSettings: Local databases Cleared.");
            }
        });

        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        final String phno = tm.getLine1Number();
        logcat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //replace the email ID's here with the real email ID's.
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "service@hp.com", null));
                i.putExtra(Intent.EXTRA_EMAIL, "service@hp.com");
                i.putExtra(Intent.EXTRA_SUBJECT, "Device information");
                i.putExtra(Intent.EXTRA_TEXT, "\n Andoid OS Version: " + System.getProperty("os.version") +
                "\n API Level: " + Build.VERSION.SDK_INT +
                "\n Device: " + Build.DEVICE +
                "\n Model: " + Build.MODEL +
                "\n Product: " + Build.PRODUCT +
                "\n Phone no: " + phno +
                "\n Free space: " + getFreeSpace() +
                "\n Free RAM: " + getFreeRam() + "MB" +
                "\n LogCat: " +
                db.getLogs());
                try {
                    startActivity(Intent.createChooser(i, "Send Mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(),"No email clients in your device.", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        /**
         * Call the uncaughtExceptionHandler to prevent unexpected app crashes.
         */

        Thread t=Thread.currentThread();
        Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

    }

    public String getFreeRam() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        am.getMemoryInfo(mi);
        long avail = mi.availMem / 1048576L;
        return String.valueOf(avail);
    }

    public String getFreeSpace() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(this, availableBlocks * blockSize);
    }

    public void initUI() {
        manual_update = (Button) findViewById(R.id.manual_update_btn);
        clear_local = (Button) findViewById(R.id.clear_local_db_btn);
        logcat_btn = (Button) findViewById(R.id.logcat_btn);
    }

    /**
     * inflate the options menu.
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Menu button handler.
     * 3 options in menu button - Search, Suggest and logout.
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
     * Netcheck class extends the AsyncTask
     */


    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;

        /**
         * Instantiate the processDialog
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(Settings.this);
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


//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getActiveNetworkInfo();
//            HttpURLConnection urlc = null;
//            if (netInfo != null && netInfo.isConnected()) {
//                try {
//
//                    URL url = new URL("http://www.google.com");
//                    urlc = (HttpURLConnection) url.openConnection();
//                    urlc.setConnectTimeout(5000);
//                    urlc.setRequestProperty("User-Agent", "test");
//                    urlc.setRequestProperty("Connection", "close");
//                    urlc.connect();
//                    if (urlc.getResponseCode() == 200) {
//                        return true;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    assert urlc != null : "The URLConnection is null";
//                    urlc.disconnect();
//                }
//            }
            return true;
        }

        /**
         *
         * @param th
         */

        @Override
        protected void onPostExecute(Boolean th) {

            if (th) {
                nDialog.dismiss();
                new FetchTask().execute();
            } else {
                nDialog.dismiss();
                Toast.makeText(Settings.this, "Cannot fetch data.Please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * FetchTask extends AsyncTask. FETCH_URL is provided here.
     */

    private class FetchTask extends AsyncTask<String, String, JSONArray> {

        ProgressDialog pDialog;
        public static final String FETCH_URL = "http://192.168.43.252:6060/HPCOE/fetchAll.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;

        /**
         * Instantiate the processDialog
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Settings.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Checking for updates...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Call the connectserver along with the postdata. Here we have no postdata.
         * @param params
         * @return
         */

        @Override
        protected JSONArray doInBackground(String... params) {
            postData = new HashMap<>();
            return new ConnectServer(Settings.this).connect(FETCH_URL, postData);
        }

        /**
         * Accept the JSON array as a parameter and store the values in the local database. Change the isDataStored sharedPreference\
         * and update the array adapter.
         * @param result
         */

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            String moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion;

            try {

                JSONObject resultObj = result.getJSONObject(0);
                mStatus = resultObj.getInt("success");
                message = resultObj.getString("message");
                //assert (mStatus == 0 || mStatus == 1) : "The success value is not 0 or 1";

                Log.d("Sub Modules", "Status code: " + mStatus);
                Log.d("Sub Modules", "Message: " + message);

                if (mStatus == 0) {
                    Toast.makeText(getApplicationContext(), "Data fetched Successfully. ", Toast.LENGTH_LONG)
                            .show();
                    pDialog = new ProgressDialog(Settings.this);
                    pDialog.setTitle("Populating data");
                    pDialog.setMessage("This may take a few minutes...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(true);
                    pDialog.show();

                    db.resetMenuOptionsTable();
                    for(int index=1;index<result.length();index++){
                        JSONObject jsonObject=result.getJSONObject(index);
                        moduleName = jsonObject.getString("MODULE_NAME");
                        submoduleName = jsonObject.getString("SUB_MODULE");
                        menuOption = jsonObject.getString("MENU_OPTION");
                        shortDescription = jsonObject.getString("MENU_OPTION_SHORT_DESC");
                        longDescription = jsonObject.getString("MENU_LONG_DESC");
                        dataState = jsonObject.getString("DATA_STATE");
                        dbVersion = jsonObject.getString("DB_VER");
                        db.addMenuOption(moduleName, submoduleName, menuOption, shortDescription, longDescription, dataState, dbVersion);
                    }

                    db.addLog("\nSettings: Manual data fetch successful.");
                    SharedPreferences.Editor editor = storePrefs.edit();
                    editor.clear();
                    editor.putBoolean("isDataStored", true);
                    editor.apply();
                    pDialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "Data could not be fetched.Please try again later.", Toast.LENGTH_LONG)
                            .show();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
