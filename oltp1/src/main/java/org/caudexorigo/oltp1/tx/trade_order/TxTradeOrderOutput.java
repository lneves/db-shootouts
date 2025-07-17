package org.caudexorigo.oltp1.tx.trade_order;

import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxTradeOrderOutput extends TxOutput
{
	private Map<String, Object> output;

	public TxTradeOrderOutput()
	{
		this(0);
	}

	public TxTradeOrderOutput(int status)
	{
		super(status);
	}

	public Map<String, Object> getOutput()
	{
		return output;
	}

	public void setOutput(Map<String, Object> output)
	{
		this.output = output;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeOrderOutput [%n%s%n, status=%s%n, tx_time=%.3f%n]", output, getStatus(), getTxTime());
	}
}