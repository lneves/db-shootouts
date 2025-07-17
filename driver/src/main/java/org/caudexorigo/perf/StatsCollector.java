package org.caudexorigo.perf;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.tdunning.math.stats.TDigest;

public class StatsCollector
{
	private final AtomicLong errorCounter = new AtomicLong();
	private final AtomicLong warningCounter = new AtomicLong();
	private AtomicLong minTs = new AtomicLong(Long.MAX_VALUE);
	private AtomicLong maxTs = new AtomicLong(Long.MIN_VALUE);
	private final Object mutex = new Object();
	private final SummaryStatistics stats = new SummaryStatistics();
	private TDigest histo = TDigest.createDigest(100);

	private final String txName;
	private final double mixPct;

	public StatsCollector(String txName, double mixPct)
	{
		super();
		this.txName = txName;
		this.mixPct = mixPct;
	}

	public StatsCollector(AtomicLong minTs, AtomicLong maxTs, TDigest histo, String txName, double mixPct)
	{
		super();
		this.minTs = minTs;
		this.maxTs = maxTs;
		this.histo = histo;
		this.txName = txName;
		this.mixPct = mixPct;
	}

	public void offerMinTs(long ts)
	{
		minTs.accumulateAndGet(ts, (o, n) -> Math.min(o, n));
	}

	public void offerMaxTs(long ts)
	{
		maxTs.accumulateAndGet(ts, (o, n) -> Math.max(o, n));
	}

	public void incrementErrors()
	{
		errorCounter.incrementAndGet();
	}

	public void incrementWarnings()
	{
		warningCounter.incrementAndGet();
	}

	public final long getErrorCount()
	{
		return errorCounter.get();
	}

	public final long getWarningCount()
	{
		return warningCounter.get();
	}

	public final void addValue(double v)
	{
		synchronized (mutex)
		{
			stats.addValue(v);
			histo.add(v);
		}
	}

	public final void clearStats()
	{
		synchronized (mutex)
		{
			minTs.set(Long.MAX_VALUE);
			maxTs.set(Long.MIN_VALUE);
			stats.clear();
			histo = TDigest.createDigest(100);
		}
	}

	public String getTxName()
	{
		return txName;
	}

	public double getMixPct()
	{
		return mixPct;
	}

	public final TxSummary getStats()
	{
		synchronized (mutex)
		{
			TxSummary txSummary = new TxSummary(txName, mixPct, stats.getN(), minTs.get(), maxTs.get(), getWarningCount(), getErrorCount(), stats.getMin(), stats.getMax(), stats.getMean(), stats.getSum(), stats.getStandardDeviation(), histo);
			return txSummary;
		}
	}
}
