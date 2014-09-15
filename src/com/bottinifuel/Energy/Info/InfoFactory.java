/*
 * Created on Oct 4, 2004 by pladd
 *
 */
package com.bottinifuel.Energy.Info;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.bottinifuel.Energy.JDBC.EnergyConnection;


/**
 * @author pladd
 *
 */

public class InfoFactory {
    private EnergyConnection EnergyConn = null;
    @SuppressWarnings("unused")
	private EnergyConnection PegasusConn = null;

    public static final char OACprefix = 'B';
    
    public InfoFactory(String host, int port, String db, String user, String pass) throws Exception
    {
        EnergyConn = new EnergyConnection(host, port, db, user, pass);
    }
    
    public InfoFactory() throws Exception
	{
        String energyHost = null;
        int    energyPort = 0;
        String energyDB = null;
        String energyLogin = null;
        String energyPassword = null;
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            URL mfURL = cl.getResource("EnergyLoginInfo.manifest");
            Manifest manifest = new Manifest(mfURL.openStream());
            Attributes attr = manifest.getMainAttributes();
            energyHost     = attr.getValue("EnergyHost");
            energyPort     = Integer.valueOf(attr.getValue("EnergyPort"));
            energyDB       = attr.getValue("EnergyDB");
            energyLogin    = attr.getValue("EnergyUser");
            energyPassword = attr.getValue("EnergyPW");
            
            EnergyConn = new EnergyConnection(energyHost, energyPort, energyDB, energyLogin, energyPassword);
        }
        catch (IOException e)
        {
            throw new Exception("Error opening Energy connection: " + e);
        }
	}

    /** Connect to the associated Pegasus database
     * 
     * @param host
     * @param port
     * @param db
     * @param user
     * @param pass
     * @throws Exception
     * 
     * Needed if technician status info is being retrieved
     */
    public void PegasusConnect(String host, int port, String db, String user, String pass) throws Exception
    {
        PegasusConn = new EnergyConnection(host, port, db, user, pass);
    }
    
    /** Connect to the associated Pegasus database
     * 
     * Needed if technician status info is being retrieved
     * 
     * Looks up connection parameters from manifest file
     */
    public void PegasusConnect() throws Exception
    {
        String pegasusHost = null;
        int    pegasusPort = 0;
        String pegasusDB = null;
        String pegasusLogin = null;
        String pegasusPassword = null;
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            URL mfURL = cl.getResource("PegasusLoginInfo.manifest");
            Manifest manifest = new Manifest(mfURL.openStream());
            Attributes attr = manifest.getMainAttributes();
            pegasusHost     = attr.getValue("PegasusHost");
            pegasusPort     = Integer.valueOf(attr.getValue("PegasusPort"));
            pegasusDB       = attr.getValue("PegasusDB");
            pegasusLogin    = attr.getValue("PegasusUser");
            pegasusPassword = attr.getValue("PegasusPW");
            
            PegasusConn = new EnergyConnection(pegasusHost, pegasusPort, pegasusDB, pegasusLogin, pegasusPassword);
        }
        catch (IOException e)
        {
            throw new Exception("Error opening Pegasus connection: " + e);
        }
    }
    
    /** Get the internal energy account number from the full account number with the check digit
     *
     * @param full_account
     * @return
     * @throws Exception
     */
    public int AccountNum(int full_account) throws Exception
    {
        Statement s = getStatement();
        
        // Format the account number properly
        String full = Integer.toString(full_account);
        if (full.length() > 10) throw new Exception("Account #" + full + " is too long");
        else if (full.length() < 10)
        {
            full = full + "         ";
            full = full.substring(0,10);
        }

        // Find the account
        ResultSet r = s.executeQuery("SELECT account_num " +
                                     " FROM dbo.FULL_ACCOUNT " +
                                     " WHERE full_account = '" + full +"'");
        if (!r.next())
        {
            throw new Exception("Account #" + full_account + " not found");
        }
        else
        {
            int rc = r.getInt("account_num");
            if (r.next()) throw new Exception("Error: whoa! more than one account matching " + full);
            else return rc;
        }
    }

    
    /** Lookup the full account number for an internal number
     * 
     * @param shortAcct
     * @return
     */
    public int InternalToFullAccount(int shortAcct) throws Exception
    {
        Statement s = getStatement();
        
        ResultSet r = s.executeQuery("SELECT full_account " +
                                     " FROM dbo.FULL_ACCOUNT " +
                                     " WHERE account_num = " + shortAcct);
        if (!r.next())
            throw new Exception("Short account #" + shortAcct + " not found");
        else
        {
            int rc = r.getInt("full_account");
            if (r.next())
                throw new Exception("Error: whoa! more than one account matching " + shortAcct);
            else return rc;
        }
    }
    
    
    /** Lookup an internal account number using an "old" account number
     * 
     * @param oacstring [O]ld [AC]count number - stored in general text as OAC: xxxxxxx
     * @return
     */
    public int OACLookup(String oacstring) throws Exception
    {
        if (Character.toUpperCase(oacstring.charAt(0)) != OACprefix)
            throw new Exception("Invalid account number prefix");

        Statement s = getStatement();
        // Find the account
        ResultSet r = s.executeQuery("SELECT gtx_text_owner " +
                                     " FROM dbo.GTX_TEXT " +
                                     " WHERE gtx_text LIKE '%OAC:" + oacstring.substring(1) +"'" +
                                     "    OR gtx_text LIKE '%OAC:" + oacstring.substring(1) +"/%'");
        if (!r.next())
        {
            throw new Exception("Account #" + oacstring + " not found");
        }
        else
        {
            int rc = r.getInt("gtx_text_owner");
            if (r.next()) throw new Exception("Error: whoa! more than one account matching " + oacstring);
            else return rc;
        }
    }
    
    
    public Vector<Integer> SortCodeLookup(String sortCode) throws Exception
    {
    	sortCode = sortCode.trim();
    	int len = (sortCode.length() > 6 ? 6 : sortCode.length());
        Statement s = EnergyConn.getStatement();
        ResultSet r;
        try
        {
            r = s.executeQuery("SELECT account_num " +
                               "FROM dbo.ACCOUNTS " +
                               "WHERE sort_code like '" + sortCode.substring(0, len).toUpperCase() + "%'");
            
            Vector<Integer> rc = null;
            while (r.next())
            {
            	if (rc == null) rc = new Vector<Integer>();
            	rc.add(new Integer(r.getInt("account_num")));
            }
            return rc;
        }
        catch (SQLException e)
        {
            throw e;
        }
    }
    
    /** Get customer info
     * 
     * @param acct_num Internal account number
     * @return
     * @throws Exception
     */
    public CustInfo GetCustomer(int acct_num) throws Exception
    {
        return new CustInfo(this, acct_num);
    }
    
    
    /** Get the billing address for an account
     * 
     * @param acct_num Internal account number
     * @return
     * @throws Exception
     */
    public AddressInfo GetBillingAddress(int acct_num) throws Exception
    {
        Statement s = EnergyConn.getStatement();
        ResultSet r;
        try
        {
            r = s.executeQuery("SELECT name, street1, street2, city, state, postal_code, telephone, " +
                               "       tank_count, service_count " +
                               "FROM dbo.ACCOUNTS " +
                               "WHERE account_num = " + acct_num);
            while (r.next())
            {
                AddressInfo billto = new AddressInfo(this, r, "Billing");
                return billto;
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        return null;
    }

    
    /** Gets all the addresses associated with an account (billing, delivery, and service)
     * 
     * @param acct_num Internal account number
     * @return
     * @throws Exception
     */
	public AddressInfo[] GetAllAddresses(int acct_num) throws Exception
	{
		Vector<AddressInfo> addrs = new Vector<AddressInfo>();

		Statement s = EnergyConn.getStatement();
		ResultSet r;
		try
		{
			r = s.executeQuery("SELECT name, street1, street2, city, state, postal_code, telephone, " +
					           "       tank_count, service_count " +
							   "FROM dbo.ACCOUNTS " +
							   "WHERE account_num = " + acct_num);
			while (r.next())
			{
				AddressInfo billto = new AddressInfo(this, r, "Billing");
				addrs.add(billto);
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		try
		{
			r = s.executeQuery("SELECT tank_seq_number, tank_num, product " +
					           "FROM dbo.TANKS " +
							   "WHERE account_num = " + acct_num);
			while (r.next())
			{
				int tank_seq = r.getInt("tank_seq_number");
				int tank_num = r.getInt("tank_num");
				int product  = r.getInt("product");

				try {
					AddressInfo tankInfo = new AddressInfo(this, tank_seq, tank_num, product);
					addrs.add(tankInfo);
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		
		try
		{
			r = s.executeQuery("SELECT service_seq_number, service_num " +
					           "FROM dbo.SERVICE " +
							   "WHERE account_num = " + acct_num);
			while (r.next())
			{
				int serv_seq = r.getInt("service_seq_number");
				int serv_num = r.getInt("service_num");

				try {
					AddressInfo servInfo = new AddressInfo(this, serv_seq, serv_num);
					addrs.add(servInfo);
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
			}
		}
		catch (Exception e)
		{
			throw e;
		}


		AddressInfo[] rc = new AddressInfo[addrs.size()];
		addrs.copyInto(rc);
		return rc;
	}

    public int[] GetMatchingFullAccounts(String matchString) throws Exception
    {
        try {
            Statement s = getStatement();
            
            String query = "SELECT full_account " +
                " FROM dbo.FULL_ACCOUNT INNER JOIN dbo.ACCOUNTS ON dbo.FULL_ACCOUNT.account_num = dbo.ACCOUNTS.account_num" +
                " WHERE full_account LIKE '" + matchString + "'";
            ResultSet r = s.executeQuery(query);

            Vector<Integer> rc = new Vector<Integer>();
            while (r.next())
            {
                rc.add(new Integer(r.getInt("full_account")));
            }
            
            int [] intRC = new int[rc.size()];
            int i = 0;
            for (Integer element : rc)
            {
                intRC[i++] = element;
            }
            return intRC;
        }
        catch (SQLException e)
        {
            return null;
        }
    }

    public List<Product> GetProducts(String mfg, String group, String stock_id, int postCode) throws Exception
    {
    	Statement s = getStatement();

    	String query =
    			"SELECT " +
    			  "PRODUCT.mfg_code, " + 
    			  "PRODUCT.group_code, " + 
    			  "PRODUCT.stock_id, " + 
    			  "PRODUCT.posting_code, " + 
    			  "PRODUCT.description " +
				"FROM PRODUCT ";

    	String qualifiers = null;
    	if (mfg != null || group != null || stock_id != null || postCode != 0)
    	{
    		if (mfg != null)
    		{
    			qualifiers = "WHERE ";
    			qualifiers += "PRODUCT.mfg_code = '" + mfg.trim() + "' ";
    		}
    		if (group != null)
    		{
    			if (qualifiers != null)
    				qualifiers += " AND ";
    			else
    				qualifiers = "WHERE ";

    			qualifiers += "PRODUCT.group_code = '" + group.trim() + "' ";
    		}
    		if (stock_id != null)
    		{
    			if (qualifiers != null)
    				qualifiers += " AND ";
    			else
    				qualifiers = "WHERE ";

    			qualifiers += "PRODUCT.stock_id = '" + stock_id.trim() + "' ";
    		}
    		if (postCode != 0)
    		{
    			if (qualifiers != null)
    				qualifiers += " AND ";
    			else
    				qualifiers = "WHERE ";

    			qualifiers += "PRODUCT.posting_code = '" + postCode + "' ";
    		}
    		query += qualifiers;
    	}
    	
    	ResultSet rs = s.executeQuery(query);
    	
    	List<Product> rc = new LinkedList<Product>();
    	while (rs.next())
    	{
    		Product p = new Product(
    				rs.getString("mfg_code").trim(), rs.getString("group_code").trim(),
    				rs.getString("stock_id").trim(), rs.getString("description").trim(), rs.getInt("posting_code"));
    		rc.add(p);
    	}
    	
    	if (rc.size() != 0)
    		return rc;
    	else
    		return null;
    }

    public ServicePart GetServicePartInfo(int partNum) throws Exception
    {
        return new ServicePart(this, partNum);
    }
    
    
    public Set<ServiceCall> CallsForDate(Date d)
    {
        Calendar start = Calendar.getInstance();
        Calendar end   = Calendar.getInstance();
        
        start.setTime(d);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        Timestamp sqlStart = new Timestamp(start.getTimeInMillis());
        
        end.setTime(d);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        Timestamp sqlEnd = new Timestamp(end.getTimeInMillis());
        
        Set<ServiceCall> rc = new LinkedHashSet<ServiceCall>();  
        
        try {
            Statement s = getStatement();
            
            String query = "SELECT wo_number, account_num, service_seq_number " +
                " FROM dbo.SERVICE_DISPATCH_1" +
                " WHERE date >= '" + sqlStart + "'" +
                "   AND date <= '" + sqlEnd   + "'";
            ResultSet r = s.executeQuery(query);

            while (r.next())
            {
                int wo_num = r.getInt("wo_number");
                @SuppressWarnings("unused")
				int acct = r.getInt("account_num");
                int seq = r.getInt("service_seq_number");
                
                ServiceLoc sl = new ServiceLoc(this, seq);
                ServiceCall sc = new ServiceCall(this, sl, wo_num);
                
                rc.add(sc);
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return rc;
    }

    
    public List<Integer> DeliveryLocations(Set<Integer> deliveryCenters)
    {
        String centers = "";
        for (Integer center : deliveryCenters)
        {
            if (centers.length() != 0)
                centers += ",";
            centers += center.toString(); 
        }
        
        try {
            Statement s = EnergyConn.getStatement();
            ResultSet r = s.executeQuery(
              "SELECT " +
              "   dbo.TANKS.tank_seq_number " +
              "FROM " +
              "   dbo.TANKS INNER JOIN " +
              "      (dbo.DELIVERY_ZONE INNER JOIN " +
              "       dbo.DELIVERY_CENTER_ZONE " +
              "      ON dbo.DELIVERY_ZONE.zone_id = dbo.DELIVERY_CENTER_ZONE.zone_id) " +
              "   ON dbo.TANKS.zone = dbo.DELIVERY_ZONE.description " +
              "WHERE " +
              "   dbo.DELIVERY_CENTER_ZONE.delivery_center_id in (" + centers + ")");

            List<Integer> rc = new ArrayList<Integer>();
            while (r.next())
            {
                rc.add(r.getInt("tank_seq_number"));
            }
            
            return rc;
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
    }
    
//TODO: Commented out - this looks unimplemented!
//    public Set<Technician> ActiveTechnicians() throws Exception
//    {
//        if (PegasusConn == null)
//            throw new Exception("Pegaus not connected");
//        Set<Technician> rc = new LinkedHashSet<Technician>();
//        return rc;
//    }
    
	public Statement getStatement() throws Exception
    {
	    return EnergyConn.getStatement();
    }

	public static void main(String[] args)
	{
		try
		{
            InfoFactory inf = new InfoFactory();

            int short_acct = inf.AccountNum(72124);
            @SuppressWarnings("unused")
			CustInfo ci = new CustInfo(inf, short_acct);
            short_acct = inf.AccountNum(532087);
            @SuppressWarnings("unused")
			CustInfo ci2 = new CustInfo(inf, short_acct);

            Set<Integer> centers = new LinkedHashSet<Integer>();
            centers.add(1);
            centers.add(2);
            
            @SuppressWarnings("unused")
			List<Integer> locations = inf.DeliveryLocations(centers);
            
            Calendar d = Calendar.getInstance();
            @SuppressWarnings("unused")
			Set<ServiceCall> calls = inf.CallsForDate(d.getTime());
            @SuppressWarnings("unused")
			int foo = 0;
        }
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
