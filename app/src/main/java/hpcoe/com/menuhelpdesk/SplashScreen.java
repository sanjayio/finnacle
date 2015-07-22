package hpcoe.com.menuhelpdesk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 *
 * This activity displays a splash screen for a definite period of time.
 */
public class SplashScreen extends Activity{
    //set the timeout interval of the splash screen here.
    private static int time_out = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final DatabaseHandler db = new DatabaseHandler(this);

        db.copyLog();
        db.resetLogTable();

        Log.d("Splash", "Loading splash");
        db.addLog("\nSplash: Loading splash started.");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Splash", "Load Complete");
                db.addLog("\nSplash: Loading splash completed.");
                Intent i = new Intent(SplashScreen.this, SignIn.class);
                startActivity(i);
                finish();
            }
        }, time_out);

    }
}
