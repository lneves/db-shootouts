package org.caudexorigo.oltp1.tx.trade_result;

import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxTradeResultOutput extends TxOutput
{
	private Map<String, Object> output;

	public TxTradeResultOutput()
	{
		this(0);
	}

	public TxTradeResultOutput(int status)
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
		return String.format("TxTradeResultOutput [%n%s%n, status=%s%n, tx_time=%.3f%n]", output, getStatus(), getTxTime());
	}
}