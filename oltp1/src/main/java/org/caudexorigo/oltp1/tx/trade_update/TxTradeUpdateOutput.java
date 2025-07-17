package org.caudexorigo.oltp1.tx.trade_update;

import java.util.List;
import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxTradeUpdateOutput extends TxOutput
{
	private Map<String, Object> tradeName;
	private List<Map<String, Object>> lstTradeStatus;
	private int numFound;

	protected TxTradeUpdateOutput()
	{
		this(0);
	}

	protected TxTradeUpdateOutput(int status)
	{
		super(status);
	}

	public void setTradeStatus(List<Map<String, Object>> tradeStatus)
	{
		lstTradeStatus = tradeStatus;
	}

	public void setTradeName(Map<String, Object> tradeName)
	{
		this.tradeName = tradeName;
	}

	public void setNumFound(int numFound)
	{
		this.numFound = numFound;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeUpdateOutput [%ntrade_name=%s%n, num_found=%s%n, trade_status=%s%n, status=%s%n, tx_time=%.3f%n]", tradeName, numFound, lstTradeStatus, getStatus(), getTxTime());
	}
}