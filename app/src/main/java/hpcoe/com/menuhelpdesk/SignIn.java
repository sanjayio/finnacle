package hpcoe.com.menuhelpdesk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;
import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import hpcoe.com.menuhelpdesk.utils.ConnectServer;
import hpcoe.com.menuhelpdesk.utils.CryptIt;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;
import hpcoe.com.menuhelpdesk.utils.ThreadUncaughtExceptionHandler;
import hpcoe.com.menuhelpdesk.utils.Validators;

/**
 * Created by Messi10 on 20-May-15.
 */

public class SignIn extends ActionBarActivity {

    /**
     * Declare the UI elements.
     */

    EditText mUserName, mUserpwd;
    Button mBtnSignIn;
    TextView mErrorMsg;
    SharedPreferences settings;
    FloatingActionsMenu fam;
    FloatingActionButton mFabRegister,mFabChangePwd;
    FrameLayout mFrameLayout;
    Validators validators;
    DatabaseHandler db = new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        /**
         * Set the shared Preferences for storing session values.
         */

//        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
//        if (settings.getBoolean("isLoggedIn", false)) {
//            startActivity(new Intent(getApplicationContext(), Modules.class));
//            finish();
//        }

        /**
         * Initialize the toolbar.
         */

        db.addLog("\nSignIn: Loading SignIn.");
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        /**
         * Initialize the UI elements.
         */

        mUserName = (EditText) findViewById(R.id.user_name);
        mUserpwd = (EditText) findViewById(R.id.user_password);
        mBtnSignIn = (Button) findViewById(R.id.btn_sign_in);
        mErrorMsg = (TextView) findViewById(R.id.errorMessage);
        mFrameLayout= (FrameLayout) findViewById(R.id.frame_layout);
        mFabRegister= (FloatingActionButton) findViewById(R.id.fab_register);
        mFabChangePwd= (FloatingActionButton) findViewById(R.id.fab_changepwd);
        validators = new Validators(this);

        /**
         * onClickListener for register button.
         */

        mFabRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), Register.class);
                startActivity(myIntent);
            }
        });

        /**
         * onClickListener for change password button.
         */

        mFabChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangePassword.class));
            }
        });

        /**
         * OnCLick listener for the sign in button.
         * Do input validations here.
         */

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if(validators.isOnline()) {
                    if ((!mUserName.getText().toString().equals("")) && (!mUserpwd.getText().toString().equals(""))) {
                        if(validators.isValidName(mUserName.getText().toString())) {
                            checkConnection(v);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Invalid Username", Toast.LENGTH_SHORT).show();
                        }
                    } else if ((!mUserName.getText().toString().equals(""))) {
                        Toast.makeText(getApplicationContext(),
                                "Password field empty", Toast.LENGTH_SHORT).show();
                    } else if ((!mUserpwd.getText().toString().equals(""))) {
                        Toast.makeText(getApplicationContext(),
                                "User name field empty", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "User name and Password fields are empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Device is not connected to internet.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /**
         * Initialize the unhandled exceptions thread to prevent app crashing.
         */

        Thread t=Thread.currentThread();
        Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

        /**
         * Initialize the floating action button. And add behaviours to it.
         */

        fam= (FloatingActionsMenu) findViewById(R.id.fam);
        fam.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mFrameLayout.setVisibility(View.VISIBLE);
                mFrameLayout.setBackgroundColor(Color.argb(200, 255, 255, 255));
            }

            @Override
            public void onMenuCollapsed() {
                mFrameLayout.setVisibility(View.INVISIBLE);
                mFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        /**
         * Remove the transparent overlay when the fab is pressed by touching the overlay.
         */

        mFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(fam.isExpanded()) {
                    fam.collapse();
                }
                return true;
            }
        });

        db.addLog("\nSignIn: All the Listeners in this activity are loaded.");
    }


    /**
     *
     * @param menu
     * @return
     *
     * Inflate the menu.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     *
     * This method takes care of the menu button in the app.
     * The menu button can be identified by 3 vertical dots on the top right.
     *
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param view
     *
     * Only if the device is online, call the login - execute().
     */

    public void checkConnection(View view) {
        if(validators.isOnline()) {
            //VERY IMPORTANT!!! COMMENT THE BELOW LINE AND UNCOMMENT THE LINE BELOW IT.
            //startActivity(new Intent(getApplicationContext(), Modules.class));
            new Login().execute();
        }else{
            mErrorMsg.setVisibility(View.VISIBLE);
            mErrorMsg.setText("Error in Network Connection");
            Toast.makeText(SignIn.this,"Error in network connection",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Class login.
     * Takes care of the sign in process. The login url is provided here.
     */

    private class Login extends AsyncTask<String, String, JSONArray> {

        ProgressDialog pDialog;
        //Important : Replace this URL with the final server URL.
        public static final String LOGIN_URL = "http://192.168.43.252:6060/HPCOE/signin.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;

        /**
         * This method starts the login process.
         * It initiates the processDialog window.
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            db.addLog("SignIn: Starting Connection from signin");
            pDialog = new ProgressDialog(SignIn.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         *
         * @param params
         * @return
         *
         * This method creates the post data and calls the connect server. Connect server sends the data to the URL
         * and returns a JSON array.
         */

        @Override
        protected JSONArray doInBackground(String... params) {
            postData = new HashMap<>();
            TelephonyManager telemanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                postData.put("username", mUserName.getText().toString());
                postData.put("password", CryptIt.encrypt(mUserpwd.getText().toString()));
                postData.put("emp_sim_srl", telemanager.getSimSerialNumber());
                Log.d("Login: ","Encrypted Password: "+ CryptIt.encrypt(mUserpwd.getText().toString()));
                Log.d("Login: ","Decrypted Password: "+ CryptIt.decrypt(CryptIt.encrypt(mUserpwd.getText().toString())));
            } catch (Exception e) {
                db.addLog("\nSignIn: Exception caught: "+e.getMessage());
                e.printStackTrace();
            }
            ConnectServer CS = new ConnectServer(SignIn.this);
            return CS.connect(LOGIN_URL, postData);
        }

        /**
         *
         * @param result
         * This function takes in the JSON Array as the parameter, stores the user details in the local database
         * and starts the modules activity.
         */

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(result!=null) {
                try {

                    JSONObject resultObj = result.getJSONObject(0);
                    mStatus = resultObj.getInt("success");
                    message = resultObj.getString("message");
                    assert (mStatus == 0 || mStatus == 1) : "The success value is not 0 or 1";
                    Log.d("Login", "Status code: " + mStatus);
                    Log.d("Login", "Message: " + message);
                    if (mStatus == 0) {
                        Toast.makeText(getApplicationContext(), "Login Unsuccessful: " + message, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT)
                                .show();
                        Log.d("login", "success");

                        //clear all recent values...
                        db.resetUserTable();
                        db.addUser(resultObj.getString("user_email"), resultObj.getString("user_name"), resultObj.getString("user_id"), resultObj.getString("user_pwd"), resultObj.getString("user_bank"));
                        db.printUserTable();
                        Log.d("login", "database insert");
//                        SharedPreferences.Editor editor = settings.edit();
//                        editor.clear();
//                        editor.putBoolean("isLoggedIn", true);
                        //editor.putString("username",mUserName.getText().toString());
                       // editor.apply();
                        Log.d("login", "intent");

                        Intent loggedIn = new Intent(SignIn.this, Modules.class);
                        loggedIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loggedIn);
                        finish();
                    }
                    db.addLog("\nSignIn: Finished authorization");

                } catch (JSONException e) {
                    db.addLog("\nSignIn: Exception caught in onPostExecute: "+e.getMessage());
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(SignIn.this,"Data Could not be fetched. Please try again later.",Toast.LENGTH_SHORT).show();
            }
        }
    }

}