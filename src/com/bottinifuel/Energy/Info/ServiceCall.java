/*
 * Created on Nov 19, 2009 by pladd
 *
 */
package com.bottinifuel.Energy.Info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class ServiceCall
{
    private static InfoFactory Info;
    private static SortedMap<Integer, String> CallReasons;
    
    private static Set<Integer> EstimateReasons;
    private static Set<Integer> CleaningReasons;
    private static Set<Integer> InstallReasons;
    @SuppressWarnings("unused")
	private static Set<Integer> PropaneReasons;
    
    public final ServiceLoc Location;
    public final int        WorkorderNum;
    
    public final int Reason1;
    public final int Reason2;
    public final int Reason3;
    
    public final boolean Forced;
    public final boolean CallScheduler;
    
    public final String Comment;
    
    public final boolean PhoneFirst;
    public final String  PhoneFirstPhone;
    
    public final Date SchedStartDateTime;
    public final Date SchedEndDateTime;
    public final int  EstTimeOnSite;

    public ServiceCall(InfoFactory inf, ServiceLoc loc, int wo_num) throws Exception, SQLException
    {
        Info = inf;
        Location = loc;
        WorkorderNum = wo_num;
        
        Statement s = inf.getStatement();
        ResultSet r = s.executeQuery("SELECT dbo.SERVICE_DISPATCH_1.date," +
                                     "       dbo.SERVICE_DISPATCH_1.from_time," +
                                     "       dbo.SERVICE_DISPATCH_1.to_time," +
                                     "       dbo.SERVICE_DISPATCH_1.call_reason_1," +
                                     "       dbo.SERVICE_DISPATCH_1.call_reason_2," +
                                     "       dbo.SERVICE_DISPATCH_1.call_reason_3," +
                                     "       dbo.SERVICE_DISPATCH_1.calendar_schedule," +
                                     "       dbo.SERVICE_DISPATCH_1.csr_forced_date," +
                                     "       dbo.SERVICE_DISPATCH_1.est_on_site_time," +
                                     "       dbo.SERVICE_DISPATCH_1.phone_first," +
                                     "       dbo.SERVICE_DISPATCH_1.telephone," +
                                     "       dbo.SERVICE_DISPATCH.text" +
                                     " FROM " +
                                     "      dbo.SERVICE_DISPATCH_1 " +
                                     " INNER JOIN " +
                                     "      dbo.SERVICE_DISPATCH" +
                                     " ON dbo.SERVICE_DISPATCH.f15_record_num = dbo.SERVICE_DISPATCH_1.f15_record_num" +
                                     " WHERE dbo.SERVICE_DISPATCH_1.account_num = " + Location.ShortAcct + 
                                     "   and dbo.SERVICE_DISPATCH_1.service_seq_number = " + Location.ServiceSeq +
                                     "   and dbo.SERVICE_DISPATCH_1.wo_number = " + wo_num);
        if (r.next()) {
            Reason1 = r.getInt("call_reason_1");
            Reason2 = r.getInt("call_reason_2");
            Reason3 = r.getInt("call_reason_3");
            
            String forced = r.getString("csr_forced_date");
            if (forced != null && forced.equals("Y"))
                Forced = true;
            else
                Forced = false;

            String sched = r.getString("calendar_schedule");
            if (sched != null && sched.equals("Y"))
                CallScheduler = true;
            else
                CallScheduler = false;

            Comment = r.getString("text");
            
            PhoneFirst = r.getBoolean("phone_first");
            PhoneFirstPhone = r.getString("telephone");
            
            Date d = r.getDate("date");

            Calendar cal = new GregorianCalendar();
            cal.setTime(d);
            cal.set(Calendar.MILLISECOND, 0);

            int start = r.getInt("from_time");
            cal.set(Calendar.HOUR_OF_DAY, start/100);
            cal.set(Calendar.MINUTE,      start - ((start/100)*100));
            SchedStartDateTime = cal.getTime();
            
            int end = r.getInt("to_time");
            cal.set(Calendar.HOUR_OF_DAY, end/100);
            cal.set(Calendar.MINUTE,      end - ((end/100)*100));
            SchedEndDateTime = cal.getTime();
            EstTimeOnSite  = r.getInt("est_on_site_time");
        }
        else throw new Exception("Call not found: wo_num " + wo_num);
    }
    
    private static void InitCallReasons()
    {
        try {
            Statement s = Info.getStatement();
            ResultSet r = s.executeQuery("SELECT reason, description" +
            " FROM dbo.CALL_REASONS");

            CallReasons = new TreeMap<Integer, String>();
            while (r.next()) {
                Integer code = r.getInt("reason");
                String descr = r.getString("description");
                CallReasons.put(code, descr);
            }
        }
        catch (Exception e)
        {}
    }
    
    public static SortedMap<Integer, String> AllCallReasons()
    {
        if (CallReasons == null)
            InitCallReasons();
        return CallReasons;
    }
    
    public static String CallReasonDescription(int reason)
    {
        if (CallReasons == null)
            InitCallReasons();
        String rc = CallReasons.get(reason);
        if (rc == null)
            return "";
        else
            return rc;
    }
    
    private boolean CheckReasons(Set<Integer> reasons)
    {
        if (reasons == null)
            return false;
        for (Integer r : reasons)
        {
            if (Reason1 == r  || Reason2 == r  || Reason3 == r)
                return true;
        }
        return false;
    }

    public static void SetEstimateReasons(Set<Integer> s)
    {
        EstimateReasons = s;
    }
    
    public boolean IsEstimate()
    {
        return CheckReasons(EstimateReasons);
    }

    public static void SetCleaningReasons(Set<Integer> s)
    {
        CleaningReasons = s;
    }
    
    public boolean IsCleaning()
    {
        return CheckReasons(CleaningReasons);
    }
    
    public static void SetPropaneReasons(Set<Integer> s)
    {
        PropaneReasons = s;
    }
    
    public boolean IsPropane()
    {
        if (Reason1 >= 215 || Reason2 >= 215 || Reason3 >= 215)
            return true;
        return false;
    }
    
    public static void SetInstallReasons(Set<Integer> s)
    {
        InstallReasons = s;
    }
    
    public boolean IsInstall()
    {
        return CheckReasons(InstallReasons);
    }
}
