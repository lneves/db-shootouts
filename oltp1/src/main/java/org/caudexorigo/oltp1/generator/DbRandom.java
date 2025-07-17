package org.caudexorigo.oltp1.generator;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;

public class DbRandom
{
	private static final RandomUtils rnd = RandomUtils.insecure();
	
	public int rndInt(int max)
	{
		return rnd.randomInt(0, max);
	}

	public int rndIntRange(int startInclusive, int endExclusive)
	{
		return rnd.randomInt(startInclusive, endExclusive);
	}

	public long rndLongRange(final long startInclusive, final long endExclusive)
	{
		return rnd.randomLong(startInclusive, endExclusive);
	}

	public double rndDoubleRange(double startInclusive, double endInclusive)
	{
		return rnd.randomDouble(startInclusive, endInclusive);
	}

	// returns TRUE or FALSE, with the chance of TRUE being as specified by (percent)
	public final boolean rndPercent(int percent)
	{
		return (rndIntRange(1, 100) <= percent);
	}

	public int rndPercentage()
	{
		return rndIntRange(0, 100);
	}

	public <T> T rndChoice(List<T> lst)
	{
		return lst.get(rndIntRange(0, lst.size()));
	}

	public long nonUniformRandom(long min, long max, int A, int s)
	{
		return (((rndLongRange(min, max) | (rndLongRange(0, A) << s)) % (max - min + 1)) + min);
	}

	public double rndDoubleRangeInc(double min, double max, double incr)
	{
		long width = (long) ((max - min) / incr);
		return min + ((double) rndLongRange(0, width) * incr);
	}
}