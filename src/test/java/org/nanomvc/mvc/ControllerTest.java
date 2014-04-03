/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc.mvc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author edmundas
 */
public class ControllerTest {
    
    private int[] arr;
    private String json;
    
    @Before
    public void prepareTes() {
        arr = new int[]{ 0 };
        json = "[0]";
    }
    
    @Test
    public void testToJson() {
        Assert.assertEquals(Controller.toJson(arr), json);
    }
    
    @Test
    public void testFromJson() {
        Assert.assertArrayEquals(Controller.fromJson(json, arr.getClass()), arr);
    }
    
}
