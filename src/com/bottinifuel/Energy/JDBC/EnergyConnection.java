/*
 * Created on Sep 16, 2004 by pladd
 *
 */
package com.bottinifuel.Energy.JDBC;

import java.sql.*;
import com.sybase.jdbcx.SybDriver;


/**
 * @author pladd
 *
 */
public class EnergyConnection {
	private Connection EnergyCon;
	private final String Host;
	private final int    Port;
	private final String DB;
	private final String User;
	private final String Password;

	static public EnergyConnection BottiniAR1() throws Exception
    {
	    return new EnergyConnection("energy.bottini.com", 5000, "AR1", "botar1", "b0tt1n1");
	    //return new EnergyConnection("energy.bottinifuel.com", 5000, "AR1", "botar1", "b0tt1n1");
    }

	private void SetupConnection() throws Exception
	{
	    if (Host.length() == 0 || Port == 0 || DB.length() == 0 || User.length() == 0)
	    	throw new Exception("Missing database login credentials");
	    
        try
        {
            EnergyCon = DriverManager.getConnection("jdbc:sybase:Tds:"+Host+":"+Port+"/"+DB,
                                                    User, Password);
        }
        catch (SQLException e)
        {
            System.out.println(e);
            throw e;
        }
	}
	
	public EnergyConnection(String host, int port, String db, String user, String pass) throws Exception
	{
	    Host = host;
	    Port = port;
	    DB   = db;
	    User = user;
	    Password = pass;
	    
        try
        {
            SybDriver sybDriver = 
                (SybDriver)Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
            sybDriver.setVersion(com.sybase.jdbcx.SybDriver.VERSION_6);
            DriverManager.registerDriver(sybDriver);
        }
        catch (Exception e)
        {
            System.out.println(e);
            throw new Exception("Unable to load Sybase JDBC Driver");
        }
        
        SetupConnection();
	}
	
	public Statement getStatement() throws Exception
	{
	    if (EnergyCon.isClosed())
	        SetupConnection(); 
		return EnergyCon.createStatement();
	}
	

	public static void main(String[] args)
	{
		try
		{
			@SuppressWarnings("unused")
			EnergyConnection ec = new EnergyConnection("energy.bottini.com", 5000, "AR1", "botar1", "b0tt1n1");
		}
		catch (Exception e)
		{
			System.out.println("e2="+e);
		}
	}
}
