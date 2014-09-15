/*
 * Created on Oct 6, 2004 by pladd
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
public class CustInfo {
    public final int     ShortAcct;
    public final boolean Terminated;
    public final double  Balance;
    public final double  BudgetPayment;
    public final double  InstallmentBal;
    public final double  LastPymt;
    public final double  Promised;
    public final double  StmtDue;
    public final double  StmtBal;
    public final double  StmtNonBudget;
    
    public final String  Name;
    public final String  Street1;
    public final String  Street2;
    public final String  Town;
    public final String  State;
    public final String  Zip;
    
    public final int     Division;
    public final int     Type;
    public final boolean InvoiceSpecialHandling;
    
    public final int     BudgetStart;
    public final int     NumBudgetPayments;

    public AddressInfo[] Addrs;

	public CustInfo(InfoFactory inf, int acct_num) throws Exception
	{
        ShortAcct = acct_num;

        try
		{
			Statement s = inf.getStatement();
			ResultSet r = s.executeQuery("SELECT terminated, balance, budget_payment, installment_balance, last_payment_amount, promised_amount, " +
                                         " amount_due_last_stmt, balance_last_stmt, non_budget_last_statement, " +
                                         " division, type, special_handling, budget_start, num_budget_payments, " +
                                         " name, street1, street2, city, state, postal_code " +
										 " FROM dbo.ACCOUNTS " +
										 " WHERE account_num = " + ShortAcct);
			if (r.next()) {
                String term = r.getString("terminated");
                if (term != null && term.equals("Y"))
                    Terminated = true;
                else
                    Terminated = false;
				Balance        = r.getDouble("balance");
                BudgetPayment  = r.getDouble("budget_payment");
                InstallmentBal = r.getDouble("installment_balance");
                LastPymt       = r.getDouble("last_payment_amount");
                Promised       = r.getDouble("promised_amount");
                StmtBal        = r.getDouble("amount_due_last_stmt");
                StmtDue        = r.getDouble("balance_last_stmt");
                StmtNonBudget  = r.getDouble("non_budget_last_statement");
                
                Name           = r.getString("name");
                Street1        = r.getString("street1");
                Street2        = r.getString("street2");
                Town           = r.getString("city");
                State          = r.getString("state");
                Zip            = r.getString("postal_code");
                
                Division       = r.getInt("division");
                Type           = r.getInt("type");
                String sh      = r.getString("special_handling");
                if (sh.equals("Y"))
                    InvoiceSpecialHandling = true;
                else
                    InvoiceSpecialHandling = false;

                BudgetStart    = r.getInt("budget_start");
                NumBudgetPayments = r.getInt("num_budget_payments");
                
				if (r.next()) throw new Exception("Duplicate customer number"); 
			}
			else throw new Exception("Invalid customer number");
		}

        catch (SQLException e)
		{
            System.out.println(e);
			throw new Exception("SQL Error locating customer info");
		}

        Addrs = inf.GetAllAddresses(ShortAcct);
	}

    public boolean isBudget()
    {
        return (NumBudgetPayments != 0 && BudgetStart != 0);
    }
}
