package org.caudexorigo.oltp1.tx.market_watch;

import java.util.HashMap;
import java.util.Map;

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
import org.sql2o.Sql2o;

public class TxMarketWatch extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxMarketWatch.class);

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final TxInputGenerator txInputGen;
	private final String get_pct_change_by_account;
	private final String get_pct_change_by_customer;
	private final String get_pct_change_by_industry;

	public TxMarketWatch(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);
		this.txInputGen = txInputGen;

		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();
		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/market_watch/market_watch_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/market_watch/market_watch_frm1.xml");

		Map<String, String> sqlMapFrm1 = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		get_pct_change_by_account = sqlMapFrm1.get("get_pct_change_by_account");
		get_pct_change_by_customer = sqlMapFrm1.get("get_pct_change_by_customer");
		get_pct_change_by_industry = sqlMapFrm1.get("get_pct_change_by_industry");
	}

	@Override
	protected final TxOutput run()
	{
		TxMarketWatchOutput txOutput = new TxMarketWatchOutput();

		final TxMarketWatchInput txInput = txInputGen.generateMarketWatchInput();

		if ((txInput.getAcctId() == 0) && (txInput.getCId() == 0) && StringUtils.isBlank(txInput.getIndustryName()))
		{
			txOutput.setStatus(-411);
			return txOutput;
		}

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

	private void executeFrame1(final Connection con, final TxMarketWatchInput txInput, final TxMarketWatchOutput txOutput)
	{
		double pct_change;

		if (txInput.getCId() != 0)
		{
			pct_change = con.createQuery(get_pct_change_by_customer)
					.addParameter("cust_id", txInput.getCId())
					.addParameter("start_date", txInput.getStartDay())
					.executeScalar(Double.class);
		}
		else if (StringUtils.isNotBlank(txInput.getIndustryName()))
		{
			pct_change = con.createQuery(get_pct_change_by_industry)
					.addParameter("industry_name", txInput.getIndustryName())
					.addParameter("start_date", txInput.getStartDay())
					.addParameter("starting_co_id", txInput.getStartingCoId())
					.addParameter("ending_co_id", txInput.getEndingCoId())
					.executeScalar(Double.class);
		}
		else if (txInput.getAcctId() != 0)
		{
			pct_change = con.createQuery(get_pct_change_by_account)
					.addParameter("acct_id", txInput.getAcctId())
					.addParameter("start_date", txInput.getStartDay())
					.executeScalar(Double.class);
		}
		else
		{
			throw new IllegalArgumentException("Bad input data in the Market-Watch transaction");
		}

		txOutput.setPctChange(pct_change);
	}
}