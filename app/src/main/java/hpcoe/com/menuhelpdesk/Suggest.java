package hpcoe.com.menuhelpdesk;

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
import android.widget.Spinner;
import android.widget.Toast;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import hpcoe.com.menuhelpdesk.utils.ConnectServer;
import hpcoe.com.menuhelpdesk.utils.CryptIt;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;
import hpcoe.com.menuhelpdesk.utils.ThreadUncaughtExceptionHandler;
import hpcoe.com.menuhelpdesk.utils.Validators;

/**
 * Created by sanjay on 10/6/15.
 */
public class Suggest extends ActionBarActivity {

    SharedPreferences settings;
    Spinner category;
    EditText edit_subject, edit_desc;
    FloatingActionButton edit_button;
    Validators validators;
    DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        db = new DatabaseHandler(getApplicationContext());
        db.addLog("\nSuggest: Suggest Activity begin loading.");
        initUI();


        validators = new Validators(this);

        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (validators.isOnline()) {
                    if ((edit_subject.getText().toString() == "") || (edit_desc.getText().toString() == "")) {
                        Toast.makeText(getApplicationContext(),
                                "One or more fields are empty.", Toast.LENGTH_SHORT).show();
                    } else {
                        //replace email ID's here with the real email ID's.
                        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "service@hp.com", null));
                        i.putExtra(Intent.EXTRA_EMAIL, "service@hp.com");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                        i.putExtra(Intent.EXTRA_TEXT, "\n Category: " + category.getSelectedItem().toString() +
                                "\n Subject: " + edit_subject.getText().toString() +
                                "\n Description: " + edit_desc.getText().toString());
                        try {
                            startActivity(Intent.createChooser(i, "Send Mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getApplicationContext(),"No email clients in your device.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Device is offline. Please connect to the internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Thread t=Thread.currentThread();
        Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

    }

    public void initUI() {
        category = (Spinner) findViewById(R.id.spinner);
        edit_subject = (EditText) findViewById(R.id.edit_subject);
        edit_desc = (EditText) findViewById(R.id.edit_desc);
        edit_button = (FloatingActionButton) findViewById(R.id.edit_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_suggest, menu);
        return true;
    }

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
                    editor.putBoolean("isLoggedIn", false);
                    editor.apply();
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    //clear all recent values...
                    db.resetUserTable();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    db.addLog("\nSuggest: Logged out Successfully.");
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

}
