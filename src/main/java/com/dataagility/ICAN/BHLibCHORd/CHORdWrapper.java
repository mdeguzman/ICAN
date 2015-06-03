/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dataagility.ICAN.BHLibCHORd;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author bugsy
 */
public class CHORdWrapper {
    private Connection conn;

    public CHORdWrapper() throws ClassNotFoundException, SQLException{
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        Properties prop = new Properties();

    	try {
     		String filename = "C:/ICANConfig/chordConfig.properties";
    		//load a properties file from class path, inside static method
    		prop.load(new FileInputStream(filename));
                //get the property value and print it out

    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
        String server = prop.getProperty("databaseServer");
        String dbName = prop.getProperty("databaseName");
        String dbUser = prop.getProperty("databaseUser");
        String dbPass = prop.getProperty("databasePassword");
        conn = java.sql.DriverManager.getConnection("jdbc:jtds:sqlserver://"+server+":1433/"+dbName, dbUser, dbPass);
    }

    public Connection getConnection(){
        return conn;
    }

}
