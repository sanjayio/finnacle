package hpcoe.com.menuhelpdesk;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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


public class ChangePassword extends ActionBarActivity {

    EditText mUserId,mOldPwd,mNewPwd,mConfNewPwd;
    Button mSubmit;
    String userId,oldPwd,newPwd,conf_newPwd;
    private TextView mErrorMsg;
    Validators validators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        /**
         * Initialize the toolbar.
         */

        Toolbar toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        final DatabaseHandler db = new DatabaseHandler(this);
        /**
         * Initialize the UI elements.
         */

        mUserId= (EditText) findViewById(R.id.emp_id);
        mOldPwd= (EditText) findViewById(R.id.emp_old_pwd);
        mNewPwd= (EditText) findViewById(R.id.emp_new_pwd);
        mConfNewPwd= (EditText) findViewById(R.id.emp_conf_new_pwd);
        mErrorMsg = (TextView) findViewById(R.id.errorMessage);
        mSubmit= (Button) findViewById(R.id.submit);

        /**
         * onClickListener for the sign in button. Do input validations here.
         */

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                userId=mUserId.getText().toString();
                oldPwd=mOldPwd.getText().toString();
                newPwd=mNewPwd.getText().toString();
                conf_newPwd = mConfNewPwd.getText().toString();

                validators=new Validators(ChangePassword.this);
                if(validators.isOnline()) {
                    if (  ( !userId.equals("")) && ( !oldPwd.equals("")) && ( !newPwd.equals("")) && ( !conf_newPwd.equals("")))
                    {
                        if(!newPwd.equals(conf_newPwd)){
                            Toast.makeText(getApplicationContext(), "The new passwords don't match! Please try again", Toast.LENGTH_SHORT).show();
                        }else{
                            checkConnection(v);
                            //new ChangePasswordTask().execute();
                            db.addLog("\nChangePassword: Calling changePasswordTask.execute()");
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
                    db.addLog("\nChangePassword: Device not connected to the internet.");
                }
            }
        });

    }

    /**
     * Inflate the options menu.
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_password, menu);
        return true;
    }

    /**
     * There are no options menu items here.
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    /**
     * Only if the device is online, call the changePasswordTask - execute().
     * @param view
     */

    public void checkConnection(View view) {
        if(validators.isOnline()) {
            new ChangePasswordTask().execute();
        }else{
            mErrorMsg.setVisibility(View.VISIBLE);
            mErrorMsg.setText("Error in Network Connection");
            Toast.makeText(ChangePassword.this,"Error in network connection",Toast.LENGTH_LONG).show();

        }
    }

    /**
     * ChangePasswordTask extends AsyncTask.
     */

    private class ChangePasswordTask extends AsyncTask<String,String,JSONArray>{

        ProgressDialog pDialog;
        //Important: Replace this URL with the final server URL.
        public static final String CHANGE_PWD_URL = "http://192.168.43.252:6060/HPCOE/changepwd.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;
        DatabaseHandler db = new DatabaseHandler(ChangePassword.this);
        /**
         * Initiate the processDialog.
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ChangePassword.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Assign the post data and call the connectserver with the corresponding URL and postdata.
         * ConnectServer will return a JSONArray.
         * @param params
         * @return
         */

        @Override
        protected JSONArray doInBackground(String... params) {
            postData = new HashMap<>();

            try {
                postData.put("emp_id",userId);
                postData.put("emp_old_pwd", CryptIt.encrypt(oldPwd));
                postData.put("emp_new_pwd",CryptIt.encrypt(newPwd));
            } catch (Exception e) {
                e.printStackTrace();
            }


            ConnectServer CS = new ConnectServer(ChangePassword.this);
            db.addLog("\nChangePassword: Sending POST data to connect server.");
            return CS.connect(CHANGE_PWD_URL, postData);
        }

        /**
         * The JSONArray returned by the ConnectServer is passed as an argument to onPostExecute.
         * Here the success message is printed as a toast.
         * @param result
         */

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            pDialog.dismiss();

            try {
                db.addLog("\nChangePassword: Received JSON array.");
                JSONObject resultObj = result.getJSONObject(0);
                mStatus = resultObj.getInt("success");
                message = resultObj.getString("message");

                Log.d("Register", "Status code: " + mStatus);
                Log.d("Register", "Message: " + message);

                if (mStatus == 0) {
                    Toast.makeText(getApplicationContext(), "Password Change Unsuccessful: " + message, Toast.LENGTH_LONG)
                            .show();
                    db.addLog("\nChangePassword: Password change failure.");
                } else {
                    Toast.makeText(getApplicationContext(), "Password is successfully updated!", Toast.LENGTH_LONG)
                            .show();
                    db.addLog("\nChangePassword: Password change success.");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
