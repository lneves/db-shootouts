package org.caudexorigo.oltp1.tx.trade_lookup;

import java.sql.Array;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.caudexorigo.oltp1.generator.TxInputGenerator;
import org.caudexorigo.perf.ErrorAnalyser;
import org.caudexorigo.perf.StatsCollector;
import org.caudexorigo.perf.TxBase;
import org.caudexorigo.perf.TxOutput;
import org.caudexorigo.perf.XmlProperties;
import org.caudexorigo.perf.db.SqlContext;
import org.caudexorigo.perf.db.SqlEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

public class TxTradeLookup extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxTradeLookup.class);

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final TxInputGenerator txInputGen;
	private final String get_trade_info;
	private final String frm2;
	private final String frm3;
	private final String frm4;
	private final String get_trade_history;

	public TxTradeLookup(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);

		this.txInputGen = txInputGen;
		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();
		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_lookup/mssql_trade_lookup_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_lookup/pgsql_trade_lookup_frm1.xml");

		Map<SqlEngine, String> queryMapFrm2 = new HashMap<SqlEngine, String>();
		queryMapFrm2.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_lookup/trade_lookup_frm2.xml");
		queryMapFrm2.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_lookup/trade_lookup_frm2.xml");

		Map<SqlEngine, String> queryMapFrm3 = new HashMap<SqlEngine, String>();
		queryMapFrm3.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_lookup/trade_lookup_frm3.xml");
		queryMapFrm3.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_lookup/trade_lookup_frm3.xml");

		Map<SqlEngine, String> queryMapFrm4 = new HashMap<SqlEngine, String>();
		queryMapFrm4.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_lookup/trade_lookup_frm4.xml");
		queryMapFrm4.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_lookup/trade_lookup_frm4.xml");

		Map<SqlEngine, String> queryMapTh = new HashMap<SqlEngine, String>();
		queryMapTh.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_lookup/mssql_trade_lookup_trade_history.xml");
		queryMapTh.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_lookup/pgsql_trade_lookup_trade_history.xml");

		Map<String, String> sqlFrm1Map = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm2Map = XmlProperties.read(queryMapFrm2.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm3Map = XmlProperties.read(queryMapFrm3.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm4Map = XmlProperties.read(queryMapFrm4.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlTHMap = XmlProperties.read(queryMapTh.get(sqlCtx.getSqlEngine()));

		get_trade_info = sqlFrm1Map.get("get_trade_info");
		frm2 = sqlFrm2Map.get("frm2");
		frm3 = sqlFrm3Map.get("frm3");
		frm4 = sqlFrm4Map.get("frm4");

		get_trade_history = sqlTHMap.get("get_trade_history");
	}

	@Override
	protected final TxOutput run()
	{
		final TxTradeLookupOutput txOutput = new TxTradeLookupOutput();

		final TxTradeLookupInput txInput = txInputGen.generateTradeLookupInput();
		// System.out.println(txInput);

		txOutput.setFrameExecuted(txInput.getFrameToexecute());

		try (Connection con = sql2o.beginTransaction(sqlCtx.getIsolationLevel()))
		{
			if (txInput.getFrameToexecute() == 1)
			{
				executeFrame1(con, txInput, txOutput);
			}
			else if (txInput.getFrameToexecute() == 2)
			{
				executeFrame2(txOutput, txInput, con);
			}
			else if (txInput.getFrameToexecute() == 3)
			{
				executeFrame3(con, txInput, txOutput);
			}
			else
			{
				executeFrame4(con, txInput, txOutput);
			}

			con.commit();
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);
			txOutput.setStatus(-1);
		}

		return txOutput;
	}

	private void executeFrame1(final Connection con, final TxTradeLookupInput txInput, final TxTradeLookupOutput txOutput)
	{
		Query frm1Query = con.createQuery(get_trade_info);

		if (sqlCtx.getSqlEngine() == SqlEngine.POSTGRESQL)
		{
			try
			{
				Array tradeIds = con.getJdbcConnection().createArrayOf("bigint", ArrayUtils.toObject(txInput.getTradeId()));
				frm1Query.addParameter("trade_ids", tradeIds);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
		else if (sqlCtx.getSqlEngine() == SqlEngine.MSSQL)
		{
			frm1Query.addParameter("trade_ids", StringUtils.join(txInput.getTradeId(), ','));
		}

		List<Map<String, Object>> lstTrades = frm1Query
				.addParameter("max_trades", txInput.getMaxTrades())
				.executeAndFetchTable()
				.asList();

		if (lstTrades.size() != 20)
		{
			System.err.println("TxTradeLookup.executeFrame1.tradeSize: " + lstTrades.size());
			txOutput.setStatus(-611);
		}

		txOutput.setLstTradesFrm1(lstTrades);

		List<Map<String, Object>> historyTrades = fetchTradeHistory(con, lstTrades);
		txOutput.setLstTradesHistory(historyTrades);
	}

	private void executeFrame2(TxTradeLookupOutput txOutput, final TxTradeLookupInput txInput, Connection con)
	{
		List<Map<String, Object>> lstTrades = con.createQuery(frm2)
				.addParameter("ca_id", txInput.getAcctId())
				.addParameter("start_dts", txInput.getStartTradeDts())
				.addParameter("end_dts", txInput.getEndTradeDts())
				.addParameter("max_trades", txInput.getMaxTrades())
				.executeAndFetchTable()
				.asList();

		if ((lstTrades.size() > txInput.getMaxTrades()))
		{
			txOutput.setStatus(-621);
		}
		else if (lstTrades.isEmpty())
		{
			txOutput.setStatus(621);
		}

		txOutput.setLstTradesFrm2(lstTrades);

		List<Map<String, Object>> historyTrades = fetchTradeHistory(con, lstTrades);
		txOutput.setLstTradesHistory(historyTrades);
	}

	private void executeFrame3(Connection con, TxTradeLookupInput txInput, TxTradeLookupOutput txOutput)
	{
		List<Map<String, Object>> lstTrades = con.createQuery(frm3)
				.addParameter("symbol", txInput.getSymbol())
				.addParameter("start_dts", txInput.getStartTradeDts())
				.addParameter("end_dts", txInput.getEndTradeDts())
				.addParameter("max_trades", txInput.getMaxTrades())
				.executeAndFetchTable()
				.asList();

		if ((lstTrades.size() > txInput.getMaxTrades()))
		{
			txOutput.setStatus(-631);
		}
		else if (lstTrades.isEmpty())
		{
			txOutput.setStatus(631);
		}

		txOutput.setLstTradesFrm3(lstTrades);

		List<Map<String, Object>> historyTrades = fetchTradeHistory(con, lstTrades);
		txOutput.setLstTradesHistory(historyTrades);
	}

	private void executeFrame4(Connection con, TxTradeLookupInput txInput, TxTradeLookupOutput txOutput)
	{
		List<Map<String, Object>> lstTrades = con.createQuery(frm4)
				.addParameter("ca_id", txInput.getAcctId())
				.addParameter("start_dts", txInput.getStartTradeDts())
				.executeAndFetchTable()
				.asList();

		if ((lstTrades.size() < 1) || (lstTrades.size() > 20))
		{
			txOutput.setStatus(-631);
		}

		txOutput.setLstTradesFrm4(lstTrades);
	}

	private List<Map<String, Object>> fetchTradeHistory(final Connection con, final List<Map<String, Object>> lstTrades)
	{
		Query thQuery = con.createQuery(get_trade_history);

		if (sqlCtx.getSqlEngine() == SqlEngine.POSTGRESQL)
		{
			Long[] tids = lstTrades.stream()
					.map(m -> ((Long) m.get("t_id")).longValue())
					.toArray(Long[]::new);

			try
			{
				Array tradeIds = con.getJdbcConnection().createArrayOf("bigint", tids);
				thQuery.addParameter("trade_ids", tradeIds);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
		else if (sqlCtx.getSqlEngine() == SqlEngine.MSSQL)
		{
			String tradeIds = lstTrades.stream()
					.map(m -> ((Long) m.get("t_id")).toString())
					.collect(Collectors.joining(","));

			thQuery.addParameter("trade_ids", tradeIds);
		}

		return thQuery.executeAndFetchTable().asList();
	}
}