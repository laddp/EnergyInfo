/*
 * Created on Nov 19, 2009 by pladd
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
public class DeliveryLoc
{
    public static enum StatusEnum
    {
        ACTIVE, INACTIVE, TERMINATED
    }

    public final int     ShortAcct;
    public final int     TankNum;
    public final int     TankSeq;

    public final StatusEnum Status;
    public final int     StopCode;
    
    public final String  FillLoc;
    public final int     Product;
    public final int     Size;
    public final String  Zone;
    public final int     Group;
    
    public final AddressInfo Address;
    public final String      Instructions;
    
    public DeliveryLoc(InfoFactory inf, int tank_seq_num) throws Exception, SQLException
    {
        TankSeq = tank_seq_num;
        
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT account_num, tank_num, " +
                                     "fill_location, product, size, zone, delivery_group, " +
                                     "tank_status, delivery_stop " +
                                     "FROM dbo.TANKS " +
                                     "WHERE tank_seq_number = " + tank_seq_num);
        
        if (r.next()) {
            ShortAcct = r.getInt("account_num");
            TankNum   = r.getInt("tank_num");
            FillLoc   = r.getString("fill_location").trim();
            Product   = r.getInt("product");
            Size      = r.getInt("size");
            Zone      = r.getString("zone");
            Group     = r.getInt("delivery_group");
            StopCode  = r.getInt("delivery_stop");
            String stat = r.getString("tank_status");
            if (stat.compareTo("T")==0)
                Status = StatusEnum.TERMINATED;
            else if (stat.compareTo("I") == 0)
                Status = StatusEnum.INACTIVE;
            else if (stat.compareTo("A") == 0 || stat.compareTo(" ") == 0)
                Status = StatusEnum.ACTIVE;
            else
                Status = StatusEnum.ACTIVE;
        }
        else throw new Exception("Unable to find tank, seq #" + tank_seq_num);

        Instructions = PopulateInstructions(inf);

        AddressInfo i = null;
        try {
            i = new AddressInfo(inf, TankSeq, TankNum, Product);
        }
        catch (Exception e)
        {
        }
        Address = i;
    }

    public DeliveryLoc(InfoFactory inf, int acct, int tanknum) throws Exception, SQLException
    {
        ShortAcct = acct;
        TankNum = tanknum;
        
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT tank_seq_number, " +
                                     "fill_location, product, size, zone, delivery_group, " +
                                     "tank_status, delivery_stop " +
                                     "FROM dbo.TANKS " +
                                     "WHERE account_num = " + ShortAcct + " and tank_num = " + TankNum);
        
        if (r.next()) {
            TankSeq   = r.getInt("tank_seq_number");
            FillLoc   = r.getString("fill_location").trim();
            Product   = r.getInt("product");
            Size      = r.getInt("size");
            Zone      = r.getString("zone");
            Group     = r.getInt("delivery_group");
            StopCode  = r.getInt("delivery_stop");
            String stat = r.getString("tank_status");
            if (stat.compareTo("T")==0)
                Status = StatusEnum.TERMINATED;
            else if (stat.compareTo("I") == 0)
                Status = StatusEnum.INACTIVE;
            else if (stat.compareTo("A") == 0)
                Status = StatusEnum.ACTIVE;
            else
                Status = StatusEnum.ACTIVE;
        }
        else throw new Exception("Unable to find tank, short acct #" + acct + " tank #" + tanknum);
        
        Instructions = PopulateInstructions(inf);
        
        AddressInfo i = null;
        try {
            i = new AddressInfo(inf, TankSeq, TankNum, Product);
        }
        catch (Exception e)
        {
        }
        Address = i;
    }
    
    private String PopulateInstructions(InfoFactory inf) throws Exception
    {
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT din_text " +
                                     "FROM dbo.DIN_TEXT " +
                                     "WHERE din_text_owner = " + TankSeq);
        if (r.next())
        {
            return r.getString("din_text");
        }
        else
            return "";
    }
}
