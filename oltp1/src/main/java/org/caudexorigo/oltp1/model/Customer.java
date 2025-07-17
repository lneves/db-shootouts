package org.caudexorigo.oltp1.model;

public class Customer
{
	private final long cId;
	private final String cTaxId;
	private final short tier;

	public Customer(long cId, String cTaxId, short tier)
	{
		super();
		this.cId = cId;
		this.cTaxId = cTaxId;
		this.tier = tier;
	}

	public long getId()
	{
		return cId;
	}

	public String getTaxId()
	{
		return cTaxId;
	}

	public short getTier()
	{
		return tier;
	}

}