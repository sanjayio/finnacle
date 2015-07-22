package hpcoe.com.menuhelpdesk;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by Messi10 on 09-Jun-15.
 */
public class FullTest extends TestSuite {
    public static Test suite(){
        return new TestSuiteBuilder(FullTest.class)
                .includeAllPackagesUnderHere().build();
    }

    public FullTest(){
        super();
    }
}
