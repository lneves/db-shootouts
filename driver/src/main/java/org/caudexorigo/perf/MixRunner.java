package org.caudexorigo.perf;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;

public class MixRunner
{
	public static TxRunSummary runTxMix(final String sutInfo, final List<TxBase> txMix, final List<StatsCollector> collectors, final int numClients, final long runDuration)
	{
		ExecutorService clients = ThreadPoolBuilder.newThreadPool(numClients, "run-loader");
		RandomUtils rnd = RandomUtils.insecure();

		collectors.forEach(c -> c.clearStats());

		double mixPctSum = txMix
				.stream()
				.mapToDouble(e -> e.getMixPct()).sum();

		Assert.isInRange("mixPctSum", mixPctSum, 0.0, 1.0);

		final double factor = 1.0 / mixPctSum; // for the cases where all the sum of all tx < 100%
		final ConcurrentNavigableMap<Double, TxBase> txMixRange = new ConcurrentSkipListMap<Double, TxBase>();

		// populate a "range" map for the transactions
		TxBase firstTx = new TxVoid(new StatsCollector("TxVoid", 1.0 - mixPctSum));
		txMixRange.put(0.0, firstTx);

		txMix.forEach(tx -> {

			Entry<Double, TxBase> lastTx = txMixRange.lastEntry();

			double nextKey = lastTx.getKey() + (lastTx.getValue().getMixPct());

			txMixRange.put(nextKey, tx);
		});

		final Runnable clientTx = () -> {

			// pick the nearest tx with a key <= than a random number
			double p = rnd.randomDouble(0.0, 1.0);
			TxBase tx = txMixRange.floorEntry(p).getValue();

			tx.execute();
		};

		final AtomicBoolean isRunning = new AtomicBoolean(true);
		final ScheduledExecutorService schedExec = Executors.newScheduledThreadPool(1);

		schedExec.schedule(() -> isRunning.set(false), runDuration, TimeUnit.SECONDS);

		long tsms = runDuration * 1000 / 50;
		schedExec.scheduleWithFixedDelay(() -> System.out.print("*"), tsms, tsms, TimeUnit.MILLISECONDS);

		while (isRunning.get())
		{
			clients.execute(clientTx);
		}

		schedExec.shutdownNow();
		clients.shutdown();
		try
		{
			clients.awaitTermination(runDuration, TimeUnit.SECONDS);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}

		TxRunSummary obj = new TxRunSummary(sutInfo, numClients);

		List<TxSummary> txStats = collectors
				.stream()
				.map(t -> t.getStats())
				.collect(Collectors.toList());

		txStats.forEach(t -> {
			t.setTargetMixPct(t.getTargetMixPct() * factor);
			obj.addTxSummary(t);
		});

		return obj;
	}
}
