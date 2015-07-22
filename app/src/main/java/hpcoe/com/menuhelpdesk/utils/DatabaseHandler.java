package hpcoe.com.menuhelpdesk.utils;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 *
 * This is a helper class which is used to store the data by creating a local database
 * in the device.
 * CRUD operations can be effectively performed using the inbuilt Libraries provided.
 *
 * The name of the database is menuhelpdesk_local.
 * The tables used are:
 * 1. user - For storing user credentials.
 * 2. menu_options - For storing all the data related to Menu Options.
 * 3. logcat - For storing the application logs.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLClientInfoException;
import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int DB_VERSION = 24;
    public static final String DB_NAME = "menuhelpdesk_local";
    public static final String TABLE_USER = "user";
    public static final String TABLE_MENU_OPTIONS = "menu_options";
    //User table details.
    public static final String ID = "ID";
    public static final String EMAIL = "email";
    public static final String USERNAME = "uname";
    public static final String BANK = "bank";
    public static final String PASSWORD = "password";
    //menu_options table details
    public static final String MODULE_NAME = "module_name";
    public static final String SUB_MODULE_NAME = "sub_module_name";
    public static final String MENU_OPTION = "menu_option";
    public static final String SHORT_DESC = "short_desc";
    public static final String LONG_DESC = "long_desc";
    public static final String DB_VER = "db_ver";
    public static final String DESC = "desc";
    public static final String LOG_CAT_TABLE = "logcat";
    public static final String DATA_STATE = "data_state";

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Create the user table and menu_option table.
     * @param db
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FETCH_TABLE = "CREATE TABLE " + TABLE_MENU_OPTIONS + "("
                + MODULE_NAME + " TEXT,"
                + SUB_MODULE_NAME + " TEXT,"
                + MENU_OPTION + " TEXT,"
                + SHORT_DESC + " TEXT,"
                + LONG_DESC + " TEXT,"
                + DATA_STATE + " TEXT,"
                + DB_VER + " TEXT"
                + ")";
        db.execSQL(CREATE_FETCH_TABLE);

        Log.d("table moptions", CREATE_FETCH_TABLE);

        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + ID + " TEXT PRIMARY KEY,"
                + EMAIL + " TEXT UNIQUE,"
                + USERNAME + " TEXT,"
                + BANK + " TEXT,"
                + PASSWORD + " TEXT" + ")";
        System.out.println(CREATE_LOGIN_TABLE);
        Log.d("DB", CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_LOGCAT_TABLE = "CREATE TABLE " + LOG_CAT_TABLE + "("
                + ID + " INTEGER PRIMARY KEY, "
                + DESC + " TEXT" + ")";
        Log.d("DB", CREATE_LOGCAT_TABLE);
        db.execSQL(CREATE_LOGCAT_TABLE);
        String INSERT_INIT = "INSERT INTO logcat VALUES(1,'')";
        db.execSQL(INSERT_INIT);
        INSERT_INIT="INSERT into logcat VALUES(2,'')";
        db.execSQL(INSERT_INIT);
        Log.d("DB", "Initial Insert");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existing.
        Log.d("DB", "upgrade");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU_OPTIONS);
        // Create tables again
        onCreate(db);
    }

    public void addUser(String email, String uname, String id, String password, String bank) {
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println(email + uname + id + password + bank);
        ContentValues values = new ContentValues();
        values.put(ID, id); // Email
        values.put(EMAIL, email); // UserName
        values.put(USERNAME, uname); // ID
        values.put(BANK, bank); // Password
        values.put(PASSWORD, password); // Bank
        // Inserting Row
        db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
    }

    public void addLog(String log) {
        Log.d("logcat", log);
        String UPDATE_TABLE = "UPDATE " + LOG_CAT_TABLE + " SET " + DESC + " = " + DESC + " || '" + log + "' WHERE ID = 1";
        Log.d("logcat", UPDATE_TABLE);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(UPDATE_TABLE);
//        String logs = null;
//        SQLiteDatabase db2 = this.getReadableDatabase();
//        String selectQuery = "SELECT " + DESC + " FROM " + LOG_CAT_TABLE + " WHERE ID = 1 ";
//        Log.d("logcat", selectQuery);
//        Cursor cursor = db2.rawQuery(selectQuery, null);
//        cursor.moveToFirst();
//        if(cursor.getCount() > 0) {
//            logs = cursor.getString(cursor.getColumnIndex(DESC));
//            Log.d("logcat",logs);
//        }
        db.close();
        //db2.close();
    }

    public void copyLog() {
        SQLiteDatabase db = this.getWritableDatabase();
        String copyQuery="UPDATE logcat set desc = (select desc from logcat where ID=1) where ID=2";
        db.execSQL(copyQuery);
        Log.d("logcat","copied contents");
        db.close();
    }

    public String getLogs() {
        String log = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + DESC + " AS desc FROM " + LOG_CAT_TABLE + " WHERE ID = 2 ";
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.moveToFirst()) {
            //Log.d("logcat","cursor not empty");
            log = cursor.getString(cursor.getColumnIndex(DESC));
            Log.d("logcat","data from db: "+log);
        }
        //Log.d("logcat", "cursor empty");
        cursor.close();
        db.close();
        return log;
    }


    public void addMenuOption(String moduleName, String subModuleName, String menuOption, String shortDescription, String longDescription, String data_state, String db_ver) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MODULE_NAME, moduleName); // FILL IN ALL THE DATA
        values.put(SUB_MODULE_NAME, subModuleName);
        values.put(MENU_OPTION, menuOption);
        values.put(SHORT_DESC, shortDescription);
        values.put(LONG_DESC, longDescription);
        values.put(DATA_STATE, data_state);
        values.put(DB_VER, db_ver);
        // Inserting Row
        db.insert(TABLE_MENU_OPTIONS, null, values);
        db.close(); // Closing database connection
    }

    public String getMaxDbVersion() {
        String max = "";
        String selectQuery = "SELECT MAX(DB_VER) AS MAX FROM " + TABLE_MENU_OPTIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            max = cursor.getString(cursor.getColumnIndex("MAX"));
        }
        cursor.close();
        db.close();
        // return user
        return max;

    }

    public void updateMenuOption(String moduleName, String subModuleName, String menuOption, String shortDescription, String longDescription, String data_state, String db_ver) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MODULE_NAME, moduleName); // FILL IN ALL THE DATA
        values.put(SUB_MODULE_NAME, subModuleName);
        values.put(MENU_OPTION, menuOption);
        values.put(SHORT_DESC, shortDescription);
        values.put(LONG_DESC, longDescription);
        values.put(DATA_STATE, data_state);
        values.put(DB_VER, db_ver);
        // Inserting Row
        String where = "menu_option=?";
        String whereArgs[] = new String[] {menuOption};
        db.update(TABLE_MENU_OPTIONS, values, where, whereArgs);
        db.close(); // Closing database connection
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("email", cursor.getString(1));
            user.put("username", cursor.getString(2));
            user.put("id", cursor.getString(3));
            user.put("password", cursor.getString(4));
            user.put("bank", cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    public String getUserID(){
        String user = new String();
        String selectQuery = "SELECT " + ID + " FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user = cursor.getString(cursor.getColumnIndexOrThrow(ID));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }



    public HashMap<String, String> getMenuOptionDetails(String mOption){
        HashMap<String,String> fetch = new HashMap<String,String>();
        String selectQuery = "SELECT  * FROM " + TABLE_MENU_OPTIONS + " where " + MENU_OPTION + " = " + mOption;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.getCount() > 0){
            fetch.put(MODULE_NAME, cursor.getString(1));
            fetch.put(SUB_MODULE_NAME, cursor.getString(2));
            fetch.put(MENU_OPTION, cursor.getString(3));
            fetch.put(SHORT_DESC, cursor.getString(4));
            fetch.put(LONG_DESC, cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return fetch
        return fetch;
    }

    public HashMap<String, String> getModuleNames() {
        HashMap<String, String> fetch = new HashMap<>();
        String selectQuery = "SELECT DISTINCT " + MODULE_NAME + " FROM " + TABLE_MENU_OPTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.d("gs", cursor.getString(cursor.getColumnIndex(MODULE_NAME)));
        int counter = 0;
        while(!cursor.isAfterLast()) {
            Log.d("gs", cursor.getString(cursor.getColumnIndex(MODULE_NAME)));
            fetch.put(String.valueOf(counter++), cursor.getString(cursor.getColumnIndex(MODULE_NAME)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return fetch;
    }

    public HashMap<String, String> getSubmodulesNames(String type) {
        HashMap<String, String> fetch = new HashMap<>();
        String selectQuery = "SELECT DISTINCT " + SUB_MODULE_NAME + " FROM " + TABLE_MENU_OPTIONS +
                " WHERE " + MODULE_NAME + " = '" + type+"'";
        Log.d("query: ", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.d("gs", cursor.getString(cursor.getColumnIndex(SUB_MODULE_NAME)));
        int counter = 0;
        while(!cursor.isAfterLast()) {
            Log.d("gs", cursor.getString(cursor.getColumnIndex(SUB_MODULE_NAME)));
            fetch.put(String.valueOf(counter++), cursor.getString(cursor.getColumnIndex(SUB_MODULE_NAME)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return fetch;
    }

    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public int getMenuOptionsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MENU_OPTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public void resetUserTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        //db.delete(LOG_CAT_TABLE, null, null);
        //db.delete(TABLE_MENU_OPTIONS, null, null);
        db.close();
    }

    public void resetLogTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String UPDATE_TABLE = "UPDATE " + LOG_CAT_TABLE + " SET " + DESC + " = '' WHERE ID = 1";
        Log.d("logcat", UPDATE_TABLE);
        db.execSQL(UPDATE_TABLE);
        //db.delete(TABLE_MENU_OPTIONS, null, null);
        db.close();
    }

    public void resetMenuOptionsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        //db.delete(TABLE_USER, null, null);
        //db.delete(LOG_CAT_TABLE, null, null);
        db.delete(TABLE_MENU_OPTIONS, null, null);
        db.close();
    }

    public void printUserTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            Log.d("user_id: ", cursor.getString(1));
        }
        db.close();
        cursor.close();
    }

    public HashMap<String, String> getMenuOptions(String type) {
        HashMap<String, String> fetch = new HashMap<>();
        String selectQuery;
        if(type.equalsIgnoreCase("fetch_all")) {
            selectQuery = "SELECT DISTINCT " + MENU_OPTION + ", " + SHORT_DESC + " FROM " + TABLE_MENU_OPTIONS;
        } else {
            selectQuery = "SELECT DISTINCT " + MENU_OPTION + ", " + SHORT_DESC + " FROM " + TABLE_MENU_OPTIONS +
                    " WHERE " + SUB_MODULE_NAME + " = '" + type+"'";
        }
        Log.d("query: ",selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.d("gs", cursor.getString(cursor.getColumnIndex(MENU_OPTION)));
        int counter = 0;
        while(!cursor.isAfterLast()) {
            Log.d("gs", cursor.getString(cursor.getColumnIndex(MENU_OPTION))+"$$$"+cursor.getString(cursor.getColumnIndex(SHORT_DESC)));
            fetch.put(String.valueOf(counter++), cursor.getString(cursor.getColumnIndex(MENU_OPTION)) + "$$$" + cursor.getString(cursor.getColumnIndex(SHORT_DESC)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        Log.d("DB", "Returning hashmap");
        return fetch;
    }

    public HashMap<String, String> getShortAndLong(String menuOptions) {
        HashMap<String, String> fetch = new HashMap<>();
        String selectQuery = "SELECT " + SHORT_DESC + ", " + LONG_DESC + " FROM " + TABLE_MENU_OPTIONS + " WHERE " + MENU_OPTION +
                " = '" + menuOptions + "'";
        Log.d("query: ",selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.d("getShortAndLong", cursor.getString(cursor.getColumnIndex(SHORT_DESC)));
        Log.d("getShortAndLong", cursor.getString(cursor.getColumnIndex(LONG_DESC)));
        int counter = 0;
        while(!cursor.isAfterLast()) {
            fetch.put(String.valueOf(counter++), cursor.getString(cursor.getColumnIndex(SHORT_DESC)) + "$$$" + cursor.getString(cursor.getColumnIndex(LONG_DESC)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        Log.d("getShortAndLong","finish fetching");
        return fetch;
    }

}
