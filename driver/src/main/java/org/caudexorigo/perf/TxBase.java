package org.caudexorigo.perf;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TxBase implements Tx
{
	private static Logger log = LoggerFactory.getLogger(TxBase.class);

	private final String txName;
	private final double mixPct;
	private final StatsCollector stats;

	public TxBase(StatsCollector stats)
	{
		super();
		this.stats = stats;

		Assert.notBlank("txName", stats.getTxName());
		Assert.isInRange("mixPct", stats.getMixPct(), 0.0, 1.0);

		this.txName = stats.getTxName();
		this.mixPct = stats.getMixPct();
	}

	public final String name()
	{
		return txName;
	}

	public final double getMixPct()
	{
		return mixPct;
	}

	@Override
	public final TxOutput execute()
	{
		final long start = System.nanoTime();
		stats.offerMinTs(start);

		try
		{
			TxOutput txOut = run();

			final long stop = System.nanoTime();
			double txTime = (stop - start) / 1000000.0;
			txOut.setTxTime(txTime);

			stats.offerMaxTs(stop);
			stats.addValue(txOut.getTxTime());
			
			
			if (txOut.getStatus() < 0)
			{
				stats.incrementErrors();
			}


			if (txOut.getStatus() > 0)
			{
				stats.incrementWarnings();
			}

			if (log.isDebugEnabled())
			{
				log.debug(txOut.toString());
			}

			return txOut;
		}
		catch (Throwable t)
		{
			stats.incrementErrors();
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);

			List<StackTraceElement> stacklements = Arrays.asList(r.getStackTrace());

			StackTraceElement trace = stacklements.stream()
					.filter(s -> StringUtils.startsWith(s.getClassName(), "org.caudexorigo"))
					.findFirst()
					.orElse(new StackTraceElement(this.getClass().getName(), "<no method>", "<no filename>", 0));

			final long stop = System.nanoTime();
			TxError txError = new TxError(t.getMessage(), trace.toString());
			txError.setTxTime(stop - start);

			stats.offerMaxTs(stop);
			stats.addValue(txError.getTxTime());
			return txError;
		}
	}

	protected abstract TxOutput run();

	public void clearStats()
	{
		stats.clearStats();
	}
}