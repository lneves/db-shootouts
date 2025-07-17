package org.caudexorigo.oltp1.tx.trade_status;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.sql2o.Sql2o;

public class TxTradeStatus extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxTradeStatus.class);

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final TxInputGenerator txInputGen;
	private final String get_trade_name;
	private final String get_trade_status;

	public TxTradeStatus(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);

		this.txInputGen = txInputGen;
		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();

		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_status/mssql_trade_status_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_status/pgsql_trade_status_frm1.xml");

		Map<String, String> sqlMapFrm1 = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		get_trade_name = sqlMapFrm1.get("get_trade_name");
		get_trade_status = sqlMapFrm1.get("get_trade_status");
	}

	@Override
	protected final TxOutput run()
	{
		TxTradeStatusOutput txOutput = new TxTradeStatusOutput();

		final TxTradeStatusInput txInput = txInputGen.generateTradeStatusInput();

		try (Connection con = sql2o.beginTransaction(sqlCtx.getIsolationLevel()))
		{
			executeFrame1(con, txInput, txOutput);

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

	private void executeFrame1(final Connection con, final TxTradeStatusInput txInput, final TxTradeStatusOutput txOutput)
	{
		Map<String, Object> tradeName = con.createQuery(get_trade_name)
				.addParameter("acct_id", txInput.getAcctId())
				.executeAndFetchTable().asList().stream().findFirst().orElse(Collections.emptyMap());

		List<Map<String, Object>> tradeStatus = con.createQuery(get_trade_status)
				.addParameter("acct_id", txInput.getAcctId())
				.executeAndFetchTable()
				.asList();

		txOutput.setTradeName(tradeName);
		txOutput.setTradeStatus(tradeStatus);
		txOutput.setNumFound(tradeStatus.size());
	}
}