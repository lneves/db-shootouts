package org.caudexorigo.oltp1.tx.trade_status;

public class TxTradeStatusInput
{
	private long acctId;

	public TxTradeStatusInput(long acctId)
	{
		super();
		this.acctId = acctId;
	}

	public Object getAcctId()
	{
		return acctId;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeLookupInput [%nacctId=%s%n]", acctId);
	}

}