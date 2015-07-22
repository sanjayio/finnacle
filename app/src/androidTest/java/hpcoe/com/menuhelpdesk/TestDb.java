package hpcoe.com.menuhelpdesk;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import hpcoe.com.menuhelpdesk.utils.DatabaseHandler;

/**
 * Created by Messi10 on 09-Jun-15.
 */
public class TestDb extends AndroidTestCase {

    private final String LOG_TAG=TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable{
        mContext.deleteDatabase(DatabaseHandler.DB_NAME);
        SQLiteDatabase db=new DatabaseHandler(this.mContext).getWritableDatabase();

        assertEquals(true,db.isOpen());
        db.close();
    }

    public void testInsertDb(){
        String moduleName="Test_Module";
        String subModuleName="Test_SubModule";
        String menuOption="Test_MenuOption";
        String shortDesc="Test_ShortDescription";
        String longDesc="Test_longDescription";

        DatabaseHandler dh=new DatabaseHandler(this.mContext);
        SQLiteDatabase db=dh.getWritableDatabase();

        ContentValues contentValues=new ContentValues();
        contentValues.put(DatabaseHandler.MODULE_NAME,moduleName);
        contentValues.put(DatabaseHandler.SUB_MODULE_NAME,subModuleName);
        contentValues.put(DatabaseHandler.MENU_OPTION,menuOption);
        contentValues.put(DatabaseHandler.SHORT_DESC,shortDesc);
        contentValues.put(DatabaseHandler.LONG_DESC,longDesc);

        long rowId=db.insert(DatabaseHandler.TABLE_MENU_OPTIONS,null,contentValues);
        assertTrue(rowId!=-1);
        Log.d(LOG_TAG,"New row id: "+rowId);

        String columns[]={DatabaseHandler.MODULE_NAME,
                DatabaseHandler.SUB_MODULE_NAME,
                DatabaseHandler.MENU_OPTION,
                DatabaseHandler.SHORT_DESC,
                DatabaseHandler.LONG_DESC};

        Cursor cursor=db.query(
                DatabaseHandler.TABLE_MENU_OPTIONS,
                columns,
                null,null,null,null,null);

        if(cursor.moveToFirst()){

            String module_name=cursor.getString(cursor.getColumnIndex(DatabaseHandler.MODULE_NAME));
            String sub_moduleName=cursor.getString(cursor.getColumnIndex(DatabaseHandler.SUB_MODULE_NAME));
            String menu_option=cursor.getString(cursor.getColumnIndex(DatabaseHandler.MENU_OPTION));
            String short_desc=cursor.getString(cursor.getColumnIndex(DatabaseHandler.SHORT_DESC));
            String long_desc=cursor.getString(cursor.getColumnIndex(DatabaseHandler.LONG_DESC));

            assertEquals(moduleName,module_name);
            assertEquals(subModuleName,sub_moduleName);
            assertEquals(menuOption,menu_option);
            assertEquals(shortDesc,short_desc);
            assertEquals(longDesc,long_desc);
            //Test Successful
        }else{
            //Test failed
            fail("No values returned");
        }
    }
}
