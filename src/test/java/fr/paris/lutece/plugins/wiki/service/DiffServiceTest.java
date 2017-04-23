/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.paris.lutece.plugins.wiki.service;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pierre
 */
public class DiffServiceTest
{
    /**
     * Test of getDiff method, of class DiffService.
     */
    @Test
    public void testGetDiff( )
    {
        System.out.println( "getDiff" );

        String strOld = "<h1>Hello, World !</h1>";
        String strNew = "<h1>Hello, It works !</h1>";
        String expResult = "";
        String result = DiffService.getDiff( strOld, strNew );
        System.out.println( result );

        // assertEquals(expResult, result);
    }
}
