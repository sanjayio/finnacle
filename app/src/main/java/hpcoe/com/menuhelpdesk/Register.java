package hpcoe.com.menuhelpdesk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
import hpcoe.com.menuhelpdesk.utils.Validators;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This activity enables the user to create an account to access this application.
 * The server will perform various validations to ensure that the user is eligible to
 * access the features and the data of it.
 */
public class Register extends ActionBarActivity {

    /**
     * Declare the UI elements.
     */

    EditText mEmpId,mEmpName,mEmpPwd,mEmpEmail,mEmpBank;
    Button mBtnRegister;
    TextView mErrorMsg;
    SharedPreferences settings;
    Validators validators;
    DatabaseHandler db = new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /**
         * Initialize the toolbar.
         */

        Toolbar toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        /**
         * Initialize the UI elements and the shared preferences.
         */

        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        mEmpId= (EditText) findViewById(R.id.emp_id);
        mEmpName= (EditText) findViewById(R.id.emp_name);
        mEmpPwd= (EditText) findViewById(R.id.emp_pwd);
        mEmpEmail= (EditText) findViewById(R.id.emp_email);
        mEmpBank= (EditText) findViewById(R.id.emp_bank_name);
        mErrorMsg = (TextView) findViewById(R.id.errorMessage);
        mBtnRegister= (Button) findViewById(R.id.register);
        validators = new Validators(this);

        /**
         * onClickListener for the register button.
         * Do the input validations here.
         */

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if(validators.isOnline()) {
                    if (  ( !mEmpName.getText().toString().equals(""))  && (!mEmpPwd.getText().toString().equals("")) && ( !mEmpId.getText().toString().equals("")) && ( !mEmpEmail.getText().toString().equals(""))  && ( !mEmpBank.getText().toString().equals("")) )
                    {
                        if(validators.isValidName(mEmpName.getText().toString())) {
                            if(validators.isValidEmailAddress(mEmpEmail.getText().toString())) {
                                if(mEmpPwd.getText().toString().length() > 3) {
                                    if ( mEmpName.getText().toString().length() > 4 ){
                                        checkConnection(v);
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Username should be minimum 5 characters", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Password should be more than 3 characters.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Email should be of the form john@doe.com", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Username should alphanumeric without any spaces or special characters.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "One or more fields are empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Device is not connected to the internet.", Toast.LENGTH_SHORT).show();
                    db.addLog("\nRegister: Device is offline.");
                }
            }
        });

    }

    /**
     * Only if the device is online, call the register task - execute() function.
     * @param view
     */

    public void checkConnection(View view) {
        if(validators.isOnline()) {
            new RegisterTask().execute();
        }else{
            mErrorMsg.setVisibility(View.VISIBLE);
            mErrorMsg.setText("Error in Network Connection");
            Toast.makeText(Register.this,"Error in network connection",Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Inflate the Options menu.
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    /**
     * Options menu item selected handler.
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    /**
     * The RegisterTask class. Give the REGISTER_URL here. This class should extend an AsyncTask.
     */

    private class RegisterTask extends AsyncTask<String, String, JSONArray> {

        ProgressDialog pDialog;
        //Important : Replace this URL with the final server URL.
        public static final String REGISTER_URL = "http://192.168.43.252:6060/HPCOE/register.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;
        DatabaseHandler db = new DatabaseHandler(Register.this);
        /**
         * Initialize the processDialog here.
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Register.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Set the post data. Also send the SIM serial number for security reasons.
         * Call the connect server from here.Connect server sends the data to the URL
         * and returns a JSON array.
         * @param params
         * @return
         */

        @Override
        protected JSONArray doInBackground(String... params) {
            TelephonyManager telemanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            postData = new HashMap<>();
            postData.put("emp_id",mEmpId.getText().toString());
            postData.put("emp_name", mEmpName.getText().toString());
            try {
                postData.put("emp_password", CryptIt.encrypt(mEmpPwd.getText().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            postData.put("emp_email",mEmpEmail.getText().toString());
            postData.put("emp_bank_name",mEmpBank.getText().toString());
            postData.put("emp_sim_srl", telemanager.getSimSerialNumber());

            ConnectServer CS = new ConnectServer(Register.this);

            return CS.connect(REGISTER_URL, postData);
        }

        /**
         * Reads the JSON Array from the connect server as the parameter and stores the user details in the local database.
         * Also call the modules activity after successful storage.
         * @param result
         */

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            pDialog.dismiss();

            try {
                JSONObject resultObj = result.getJSONObject(0);
                mStatus = resultObj.getInt("success");
                message = resultObj.getString("message");
                //assert (mStatus == 0 || mStatus == 1) : "The success value is not 0 or 1";

                Log.d("Register", "Status code: " + mStatus);
                Log.d("Register", "Message: " + message);

                if (mStatus == 0) {
                    Toast.makeText(getApplicationContext(), "Registration Unsuccessful: " + message, Toast.LENGTH_SHORT)
                            .show();
                    db.addLog("\nRegister: Registration failure.");

                } else {
                    Toast.makeText(getApplicationContext(), "Registration Successful!", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("register", "register success");
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    //clear all recent values...
                    db.resetUserTable();
                    db.addUser(resultObj.getString("user_email"), resultObj.getString("user_name"), resultObj.getString("user_id"), resultObj.getString("user_pwd"), resultObj.getString("user_bank"));
                    db.printUserTable();
                    Log.d("register", "register added to db");
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("isLoggedIn", true);
                    //editor.putString("username",mEmpName.getText().toString());
                    editor.apply();
                    db.addLog("\nRegister: Register successful.");
                    Log.d("register", "register intent");
                    Intent loggedIn = new Intent(Register.this, Modules.class);
                    loggedIn.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    loggedIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    loggedIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loggedIn);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
