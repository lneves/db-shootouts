package org.caudexorigo.oltp1.tx.customer_position;

public class TxCustomerPositionInput
{
	private int acctIdIdx;
	private long custId;
	private boolean getHistory;
	private String taxId;

	public TxCustomerPositionInput()
	{
		super();
	}

	public TxCustomerPositionInput(int acctIdIdx, long custId, boolean getHistory, String taxId)
	{
		super();
		this.acctIdIdx = acctIdIdx;
		this.custId = custId;
		this.getHistory = getHistory;
		this.taxId = taxId;
	}

	public int getAcctIdIdx()
	{
		return acctIdIdx;
	}

	public long getCustomerId()
	{
		return custId;
	}

	public boolean getHistory()
	{
		return getHistory;
	}

	public String getTaxId()
	{
		return taxId;
	}

	public void setAcctIdIdx(int acctIdIdx)
	{
		this.acctIdIdx = acctIdIdx;
	}

	public void setCustomerId(long custId)
	{
		this.custId = custId;
	}

	public void setHistory(boolean getHistory)
	{
		this.getHistory = getHistory;
	}

	public void setTaxId(String taxId)
	{
		this.taxId = taxId;
	}

	@Override
	public String toString()
	{
		return String.format("TxCustomerPositionInput [acct_id_idx=%s, cust_id=%s, get_history=%s, tax_id=%s]", acctIdIdx, custId, getHistory, taxId);
	}
}