package hpcoe.com.menuhelpdesk.utils;

import android.util.Log;

/**
 * Created by Abhijith Gururaj and Sanjay kumar.
 * This is used to handle uncaught exception in the Main Thread.
 * This will prevent the application from displaying ANR for unknown issues.
 */
public class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * Overridden method to catch unhandled Exception.
     * @param thread : The thread where the exception is thrown.
     * @param ex:
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d("Error: ", "Received exception: " + ex.getMessage() + " from thread: " + thread.getName(), ex);
    }
}
