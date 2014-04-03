/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc.mvc;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author edmundas
 */
public class ResultTest {
    
    @Test
    public void testController() {
        Result result = new Result(Result.SC_404_NOT_FOUND);
        Assert.assertEquals(result.getStatusCode(), Result.SC_404_NOT_FOUND);
    }
}
