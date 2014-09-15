/*
 * Created on Oct 14, 2004 by pladd
 *
 */
package com.bottinifuel.Energy.Info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * @author pladd
 *
 */
public class AddressInfo
{
	/**
     * 
     */
    @SuppressWarnings("unused")
	private static final long serialVersionUID = 3257849878779345205L;
    public final String Name;
    public final String Street1;
    public final String Street2;
    public final String City;
    public final String State;
    public final String Zip;
    public final String ZipRaw;
	public final String PhoneNum;
	public final String AddressLabel;
    public final String AddressLabelNoProd;
	private InfoFactory inf;

    public String Phone()
    {
        if (PhoneNum != null)
        {
            if (PhoneNum.length() == 10)
                return "(" + PhoneNum.substring(0,3) + ")" + PhoneNum.substring(3,6) + "-" + PhoneNum.substring(6,10);
            else if (PhoneNum.length() == 7)
                return PhoneNum.substring(0,3) + "-" + PhoneNum.substring(3,7);
            else
                return PhoneNum;
        }
        else
            return "";
    }

    public String Addr()
    {
        String rc = Street1 + "\n"; 
        if (Street2 != null && Street2.length() != 0)
            rc += Street2 + "\n";
        rc += City + ", " + State + " " + Zip;
        return rc;
    }
    public String FullAddr() { return Name + "\n" + Addr(); }

	public AddressInfo(InfoFactory i, ResultSet r, String lbl)	throws Exception
	{
        inf = i;
        AddressLabel = AddressLabelNoProd = lbl;
		Name = r.getString("name").trim();
        Street2 = r.getString("street2").trim();
		PhoneNum = r.getString("telephone").trim();
		Street1 = r.getString("street1").trim();
        City = r.getString("city").trim();
        State = r.getString("state").trim();
        String fullZip = ZipRaw = r.getString("postal_code").trim();
        if (fullZip != null && fullZip.length() > 5)
            if (fullZip.substring(5).equals("0000"))
                fullZip = fullZip.substring(0,5);
            else
                fullZip = fullZip.substring(0,5) + "-" + fullZip.substring(5);
        Zip = fullZip;
	}

	private String [] PopulateFields(String prefix, int seq) throws Exception, SQLException
	{
        Statement s = inf.getStatement();

        String query = "SELECT " + prefix + "_title, " + prefix + "_first_name, " + prefix + "_middle_initial, " +
                                   prefix + "_last_name, " + prefix + "_name_suffix, " + prefix + "_street1, " +
                                   prefix + "_street2, " + prefix + "_city, " + prefix + "_state, " + prefix + "_postal_code " +
                       "FROM dbo." + prefix.toUpperCase() + "_TEXT " +
                       "WHERE " + prefix + "_text_owner = " + seq;
        ResultSet r = s.executeQuery(query);

        if (!r.next())
        {
            throw new Exception("Missing address for " + AddressLabel);
        }

        String [] rc = new String[7];
        
        rc[1] = r.getString(prefix + "_street1").trim();
        
        String s2 = r.getString(prefix + "_street2");
        if (s2 != null)
            rc[2] = s2.trim();

        rc[3] = r.getString(prefix + "_city").trim();
        rc[4] = r.getString(prefix + "_state").trim();
        String fullZip = rc[6] = r.getString(prefix + "_postal_code").trim();
        if (fullZip != null && fullZip.length() > 5)
            if (fullZip.substring(5).equals("0000"))
                fullZip = fullZip.substring(0,5);
            else
                fullZip = fullZip.substring(0,5) + "-" + fullZip.substring(5);
        rc[5] = fullZip;

		String buildName = "";
		String tmp;
		
		tmp = r.getString(prefix + "_title");  
        if (tmp != null) tmp.trim();
        if (tmp != null && tmp.length() != 0 && !tmp.matches("^\\s+$"))
            buildName += tmp;
        
		tmp = r.getString(prefix + "_first_name");     
        if (tmp != null) tmp.trim(); 
        if (tmp != null && tmp.length() != 0 && !tmp.matches("^\\s+$"))
        {
            if (buildName.length() != 0) buildName += " ";
            buildName += tmp;
        }
        
		tmp = r.getString(prefix + "_middle_initial"); 
        if (tmp != null) tmp.trim(); 
        if (tmp != null && tmp.length() != 0 && !tmp.matches("^\\s+$"))
        {
            if (buildName.length() != 0) buildName += " "; 
            buildName += tmp;
        }
        
        tmp = r.getString(prefix + "_last_name");      
        if (tmp != null) tmp.trim(); 
        if (tmp != null && tmp.length() != 0 && !tmp.matches("^\\s+$"))
        {
            if (buildName.length() != 0) buildName += " "; 
            buildName += tmp;
        }
        
		tmp = r.getString(prefix + "_name_suffix");   
        if (tmp != null) tmp.trim(); 
        if (tmp != null && tmp.length() != 0 && !tmp.matches("^\\s+$"))
        {
            if (buildName.length() != 0) buildName += " "; 
            buildName += tmp;
        }
		
        if (r.next()) throw new Exception("Extra address record for " + AddressLabel);

        rc[0] = buildName.trim();
        
        return rc;
	}

	public AddressInfo(InfoFactory i, int tank_seq, int tank_num, int product) throws Exception, SQLException
	{
        inf = i;
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT short_desc FROM dbo.POST_CODE WHERE product = " + product);

        AddressLabelNoProd = "Tank #" + tank_num;
        if (r.next())
        {           
            AddressLabel = AddressLabelNoProd + " (" + r.getString("short_desc") + ")";
        }
        else
        {
            AddressLabel = AddressLabelNoProd + " (" + product + ")";
        }

		PhoneNum = "";

        String [] rc = PopulateFields("dad", tank_seq);
        Name = rc[0];
        Street1 = rc[1];
        Street2 = rc[2];
        City = rc[3];
        State = rc[4];
        Zip = rc[5];
        ZipRaw = rc[6];
	}

	public AddressInfo(InfoFactory i, int serv_seq, int serv_num) throws Exception, SQLException
	{
        inf = i;
		AddressLabel = AddressLabelNoProd = "Service Loc #" + serv_num;
		PhoneNum = "";

        String [] rc = PopulateFields("sad", serv_seq);
        Name = rc[0];
        Street1 = rc[1];
        Street2 = rc[2];
        City = rc[3];
        State = rc[4];
        Zip = rc[5];
        ZipRaw = rc[6];
    }
	
	public String NameNoEmbeddedDoubleBlanks()
	{
	    String rc = Name;
	    rc = rc.replaceAll("\\s+", " ");
	    return rc;
	}

    public String toString()
    {
        String rc = AddressLabel + ":\n" + FullAddr();
        String p = Phone();
        if (p != null && p.length() != 0)
            rc += "\n" + p;
        return rc;
    }
}
