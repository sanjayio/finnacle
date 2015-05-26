package hpcoe.com.menuhelpdesk;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by sanjay on 26/5/15.
 */
public class Modules_Activity extends ActionBarActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        ListView listView1 = (ListView) findViewById(R.id.listView1);

        String[] items = { "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        listView1.setAdapter(adapter);
    }

}
