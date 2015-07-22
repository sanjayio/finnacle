package hpcoe.com.menuhelpdesk;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hpcoe.com.menuhelpdesk.RecyclerUtils.RecyclerAdapter;
import hpcoe.com.menuhelpdesk.RecyclerUtils.RecyclerModel;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This activity will load all the modules of a particular submodule clicked by the user.
 * These SubModules are loaded in SubModules Activity.
 * A RecyclerView is used to display all the MenuOptions as this is a more efficient
 * version of a list view, especially when there are a lot of items to be displayed.
 *
 * @see hpcoe.com.menuhelpdesk.SubModules to view the SubModules whcih the user can click.
 * @see DatabaseHandler for more details on fetching data from the database.
 * @see RecyclerAdapter for viewing the functionality of RecyclerView.
 */
public class MenuOptions extends ActionBarActivity implements SearchView.OnQueryTextListener {

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private List<RecyclerModel> mModels;
    String type;
    Toolbar toolbar;
    LinearLayoutManager linearLayoutManager;
    SharedPreferences storeMenus, settings;
    DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_options);


         /* Get the submodule number from the intent arguments.
         */
        type = getIntent().getStringExtra("sub_module_no");
        storeMenus=getSharedPreferences("userClicked", Context.MODE_PRIVATE);
        settings = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
         if(type==null)
             type=storeMenus.getString("latest_subModule_clicked","Commercial Loans");
        else
         {
             SharedPreferences.Editor editor = storeMenus.edit();
             editor.putString("latest_subModule_clicked",type);
             editor.apply();
         }

        db.addLog("\nMenuOptions: Received intent extra from Submodules: " + type);

        //Initialize and setup the toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if(type.equalsIgnoreCase("fetch_all")) {
            getSupportActionBar().setTitle("All Menu Options");
        } else {
            getSupportActionBar().setTitle(type);
        }


        //Add the data fetched from the Database to a List.
        mModels=new ArrayList<>();

        HashMap<String, String> f = db.getMenuOptions(type);
        Log.d("Menu Options", "Recieved Hashmap");
        Set s = f.entrySet();
        for (Object value : s) {
            Map.Entry me = (Map.Entry) value;
            String temp = (String) me.getValue();
            String[] items = temp.split("\\$\\$\\$");
            Log.d("Options Short desc:", Arrays.toString(items));
            mModels.add(new RecyclerModel(items[0],items[1]));
            //shortDescStringList.add(items[1]);
        }

        /**
         * Setup the recycler view, RecyclerAdapter.
         * Set the itemClickListener to start an activity when the user clicks on the view
         * Also send the corresponding data in an intent.
         */
        linearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerAdapter(this, mModels);
        mRecyclerView.setAdapter(mAdapter);

        db.addLog("\nMenuOptions: Added menu options from database to the recycler view.");

        mAdapter.setItemClickListener(new RecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView tv= (TextView) view.findViewById(R.id.txt_listitem);
                mAdapter.notifyItemChanged(position);
                Intent intent=new Intent(MenuOptions.this,Description.class);
                intent.putExtra("menu_option",tv.getText().toString());
                Log.d("Menu option",tv.getText().toString());
                startActivity(intent);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_menu_options,menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * OptionsMenuItemSelected listener. We have 2 options here.
     * They are suggest, logout.
     * @param item : Item selected in the menu
     * @return : true if successful , else false.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
                    Intent i = new Intent(MenuOptions.this, Settings.class);
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

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    /**
     * Implementing the search functionality based on the user's input.
     * (Removal or addition of List items)
     *
     * @param query: Sequence of characted entered by the user
     * @return : Always return true.(if returned false, in listener will stop)
     */
    @Override
    public boolean onQueryTextChange(String query) {
        final List<RecyclerModel> filteredModelList = filter(mModels, query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    /**
     * Filtering the data.
     * @param models : original List containing all the Items
     * @param query : String sent from onQueryTextChange
     * @return : The filtered List.
     */
    private List<RecyclerModel> filter(List<RecyclerModel> models, String query) {
        query = query.toLowerCase();

        final List<RecyclerModel> filteredModelList = new ArrayList<>();
        for (RecyclerModel model : models) {
            final String text = model.getText().toLowerCase()+model.getShortDesc().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
