package hpcoe.com.menuhelpdesk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Modules_Activity extends ActionBarActivity {

    SharedPreferences settings;
    List<Map<String, String>> modulesList = new ArrayList<Map<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);
        settings=getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        initList();

        Toolbar toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ListView lv = (ListView) findViewById(R.id.modules_list);

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, modulesList, android.R.layout.simple_list_item_1, new String[] {"ABCD"}, new int[] {android.R.id.text1});
        lv.setAdapter(simpleAdapter);
    }

    private void initList() {
        modulesList.add(createModule("ABCD", "abcd"));
        modulesList.add(createModule("ABCD", "efgh"));
        modulesList.add(createModule("ABCD", "ijkl"));
        modulesList.add(createModule("ABCD", "mnop"));
        modulesList.add(createModule("ABCD", "qrst"));
    }

    private HashMap<String, String> createModule(String key, String val) {
        HashMap<String, String> module = new HashMap<String, String>();
        module.put(key, val);
        return module;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modules_, menu);
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


}
