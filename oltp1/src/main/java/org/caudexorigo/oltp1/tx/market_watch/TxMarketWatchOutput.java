package org.caudexorigo.oltp1.tx.market_watch;

import org.caudexorigo.perf.TxOutput;

public class TxMarketWatchOutput extends TxOutput
{
	private double pctChange;

	public TxMarketWatchOutput()
	{
		this(0);
	}

	public TxMarketWatchOutput(int status)
	{
		super(status);
	}

	public double getPctChange()
	{
		return pctChange;
	}

	public void setPctChange(double pctChange)
	{
		this.pctChange = pctChange;
	}

	@Override
	public String toString()
	{
		return String.format("TxMarketWatchOutput [%npct_change=%s%n, status=%s%n, tx_time=%.3f%n]", pctChange, getStatus(), getTxTime());
	}
}