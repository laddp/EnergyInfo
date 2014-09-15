/*
 * Created on Jan 19, 2007 by pladd
 *
 */
package com.bottinifuel.Energy.Info;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author pladd
 *
 */
public class ServicePart extends Product
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int     PartNum;
    public final String  Unit_name;
    public final double  Cost;
    public final double  Multiplier;
    public final boolean OverrideMultiplier;
    public final double  CustCost;
    public final boolean Warranty_coverage[];
    
    public ServicePart(InfoFactory inf, int partNum) throws Exception
    {
        PartNum = partNum;
        String query = "SELECT " +
            "dbo.SERVICE_PART.part_group, " +
            "dbo.SERVICE_PART.part_stock_id, " +
            "dbo.PRODUCT.unit_name, " +
            "dbo.PRODUCT.description, " + 
            "dbo.PRODUCT.reference_price, " +
            "dbo.SERVICE_PART.a_coverage, " +
            "dbo.SERVICE_PART.b_coverage, " +
            "dbo.SERVICE_PART.c_coverage, " +
            "dbo.SERVICE_PART.d_coverage, " +
            "dbo.SERVICE_PART.e_coverage, " +
            "dbo.SERVICE_PART.f_coverage, " +
            "dbo.SERVICE_PART.g_coverage, " +
            "dbo.SERVICE_PART.part_mfg " +
            "dbo.PRODUCT.posting_code " +
        "FROM (dbo.PRODUCT INNER JOIN dbo.SERVICE_PART ON dbo.PRODUCT.service_part_num = dbo.SERVICE_PART.part_number) " +
            "INNER JOIN dbo.GROUP_INFO ON dbo.PRODUCT.group_code = dbo.GROUP_INFO.group_code " +
        "WHERE dbo.SERVICE_PART.part_number = " + partNum +
            "AND dbo.SERVICE_PART.status = 'A' " +
            "AND dbo.GROUP_INFO.status = 'A'";

        Statement s = inf.getStatement();
        // Find the account
        ResultSet r = s.executeQuery(query);
        if (!r.next())
        {
            throw new Exception("ServicePart #" + partNum + " not found");
        }
        else
        {
            Manufacturer = r.getString("part_mfg");
            Group        = r.getString("part_group");
            StockID      = r.getString("part_stock_id");
            Description  = r.getString("description");
            PostingCode  = r.getInt("posting_code");

            Warranty_coverage = new boolean[7];
            Warranty_coverage[0] = r.getString("a_coverage").equals("Y");
            Warranty_coverage[1] = r.getString("b_coverage").equals("Y");
            Warranty_coverage[2] = r.getString("c_coverage").equals("Y");
            Warranty_coverage[3] = r.getString("d_coverage").equals("Y");
            Warranty_coverage[4] = r.getString("e_coverage").equals("Y");
            Warranty_coverage[5] = r.getString("f_coverage").equals("Y");
            Warranty_coverage[6] = r.getString("g_coverage").equals("Y");
            Unit_name = r.getString("unit_name");
            Cost = r.getDouble("reference_price");
            
            query = "SELECT " +
                    "price " +
                "FROM dbo.VOLUME_DISCOUNT_DTL " +
                "WHERE " +
                    "mfg_code = '" + Manufacturer + "' AND " +
                    "group_code = '" + Group + "' AND " +
                    "stock_id = '" + StockID + "'";
            r = s.executeQuery(query);
            Double mult;
            if (r.next())
            {
                mult = r.getDouble("price");
                OverrideMultiplier = true;
            }
            else
            {
                query = "SELECT " +
                        "price " +
                    "FROM dbo.VOLUME_DISCOUNT_DTL " +
                    "WHERE " +
                        "mfg_code = '" + Manufacturer + "' AND " +
                        "group_code = '" + Group + "'";
                r = s.executeQuery(query);
                if (r.next())
                {
                    mult = r.getDouble("price");
                    OverrideMultiplier = false;
                }
                else
                {
                    mult = -1.0;
                    OverrideMultiplier = false;
                }
            }
            Multiplier = mult;
            CustCost = Cost * Multiplier;
        }
    }
}
