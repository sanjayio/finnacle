package hpcoe.com.menuhelpdesk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This activity is used to display all the SubModules of a Module which is clicked by the
 * user in the Modules Activity.
 *
 * @see hpcoe.com.menuhelpdesk.Modules to check the list of modules which a user can click.
 * @see DatabaseHandler for more details on fetching data from database.
 */

public class SubModules extends ActionBarActivity{

    ListView subModuleList;
    Toolbar toolbar;
    String type;
    ArrayAdapter<String> arrayAdapter;
    SharedPreferences storeMenus;
    String pos;

    /**
     * Initialize UI and ArrayAdapter for the list.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_modules);

        subModuleList= (ListView) findViewById(R.id.sub_modules_list);
        /**
         * Get the intent extra parameter.
         */
        type=getIntent().getStringExtra("module_no");
        storeMenus=getSharedPreferences("userClicked", Context.MODE_PRIVATE);
        DatabaseHandler db = new DatabaseHandler(this);
        db.addLog("\nSubModules: Loading SubModules");
        if(type==null)
            type=storeMenus.getString("latest_module_clicked","Assets");
        else
        {
            SharedPreferences.Editor editor = storeMenus.edit();
            editor.putString("latest_module_clicked",type);
            editor.apply();
        }
        //Initialize and setup the toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("");


        arrayAdapter=new ArrayAdapter<>(this,R.layout.simple_list_item);

        View header=getLayoutInflater().inflate(R.layout.submodule_list_header,null);
        TextView headerTv=(TextView) header.findViewById(R.id.listView_header_tv);
        headerTv.setText(type);
        subModuleList.addHeaderView(header, null, false);
        subModuleList.setAdapter(arrayAdapter);
        subModuleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SubModules.this, MenuOptions.class);
                pos = arrayAdapter.getItem(position-1);
                intent.putExtra("sub_module_no", pos);
                Log.d("Sub_Module Activity", "Sub_Module clicked: " + pos);
                startActivity(intent);
            }
        });
        db.addLog("\nSubModules: ListView is set. Now loading items from local db.");
        //Add the data to the adapter which is fetched from local database.

        HashMap<String, String> f = db.getSubmodulesNames(type);
        Set s = f.entrySet();
        for (Object value : s) {
            Map.Entry me = (Map.Entry) value;
            arrayAdapter.add((String) me.getValue());
        }

        subModuleList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(view.getId()==subModuleList.getId()){
                    int currentFirstVisibleItem = subModuleList.getFirstVisiblePosition();

                    if(currentFirstVisibleItem > 0){
                        getSupportActionBar().setTitle("Modules");
                    }else if(currentFirstVisibleItem==0){
                        getSupportActionBar().setTitle("");
                    }

                }
            }
        });
            db.addLog("\nSubModules: SubModules Loaded Successfully");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sub_modules, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if(id == R.id.action_search) {
            //call search function
            search_from_action_bar();
            return true;
        }

        if(id == R.id.action_settings) {
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
            alert.setTitle("Caution!");
            alert.setMessage("Use these settings under the supervision of HP technical support team. Do you want to proceed?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(SubModules.this, Settings.class);
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

        if(id == R.id.action_suggest) {
            //call suggest intent
            Intent i = new Intent(this, Suggest.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * Search all menu Options functionality. MenuOptions class with all menuoptions
     * not caring which modules or submodules are clicked.
     */
    private void search_from_action_bar(){
        Intent i = new Intent(SubModules.this, MenuOptions.class);
        i.putExtra("sub_module_no", "fetch_all");
        startActivity(i);
    }
}
