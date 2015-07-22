package hpcoe.com.menuhelpdesk.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import java.sql.Time;

import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by: Abhijith Gururaj and Sanjay Kumar.
 * invoked at boot time.
 * If a device reboots, all the alarms in it get cleared. Thus, we need a receiver
 * at boot time to set the alarm again so as to implement UpdateAlarmReceiver.
 *
 * @see UpdateAlarmReceiver
 */
public class UpdateBootReceiver extends BroadcastReceiver {

    /**
     * This method is automatically invoked when the broadcast receiver is receiving
     * an intent.
     * Here, we use this method to set an alarm for the Alarm Receiver.
     *
     * @param context : The context in which the intent is running
     * @param intent : The intent being received. This can be used to pass data.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

            DatabaseHandler db=new DatabaseHandler(context);
            db.addLog("\nUpdateBootReceiver: Starting Alarm");
            Log.d("UpdateBoot","Starting Alarm");
            Intent alarmIntent=new Intent(context,UpdateAlarmReceiver.class);
            PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,alarmIntent,0);
            AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int interval=30000;
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,SystemClock.elapsedRealtime()+interval,interval,pendingIntent);
            Log.d("UpdateBoot","Started Alarm");
            db.addLog("\nUpdateBootReceiver: Started Alarm.");
    }
}
