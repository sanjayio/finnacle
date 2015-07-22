package hpcoe.com.menuhelpdesk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;

import hpcoe.com.menuhelpdesk.UpdateService;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by: Abhijith Gururaj and Sanjay Kumar
 *
 * This is a Broadcast Receiver which gets fired whenever the AlarmManager
 * invokes the intent.
 * When this receiver is invoked, the service which checks for updates
 * in the server is started
 *
 * @see hpcoe.com.menuhelpdesk.Modules where the alarm for this receiver
 * is called in appication context.
 *
 * @see hpcoe.com.menuhelpdesk.receivers.UpdateBootReceiver where the alarm
 * for this receiver is called at boot time.
 */
public class UpdateAlarmReceiver extends BroadcastReceiver {

    /**
     * This method gets automatically invoked when the Alarm Manager invokes the alarm
     * for this intent.
     * Override onReceive to implement our code.
     *
     * @param context : The context in which the receiver is running
     * @param intent : The intent being received.
     *               This can be used to pass data from a context to this receiver.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHandler db=new DatabaseHandler(context);
        db.addLog("\nUpdateAlarmreceiver: Starting Service");
        Log.d("UpdateAlarm", "Starting service");
        Intent i;
        i = new Intent(context,UpdateService.class);
        context.startService(i);
        Log.d("UpdateAlarm","Started service");
        db.addLog("\nUpdateAlarmReceiver: Started Service.");
    }
}
