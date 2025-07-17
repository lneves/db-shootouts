package org.caudexorigo.oltp1.model;

public class AccountPermission
{
	private final String taxId;
	private final String lName;
	private final String fName;

	public AccountPermission(String taxId, String lName, String fName)
	{
		super();
		this.taxId = taxId;
		this.lName = lName;
		this.fName = fName;
	}

	public String getTaxId()
	{
		return taxId;
	}

	public String getlName()
	{
		return lName;
	}

	public String getfName()
	{
		return fName;
	}

	@Override
	public String toString()
	{
		return String.format("AccountPermission [ap_tax_id=%s, ap_l_name=%s, ap_f_name=%s]", taxId, lName, fName);
	}
}
