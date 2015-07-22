package hpcoe.com.menuhelpdesk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This activity is will display the description of the Menu Option which the user has clicked
 * interested to know/use.
 */
public class Description extends ActionBarActivity {

    TextView shortDesc,longDesc;
    //@TargetApi(Build.VERSION_CODES.KITKAT)
    SharedPreferences settings;
    DatabaseHandler db = new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        /**
         * Get the shared preference loginPrefs.
         */
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        /**
         * Get the menu_option from intent extra parameter.
         */

        Toast.makeText(this,getIntent().getStringExtra("menu_option"),Toast.LENGTH_LONG).show();
        String type=getIntent().getStringExtra("menu_option");
        db.addLog("\nDescription: Got intent Extra from Menu Option: " + type);
        /**
         * Initialize the toolbar.
         */

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(type);

        /**
         * Initialize the database handler. Add the short description and the long description to models.
         */

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        HashMap<String, String> result = db.getShortAndLong(type);
        ArrayList<String> mModels = new ArrayList<>();
        Set s = result.entrySet();
        db.addLog("\nDescription: Start loading description from database.");
        for (Object value : s) {
            Map.Entry me = (Map.Entry) value;
            String temp = (String) me.getValue();
            String[] items = temp.split("\\$\\$\\$");
            //comment the below line if the build version is not kitkat.
            //temp.replaceAll("\n", System.lineSeparator());
           // Log.d("Options Short desc:", Arrays.toString(items));
            mModels.add(items[0]);
            mModels.add(items[1]);
        }
        db.addLog("\nDescription: Finish loading description from database.");

        Log.d("Short:", mModels.get(0));
        Log.d("Long:", mModels.get(1));

        /**
         * Add the short descriptions and long descriptions to the textviews.
         */

        shortDesc= (TextView) findViewById(R.id.menu_short_desc);
        longDesc= (TextView) findViewById(R.id.menu_long_desc);
        //longDesc.setMovementMethod(LinkMovementMethod.getInstance());
        shortDesc.setText(Html.fromHtml(mModels.get(0)));
        longDesc.setText(Html.fromHtml(mModels.get(1)));
    }

    /**
     * Inflate the options menu.
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_description, menu);
        return true;
    }

    /**
     * Options menu item selected handler.
     * Options menu items are suggest, and logout.
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_suggest) {
            //call suggest intent
            Intent i = new Intent(this, Suggest.class);
            startActivity(i);
            return true;
        }

        if(id == R.id.action_settings) {
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
            alert.setTitle("Caution!");
            alert.setMessage("Use these settings under the supervision of HP technical support team. Do you want to proceed?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Description.this, Settings.class);
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

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }
}
