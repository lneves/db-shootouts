package org.caudexorigo.oltp1.tx.trade_lookup;

import java.util.Arrays;
import java.util.Date;

public class TxTradeLookupInput
{
	private final int frameToexecute; // which of the frames to execute
	private final long[] tradeId;
	private final long acctId;
	private final long maxAcctId;
	private final int maxTrades;
	private final Date startTradeDts;
	private final Date endTradeDts;
	private final String symbol;

	public TxTradeLookupInput(int frameToexecute, long[] tradeId, long acctId, long maxAcctId, int maxTrades, Date startTradeDts, Date endTradeDts, String symbol)
	{
		super();
		this.frameToexecute = frameToexecute;
		this.tradeId = tradeId;
		this.acctId = acctId;
		this.maxAcctId = maxAcctId;
		this.maxTrades = maxTrades;
		this.startTradeDts = startTradeDts;
		this.endTradeDts = endTradeDts;
		this.symbol = symbol;
	}

	public int getFrameToexecute()
	{
		return frameToexecute;
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

	public Date getStartTradeDts()
	{
		return startTradeDts;
	}

	public Date getEndTradeDts()
	{
		return endTradeDts;
	}

	public String getSymbol()
	{
		return symbol;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeLookupInput [frame_to_execute=%s, trade_id=%s, acct_id=%s, max_acct_id=%s, max_trades=%s, start_trade_dts=%s, end_trade_dts=%s, symbol=%s]", frameToexecute, Arrays.toString(tradeId), acctId, maxAcctId, maxTrades, startTradeDts, endTradeDts, symbol);
	}
}