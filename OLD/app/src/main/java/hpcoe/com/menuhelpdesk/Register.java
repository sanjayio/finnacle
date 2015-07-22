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


public class Register extends ActionBarActivity {

    EditText mEmpId,mEmpName,mEmpPwd,mEmpEmail,mEmpBank;
    Button mBtnRegister;
    TextView mErrorMsg;
    SharedPreferences settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        mEmpId= (EditText) findViewById(R.id.emp_id);
        mEmpName= (EditText) findViewById(R.id.emp_name);
        mEmpPwd= (EditText) findViewById(R.id.emp_pwd);
        mEmpEmail= (EditText) findViewById(R.id.emp_email);
        mEmpBank= (EditText) findViewById(R.id.emp_bank_name);
        mErrorMsg = (TextView) findViewById(R.id.errorMessage);
        mBtnRegister= (Button) findViewById(R.id.register);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (  ( !mEmpName.getText().toString().equals("")) && ( !mEmpPwd.getText().toString().equals("")) && ( !mEmpId.getText().toString().equals("")) && ( !mEmpEmail.getText().toString().equals("")) && ( !mEmpBank.getText().toString().equals("")) )
                {
                    if ( mEmpName.getText().toString().length() > 4 ){
                        checkConnection(v);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "Username should be minimum 5 characters", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "One or more fields are empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void checkConnection(View view) {
        new NetCheck().execute();
    }

    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(Register.this);
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
                new RegisterTask().execute();
            } else {
                nDialog.dismiss();
                mErrorMsg.setVisibility(View.VISIBLE);
                mErrorMsg.setText("Error in Network Connection");
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RegisterTask extends AsyncTask<String, String, JSONArray> {

        ProgressDialog pDialog;
        public static final String REGISTER_URL = "http://192.168.43.41:6060/HPCOE/register.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;

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

        @Override
        protected JSONArray doInBackground(String... params) {
            postData = new HashMap<>();
            postData.put("emp_id",mEmpId.getText().toString());
            postData.put("emp_name", mEmpName.getText().toString());
            postData.put("emp_password", mEmpPwd.getText().toString());
            postData.put("emp_email",mEmpEmail.getText().toString());
            postData.put("emp_bank_name",mEmpBank.getText().toString());

            ConnectServer CS = new ConnectServer();

            return CS.connect(REGISTER_URL, postData);
        }

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
                } else {
                    Toast.makeText(getApplicationContext(), "Registration Successful!", Toast.LENGTH_SHORT)
                            .show();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("isLoggedIn", true);
                    //editor.putString("username",mEmpName.getText().toString());
                    editor.apply();
                    Intent loggedIn = new Intent(Register.this, Modules_Activity.class);
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
