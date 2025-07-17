package org.caudexorigo.perf;

import java.io.PrintWriter;
import java.util.Date;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class ConsoleReportWriter implements Consumer<TxRunSummary>
{
	private static FastDateFormat isoDateFmt = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT;

	@Override
	public void accept(final TxRunSummary trun)
	{
		final long globalMinTs = trun.getTxStats().stream().mapToLong(t -> t.getMinTs()).min().getAsLong();
		final long globalMaxTs = trun.getTxStats().stream().mapToLong(t -> t.getMaxTs()).max().getAsLong();
		final double globalElapsed = ((double) (globalMaxTs - globalMinTs) / 1000000.0);
		final long totalTx = trun.getTxStats().stream().mapToLong(t -> t.getCount()).sum();

		try (PrintWriter pw = new PrintWriter(System.out))
		{
			pw.printf("#SUT%n%n%s%n%n", trun.getSutInfo());
			pw.printf("Date: %s%n%n", isoDateFmt.format(new Date()));

			pw.println(StringUtils.leftPad("----------- Response Time(ms) ----------", 85, " "));
			pw.print(StringUtils.rightPad("Transaction", 20, " "));
			pw.print(StringUtils.leftPad("Target(%)", 10, " "));
			pw.print(StringUtils.leftPad("Actual(%)", 10, " "));
			pw.print(StringUtils.leftPad("Mean", 9, " "));
			pw.print(StringUtils.leftPad("StdDev", 9, " "));
			pw.print(StringUtils.leftPad("Min", 9, " "));
			pw.print(StringUtils.leftPad("Max", 9, " "));
			pw.print(StringUtils.leftPad("Pct90", 9, " "));
			pw.print(StringUtils.leftPad("Count", 10, " "));
			pw.print(StringUtils.leftPad("Warnings", 10, " "));
			pw.print(StringUtils.leftPad("Errors", 8, " "));
			pw.println(StringUtils.leftPad("Rate(tx/sec)", 14, " "));

			trun.getTxStats().stream().forEach(test -> {

				String tTargetMixPct = String.format("%.2f", test.getTargetMixPct() * 100.0);
				String tActualMixPct = String.format("%.2f", test.getActualMixPct(totalTx) * 100.0);
				String tMean = String.format("%.2f", test.getMean());
				String tStdDev = String.format("%.2f", test.getStdDev());
				String tMin = String.format("%.2f", test.getMin());
				String tMax = String.format("%.2f", test.getMax());
				String tP90 = String.format("%.2f", test.getQuantile(0.9));
				String tCount = String.format("%s", test.getCount());
				String tWCount = String.format("%s", test.getWarningCount());
				String tECount = String.format("%s", test.getErroCount());
				String tRate = String.format("%.2f", test.getCount() / test.getTxElapsedTime() * 1000.0);

				pw.print(StringUtils.rightPad(test.getTxName(), 20, " "));
				pw.print(StringUtils.leftPad(tTargetMixPct, 10, " "));
				pw.print(StringUtils.leftPad(tActualMixPct, 10, " "));
				pw.print(StringUtils.leftPad(tMean, 9, " "));
				pw.print(StringUtils.leftPad(tStdDev, 9, " "));
				pw.print(StringUtils.leftPad(tMin, 9, " "));
				pw.print(StringUtils.leftPad(tMax, 9, " "));
				pw.print(StringUtils.leftPad(tP90, 9, " "));
				pw.print(StringUtils.leftPad(tCount, 10, " "));
				pw.print(StringUtils.leftPad(tWCount, 10, " "));
				pw.print(StringUtils.leftPad(tECount, 8, " "));
				pw.println(StringUtils.leftPad(tRate, 14, " "));
			});

			pw.printf("%nRun time: %.0f sec.%n", globalElapsed/1000);
			pw.printf("Clients: %d%n", trun.getNumClients());
			pw.printf("Total Tx: %d%n", totalTx);
			pw.printf("Tx Rate: %.2f tx/sec%n", ((double) totalTx) / (globalElapsed / 1000.0));

			pw.flush();
		}
		catch (Throwable tw)
		{
			throw new RuntimeException(tw);
		}
	}
}