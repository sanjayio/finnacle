package hpcoe.com.menuhelpdesk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Modules_Activity extends ActionBarActivity {

    private ListView searchFilter;
    private EditText searchEdit;
    private ArrayList<String> stringList;
    private ValueAdapter valueAdapter;
    private TextWatcher searchtw;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);
        settings=getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        initList();
        initUI();

        valueAdapter = new ValueAdapter(stringList, this);
        searchFilter.setAdapter(valueAdapter);
        searchEdit.addTextChangedListener(searchtw);
    }

    private void initList() {
        stringList = new ArrayList<String>();
        stringList.add("one");
        stringList.add("two");
        stringList.add("three");
        stringList.add("four");
        stringList.add("five");
        stringList.add("six");
        stringList.add("seven");
        stringList.add("eight");
        stringList.add("nine");
        stringList.add("ten");

        searchtw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valueAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valueAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                valueAdapter.getFilter().filter(s);
            }
        };
    }

    private void initUI() {
        searchFilter = (ListView) findViewById(R.id.modules_list);
        searchEdit = (EditText) findViewById(R.id.searchedt);
    }


}
