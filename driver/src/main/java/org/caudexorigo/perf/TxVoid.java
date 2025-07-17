package org.caudexorigo.perf;

public class TxVoid extends TxBase
{
	private static TxOutput nullOutput = new TxOutput(0)
	{
	};

	public TxVoid(StatsCollector stats)
	{
		super(stats);
	}


	@Override
	public TxOutput run()
	{
		return nullOutput;
	}
}