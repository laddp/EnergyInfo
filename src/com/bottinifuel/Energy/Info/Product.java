/**
 * 
 */
package com.bottinifuel.Energy.Info;

import java.io.Serializable;

/**
 * @author laddp
 *
 */
public class Product implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8985376842097272892L;
	protected String Manufacturer;
	protected String Group;
	protected String StockID;
	protected String Description;
	protected int    PostingCode;
	
	protected Product(String manuf, String grp, String stock, String desc, int post)
	{
		Manufacturer = manuf;
		Group        = grp;
		StockID      = stock;
		Description  = desc;
		PostingCode  = post;
	}

	// For use by subclasses that will set info themselves
	protected Product()
	{
	}
	
	/**
	 * @param manufacturer the manufacturer to set
	 */
	protected void setManufacturer(String manufacturer) {
		Manufacturer = manufacturer;
	}

	/**
	 * @param group the group to set
	 */
	protected void setGroup(String group) {
		Group = group;
	}

	/**
	 * @param stockID the stockID to set
	 */
	protected void setStockID(String stockID) {
		StockID = stockID;
	}

	/**
	 * @param description the description to set
	 */
	protected void setDescription(String description) {
		Description = description;
	}

	/**
	 * @param postingCode the postingCode to set
	 */
	protected void setPostingCode(int postingCode) {
		PostingCode = postingCode;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return Manufacturer;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return Group;
	}

	/**
	 * @return the stockID
	 */
	public String getStockID() {
		return StockID;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return Description;
	}

	/**
	 * @return the postingCode
	 */
	public int getPostingCode() {
		return PostingCode;
	}

	public String toString()
	{
		return StockID + " - (" + Manufacturer + "/" + Group + ") " + Description;
	}
}
