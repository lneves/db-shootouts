package transactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.caudexorigo.oltp1.BenchmarkArgs;
import org.caudexorigo.oltp1.generator.TxInputGenerator;
import org.caudexorigo.oltp1.tx.broker_volume.TxBrokerVolume;
import org.caudexorigo.oltp1.tx.customer_position.TxCustomerPosition;
import org.caudexorigo.oltp1.tx.market_watch.TxMarketWatch;
import org.caudexorigo.oltp1.tx.security_detail.TxSecurityDetail;
import org.caudexorigo.oltp1.tx.trade_lookup.TxTradeLookup;
import org.caudexorigo.oltp1.tx.trade_order.TxTradeOrder;
import org.caudexorigo.oltp1.tx.trade_result.TxTradeResult;
import org.caudexorigo.oltp1.tx.trade_status.TxTradeStatus;
import org.caudexorigo.perf.ErrorAnalyser;
import org.caudexorigo.perf.StatsCollector;
import org.caudexorigo.perf.TxBase;
import org.caudexorigo.perf.TxOutput;
import org.caudexorigo.perf.db.SqlContext;
import org.slf4j.LoggerFactory;

import com.lexicalscope.jewel.cli.CliFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Sandbox
{
	public static void main(String[] args)
	{
		try
		{
			BenchmarkArgs cargs = CliFactory.parseArguments(BenchmarkArgs.class, args);
			final String db = cargs.getEngine();
			int port = cargs.getPort();

			SqlContext sqlCtx;

			int clients = 1;
			if ("mssql".equals(db))
			{
				if (port == 0)
				{
					port = 1433;
				}

				// String jdbcuri = String.format("jdbc:jtds:sqlserver://%s:%s/tpce;user=%s;password=%s;prepareSQL=3;cacheMetaData=true", cargs.getServer(), port, cargs.getUsername(), cargs.getPassword());
				// sqlCtx = SqlContext.buildSqlContext(jdbcuri, "net.sourceforge.jtds.jdbc.Driver", clients);

				String jdbcuri = String.format("jdbc:sqlserver://%s:%s;database=tpce;user=%s;password=%s;disableStatementPooling=false;serverPreparedStatementDiscardThreshold=1000;statementPoolingCacheSize=500", cargs.getServer(), port, cargs.getUsername(), cargs.getPassword());
				sqlCtx = SqlContext.buildSqlContext(jdbcuri, "com.microsoft.sqlserver.jdbc.SQLServerDriver", clients);
			}
			else if ("pgsql".equals(db))
			{
				if (port == 0)
				{
					port = 5432;
				}

				String jdbcuri = String.format("jdbc:postgresql://%s:%s/tpce?user=%s&password=%s", cargs.getServer(), port, cargs.getUsername(), cargs.getPassword());
				sqlCtx = SqlContext.buildSqlContext(jdbcuri, "org.postgresql.Driver", clients);
			}
			else
			{
				throw new IllegalArgumentException("unknown db engine");
			}

			BlockingQueue<Map<String, Object>> mq = new LinkedBlockingQueue<>();
			TxInputGenerator txInputGen = new TxInputGenerator(sqlCtx);

			Map<String, StatsCollector> collectors = new HashMap<>();
			collectors.put("Broker-Volume", new StatsCollector("Broker-Volume", 1.0));
			collectors.put("Customer-Position", new StatsCollector("Customer-Position", 1.0));
			collectors.put("Market-Watch", new StatsCollector("Market-Watch", 1.0));
			collectors.put("Security-Detail", new StatsCollector("Security-Detail", 1.0));
			collectors.put("Trade-Lookup", new StatsCollector("Trade-Lookup", 1.0));
			collectors.put("Trade-Status", new StatsCollector("Trade-Status", 1.0));
			collectors.put("Trade-Order", new StatsCollector("Trade-Order", 1.0));
			collectors.put("Trade-Result", new StatsCollector("Trade-Result", 1.0));

			List<StatsCollector> colList = new ArrayList<>(collectors.values());

			final List<TxBase> ltx = new ArrayList<>();
			ltx.add(new TxBrokerVolume(txInputGen, sqlCtx, collectors.get("Broker-Volume")));
			ltx.add(new TxCustomerPosition(txInputGen, sqlCtx, collectors.get("Customer-Position")));
			ltx.add(new TxMarketWatch(txInputGen, sqlCtx, collectors.get("Market-Watch")));
			ltx.add(new TxSecurityDetail(txInputGen, sqlCtx, collectors.get("Market-Watch")));
			ltx.add(new TxTradeLookup(txInputGen, sqlCtx, collectors.get("Trade-Lookup")));
			ltx.add(new TxTradeStatus(txInputGen, sqlCtx, collectors.get("Trade-Status")));
			ltx.add(new TxTradeOrder(txInputGen, sqlCtx, collectors.get("Trade-Order"), new TxTradeResult(sqlCtx, collectors.get("Trade-Result"))));

			Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			root.setLevel(Level.DEBUG);

			while (true)
			{
				ltx.forEach(tx -> {
					TxOutput txOut = tx.execute();

					System.out.println("#################################################################");

					sleep(500);
				});

			}
		}
		catch (Throwable t)
		{
			ErrorAnalyser.shutdown(t);
		}
	}

	private static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}