package org.nanomvc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nanomvc.mvc.ControllerTest;
import org.nanomvc.mvc.ResultTest;

/**
 *
 * @author edmundas
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ControllerTest.class,
    ResultTest.class
})
public class FrameworkTestSuite {
    
}
