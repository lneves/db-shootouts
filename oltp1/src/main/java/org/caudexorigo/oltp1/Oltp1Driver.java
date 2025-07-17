package org.caudexorigo.oltp1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.caudexorigo.oltp1.generator.TxInputGenerator;
import org.caudexorigo.oltp1.tx.broker_volume.TxBrokerVolume;
import org.caudexorigo.oltp1.tx.customer_position.TxCustomerPosition;
import org.caudexorigo.oltp1.tx.market_watch.TxMarketWatch;
import org.caudexorigo.oltp1.tx.security_detail.TxSecurityDetail;
import org.caudexorigo.oltp1.tx.trade_lookup.TxTradeLookup;
import org.caudexorigo.oltp1.tx.trade_order.TxTradeOrder;
import org.caudexorigo.oltp1.tx.trade_result.TxTradeResult;
import org.caudexorigo.oltp1.tx.trade_status.TxTradeStatus;
import org.caudexorigo.perf.ConsoleReportWriter;
import org.caudexorigo.perf.ErrorAnalyser;
import org.caudexorigo.perf.MixRunner;
import org.caudexorigo.perf.StatsCollector;
import org.caudexorigo.perf.TxBase;
import org.caudexorigo.perf.TxRunSummary;
import org.caudexorigo.perf.db.SqlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;

public class Oltp1Driver {
	private static Logger log = LoggerFactory.getLogger(Oltp1Driver.class);

	public static void main(String[] args) {
		try {
			BenchmarkArgs cargs = CliFactory.parseArguments(BenchmarkArgs.class, args);

			final int clients = cargs.getClients();
			final long runDuration = cargs.getDuration();

			final String db = cargs.getEngine();
			int port = cargs.getPort();

			SqlContext sqlCtx;

			if ("mssql".equals(db)) {
				if (port == 0) {
					port = 1433;
				}

				String jdbcuri = String.format(
						"jdbc:sqlserver://%s:%s;database=tpce;user=%s;password=%s;disableStatementPooling=false;serverPreparedStatementDiscardThreshold=1000;statementPoolingCacheSize=500;encrypt=false;trustServerCertificate=true",
						cargs.getServer(), port, cargs.getUsername(), cargs.getPassword());
				sqlCtx = SqlContext.buildSqlContext(jdbcuri, "com.microsoft.sqlserver.jdbc.SQLServerDriver", clients);
			} else if ("pgsql".equals(db)) {
				if (port == 0) {
					port = 5432;
				}

				String jdbcuri = String.format("jdbc:postgresql://%s:%s/tpce?user=%s&password=%s", cargs.getServer(),
						port, cargs.getUsername(), cargs.getPassword());
				sqlCtx = SqlContext.buildSqlContext(jdbcuri, "org.postgresql.Driver", clients);
			} else {
				throw new IllegalArgumentException("unknown db engine");
			}

			final String dbInfo = getDbInfo(sqlCtx);

			final TxInputGenerator txInputGen = new TxInputGenerator(sqlCtx);

//			# Read-Write Transactions
//			    Trade-Order (TO)
//			    Trade-Result (TR)
//			    Trade-Update (TU)
//			    Market-Feed (MF)
//			    Customer-Update (CU)
//			    Broker-Update (BU)
//			    Company-Update (COU)
//			    Daily-Market (DM)
//			    Data-Maintenance (DMt)
//
//			# Read-Only Transactions
//			    Trade-Status (TS) *
//			    Trade-Lookup (TL) *
//			    Customer-Position (CP) *
//			    Customer-Account-Information (CA)
//			    Customer-Account-Summary (CAS)
//			    Customer-Watch-List (CW)
//			    Security-Detail (SD)

			Map<String, StatsCollector> collectors = new LinkedHashMap<>();
			collectors.put("Broker-Volume", new StatsCollector("Broker-Volume", 0.049));
			collectors.put("Customer-Position", new StatsCollector("Customer-Position", 0.13)); //
			collectors.put("Market-Watch", new StatsCollector("Market-Watch", 0.18));
			collectors.put("Security-Detail", new StatsCollector("Security-Detail", 0.14));
			collectors.put("Trade-Lookup", new StatsCollector("Trade-Lookup", 0.08)); //
			collectors.put("Trade-Status", new StatsCollector("Trade-Status", 0.19)); //
			// collectors.put("Trade-Order", new StatsCollector("Trade-Order", 0.101));
			// collectors.put("Trade-Result", new StatsCollector("Trade-Result", 0.10));
			// collectors.put("Market-Feed", new StatsCollector("Market-Feed", 0.01));
			// collectors.put("Trade-Update", new StatsCollector("Trade-Update", 0.02));

			List<StatsCollector> colList = new ArrayList<>(collectors.values());

			final List<TxBase> txMix = new ArrayList<>();

			// txMix.add(new TxBaseLine(sqlCtx, 1.0));
			txMix.add(new TxBrokerVolume(txInputGen, sqlCtx, collectors.get("Broker-Volume")));
			txMix.add(new TxCustomerPosition(txInputGen, sqlCtx, collectors.get("Customer-Position")));
			txMix.add(new TxMarketWatch(txInputGen, sqlCtx, collectors.get("Market-Watch")));
			txMix.add(new TxSecurityDetail(txInputGen, sqlCtx, collectors.get("Security-Detail")));
			txMix.add(new TxTradeLookup(txInputGen, sqlCtx, collectors.get("Trade-Lookup")));
			txMix.add(new TxTradeStatus(txInputGen, sqlCtx, collectors.get("Trade-Status")));
			// txMix.add(new TxTradeOrder(txInputGen, sqlCtx, collectors.get("Trade-Order"),
			// new TxTradeResult(sqlCtx, collectors.get("Trade-Result"))));

			System.out.println("warming up");
			MixRunner.runTxMix(dbInfo, txMix, colList, clients, runDuration / 2); // warmup

			System.out.println("\nrunning transaction mix");
			TxRunSummary runSummary = MixRunner.runTxMix(dbInfo, txMix, colList, clients, runDuration);

			System.out.println("\n");

			(new ConsoleReportWriter()).accept(runSummary);
			// (new HtmlReportWriter("./target/",
			// sqlCtx.getSqlEngine().getAbrev())).accept(runSummary);

			System.exit(0);
		} catch (ArgumentValidationException a) {
			ErrorAnalyser.shutdown(a);
		} catch (Throwable t) {
			ErrorAnalyser.shutdown(t, log);
		}
	}

	private static String getDbInfo(SqlContext sqlCtx) {
		try (Connection con = sqlCtx.getSql2o().open()) {
			Query tx = con.createQuery(sqlCtx.getSqlEngine().getInfoQuery());

			String dbInfo0 = tx.executeAndFetchFirst(String.class);

			return dbInfo0;
		}
	}
}