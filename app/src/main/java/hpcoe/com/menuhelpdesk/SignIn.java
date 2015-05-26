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
import hpcoe.com.menuhelpdesk.utils.CryptIt;

/**
 * Created by Messi10 on 20-May-15.
 */

public class SignIn extends ActionBarActivity {

    EditText mUserName, mUserpwd;
    Button mSearchBtn, mBtnSignIn, mBtnRegister, mModulesButton;
    TextView mErrorMsg;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        if (settings.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(getApplicationContext(), Modules_Activity.class));
            finish();
        }
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        mUserName = (EditText) findViewById(R.id.user_name);
        mUserpwd = (EditText) findViewById(R.id.user_password);
        mBtnSignIn = (Button) findViewById(R.id.btn_sign_in);
        mSearchBtn = (Button) findViewById(R.id.button_search_signin);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mErrorMsg = (TextView) findViewById(R.id.errorMessage);
        mModulesButton = (Button) findViewById(R.id.modulesbutton);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), Register.class);
                startActivity(myIntent);
            }
        });

        mModulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), Modules_Activity.class);
                startActivity(myIntent);
            }
        });


        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), Menu_Options.class);
                startActivity(myIntent);
            }
        });

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if ((!mUserName.getText().toString().equals("")) && (!mUserpwd.getText().toString().equals(""))) {
                    checkConnection(v);
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
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkConnection(View view) {
        new NetCheck().execute();
    }

    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(SignIn.this);
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
                new Login().execute();
            } else {
                nDialog.dismiss();
                mErrorMsg.setVisibility(View.VISIBLE);
                mErrorMsg.setText("Error in Network Connection");
            }
        }
    }

    private class Login extends AsyncTask<String, String, JSONArray> {

        ProgressDialog pDialog;
        public static final String LOGIN_URL = "http://192.168.43.41:6060/HPCOE/test.jsp";
        HashMap<String, String> postData;
        int mStatus;
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SignIn.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            postData = new HashMap<>();
            postData.put("username", mUserName.getText().toString());
            postData.put("password", mUserpwd.getText().toString());

            try {
                Log.d("Login: ","Encrypted Password: " + CryptIt.encrypt(mUserpwd.getText().toString()));
                Log.d("Login: ","Decrypted Password: " + CryptIt.decrypt(CryptIt.encrypt(mUserpwd.getText().toString())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            ConnectServer CS = new ConnectServer();

            return CS.connect(LOGIN_URL, postData);
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            pDialog.dismiss();

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
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("isLoggedIn", true);
                    //editor.putString("username",mUserName.getText().toString());
                    editor.apply();
                    Intent loggedIn = new Intent(SignIn.this, Modules_Activity.class);
                    loggedIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loggedIn);
                    finish();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}