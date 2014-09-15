/*
 * Created on Nov 19, 2009 by pladd
 *
 */
package com.bottinifuel.Energy.Info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * @author pladd
 *
 */
public class ServiceLoc
{
    public final int     ShortAcct;
    public final int     ServiceNum;
    public final int     ServiceSeq;

    public final AddressInfo ServiceAddress;
    
    public final String Zone;
    public final String Contract;
    
    public final Date LastCleaning;
    public final Date LastService;

    public ServiceLoc(InfoFactory inf, int service_seq_num) throws Exception, SQLException
    {
        ServiceSeq = service_seq_num;
        
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT account_num, service_num," +
                                     " zone, service_contract," +
                                     " last_clean_date, date_last_service" +
                                     " FROM dbo.SERVICE " +
                                     " WHERE service_seq_number = " + service_seq_num);
        
        if (r.next()) {
            ShortAcct = r.getInt("account_num");
            ServiceNum = r.getInt("service_num");
            
            Zone = r.getString("zone");
            Contract = r.getString("service_contract");
            
            LastCleaning = r.getDate("last_clean_date");
            LastService = r.getDate("date_last_service");
        }
        else throw new Exception("Unable to find service location, seq #" + service_seq_num);

        AddressInfo i = null;
        try {
            i = new AddressInfo(inf, ServiceSeq, ServiceNum);
        }
        catch (Exception e)
        {
        }
        ServiceAddress = i;
    }

    public ServiceLoc(InfoFactory inf, int sacct, int svcnum) throws Exception, SQLException
    {
        ShortAcct = sacct;
        ServiceNum = svcnum;
        
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT service_seq_number," +
                                     " zone, service_contract," +
                                     " last_clean_date, date_last_service" +
                                     " FROM dbo.SERVICE " +
                                     " WHERE account_num = " + ShortAcct + " and service_num = " + ServiceNum);
        if (r.next()) {
            ServiceSeq = r.getInt("service_seq_number");
            Zone = r.getString("zone");
            Contract = r.getString("service_contract");
            
            LastCleaning = r.getDate("last_clean_date");
            LastService = r.getDate("date_last_service");
        }
        else throw new Exception("Unable to find service location, short acct #" + sacct + " loc #" + svcnum);
        
        ServiceAddress = new AddressInfo(inf, ServiceSeq, ServiceNum);
    }
}
