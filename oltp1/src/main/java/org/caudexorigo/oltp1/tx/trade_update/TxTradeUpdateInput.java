package org.caudexorigo.oltp1.tx.trade_update;

import java.util.Arrays;
import java.util.Date;

public class TxTradeUpdateInput
{
	private int frameToExecute; // which of the frames to execute
	private long[] tradeId;
	private long acctId;
	private long maxAcctId;
	private int maxTrades;
	private int maxUpdates;
	private Date endTradeDts;
	private Date startTradeDts;
	private String symbol;

	public TxTradeUpdateInput()
	{
		super();
	}

	public TxTradeUpdateInput(int frameToExecute, long[] tradeId, long acctId, long maxAcctId, int maxTrades, int maxUpdates, Date endTradeDts, Date startTradeDts, String symbol)
	{
		super();
		this.frameToExecute = frameToExecute;
		this.tradeId = tradeId;
		this.acctId = acctId;
		this.maxAcctId = maxAcctId;
		this.maxTrades = maxTrades;
		this.maxUpdates = maxUpdates;
		this.endTradeDts = endTradeDts;
		this.startTradeDts = startTradeDts;
		this.symbol = symbol;
	}

	public int getFrameToExecute()
	{
		return frameToExecute;
	}

	public long[] getTradeId()
	{
		return tradeId;
	}

	public long getAcctId()
	{
		return acctId;
	}

	public long getMaxAcctId()
	{
		return maxAcctId;
	}

	public int getMaxTrades()
	{
		return maxTrades;
	}

	public int getMaxUpdates()
	{
		return maxUpdates;
	}

	public Date getEndTradeDts()
	{
		return endTradeDts;
	}

	public Date getStartTradeDts()
	{
		return startTradeDts;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public void setFrameToExecute(int frameToExecute)
	{
		this.frameToExecute = frameToExecute;
	}

	public void setTradeId(long[] tradeId)
	{
		this.tradeId = tradeId;
	}

	public void setAcctId(long acctId)
	{
		this.acctId = acctId;
	}

	public void setMaxAcctId(long maxAcctId)
	{
		this.maxAcctId = maxAcctId;
	}

	public void setMaxTrades(int maxTrades)
	{
		this.maxTrades = maxTrades;
	}

	public void setMaxUpdates(int maxUpdates)
	{
		this.maxUpdates = maxUpdates;
	}

	public void setEndTradeDts(Date endTradeDts)
	{
		this.endTradeDts = endTradeDts;
	}

	public void setStartTradeDts(Date startTradeDts)
	{
		this.startTradeDts = startTradeDts;
	}

	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeUpdateInput [frameToExecute=%s, tradeId=%s, acctId=%s, maxAcctId=%s, maxTrades=%s, maxUpdates=%s, endTradeDts=%s, startTradeDts=%s, symbol=%s]", frameToExecute, Arrays.toString(tradeId), acctId, maxAcctId, maxTrades, maxUpdates, endTradeDts, startTradeDts, symbol);
	}
}