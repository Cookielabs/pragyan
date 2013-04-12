/*
 * Main.java
 *
 * Created on July 11, 2012, 2:24 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package project2;
import java.io.*;
import java_cup.runtime.*;

/* This is where write the recursive decent parser. */

public class Main {

    /** Creates a new instance of Main */


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        
        FileReader f=new FileReader(args[0]);
        Yylex yy=new Yylex(f);
        Symbol o=yy.next_token();
        while(o!=null)
            yy.next_token();
        }

    }
