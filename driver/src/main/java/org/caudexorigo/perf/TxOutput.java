package org.caudexorigo.perf;

public class TxOutput
{
	private int status;
	private double txTime;

	public TxOutput(int status)
	{
		super();
		this.status = status;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public double getTxTime()
	{
		return (txTime);
	}

	public void setTxTime(double txTime)
	{
		this.txTime = txTime;
	}
}