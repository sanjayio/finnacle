package hpcoe.com.menuhelpdesk;

import android.test.AndroidTestCase;
import android.util.Log;

import hpcoe.com.menuhelpdesk.utils.Validators;

/**
 * Created by Messi10 on 09-Jun-15.
 */
public class ValidationTest extends AndroidTestCase {
    private final String LOG_TAG=ValidationTest.class.getSimpleName();

    public void testValidation() throws Throwable{
        String userNames[]={"Abhijith","Sanjay","Abhijith Gururaj","Sanjay Kumar",
                "12234243","This is test","!@$@@#@","    ","Abhijith(*&*","Abhi123","sanjay_1234"};

        Boolean[] validUserNames={true,true,false,false,true,false,false,false,false,true,false};

        String emailIds[]={"abhijith@test.com","sanjay@test.com","abhijith@","sanjay.com",
        "name"," ","abhijith@gmail","test@test.com","test@test.in","test@test.org",
        ".org",".in",""};

        Boolean validEmailIds[]={true,true,false,false,false,false,false,true,true,true,false,false,false};

        Validators validators=new Validators(mContext);

        Boolean arevalidUserNames[]=new Boolean[userNames.length];
        int i=0;
        for(String username: userNames){
            arevalidUserNames[i++]=validators.isValidName(username);
        }

        i=0;
        Boolean arevalidEmailIds[]=new Boolean[emailIds.length];
        for(String emailId: emailIds){
            arevalidEmailIds[i++]=validators.isValidEmailAddress(emailId);
        }

        for(i=0;i<arevalidUserNames.length;i++){
            Log.d(LOG_TAG,"Checking:Usernames "+(i)+" "+arevalidUserNames[i]+" : "+validUserNames[i]);
            assertTrue(arevalidUserNames[i]==validUserNames[i]);

        }

        for(i=0;i<arevalidEmailIds.length;i++){
            Log.d(LOG_TAG,"Checking:emailids "+i+" "+arevalidEmailIds[i]+" : "+validEmailIds[i]);
            assertTrue(arevalidEmailIds[i]==validEmailIds[i]);

        }
            Log.d("Test","Finished");
    }
}
