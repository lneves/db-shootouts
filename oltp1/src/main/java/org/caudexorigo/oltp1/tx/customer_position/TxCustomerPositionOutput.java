package org.caudexorigo.oltp1.tx.customer_position;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxCustomerPositionOutput extends TxOutput
{
	private List<Map<String, Object>> customerAccounts;
	private int assetsSize;
	private Map<String, Object> customer;

	private int historySize;
	private List<Map<String, Object>> tradeHistory;
	
	public TxCustomerPositionOutput()
	{
		this(0);
	}

	public TxCustomerPositionOutput(int status)
	{
		super(status);
		setCustomerAccounts(Collections.emptyList());
		setTradeHistory(Collections.emptyList());
	}

	public int getAassetsSize()
	{
		return assetsSize;
	}

	public List<Map<String, Object>> getCustomerAccounts()
	{
		return customerAccounts;
	}

	public int getAssetsSize()
	{
		return assetsSize;
	}

	public Map<String, Object> getCustomer()
	{
		return customer;
	}

	public int getHistorySize()
	{
		return historySize;
	}

	public List<Map<String, Object>> getTradeHistory()
	{
		return tradeHistory;
	}

	public void setCustomerAccounts(List<Map<String, Object>> assets)
	{
		this.customerAccounts = assets;
		this.assetsSize = assets.size();
	}

	public void setCustomer(Map<String, Object> customer)
	{
		this.customer = customer;
	}

	public void setTradeHistory(List<Map<String, Object>> tradeHistory)
	{
		this.tradeHistory = tradeHistory;
		this.historySize = tradeHistory.size();
	}

	@Override
	public String toString()
	{
		return String.format("TxCustomerPositionOutput [%ncustomer=%s%n, customer_accounts=%s%n, acct_len=%s%n, trade_history=%s%n, hist_len=%s%n, status=%s%n, tx_time=%.3f%n]", customer, customerAccounts, assetsSize, tradeHistory, historySize, getStatus(), getTxTime());
	}
}