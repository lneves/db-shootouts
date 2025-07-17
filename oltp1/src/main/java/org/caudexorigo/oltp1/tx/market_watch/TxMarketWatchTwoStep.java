package org.caudexorigo.oltp1.tx.market_watch;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.caudexorigo.oltp1.generator.TxInputGenerator;
import org.caudexorigo.perf.ErrorAnalyser;
import org.caudexorigo.perf.StatsCollector;
import org.caudexorigo.perf.TxBase;
import org.caudexorigo.perf.TxOutput;
import org.caudexorigo.perf.XmlProperties;
import org.caudexorigo.perf.db.SqlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class TxMarketWatchTwoStep extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxMarketWatchTwoStep.class);

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final TxInputGenerator txInputGen;

	private final String get_new_mkt_cap_by_account;
	private final String get_new_mkt_cap_by_customer;
	private final String get_new_mkt_cap_by_industry;
	private final String get_old_mkt_cap_by_account;
	private final String get_old_mkt_cap_by_customer;
	private final String get_old_mkt_cap_by_industry;

	public TxMarketWatchTwoStep(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);
		this.txInputGen = txInputGen;

		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		String pfname = String.format("/org/caudexorigo/oltp1/tx/market_watch/%s_market_watch_frm1.xml", sqlCtx.getSqlEngine().getAbrev());
		Map<String, String> sqlMap = XmlProperties.read(pfname);
		get_new_mkt_cap_by_account = sqlMap.get("get_new_mkt_cap_by_account");
		get_new_mkt_cap_by_customer = sqlMap.get("get_new_mkt_cap_by_customer");
		get_new_mkt_cap_by_industry = sqlMap.get("get_new_mkt_cap_by_industry");
		get_old_mkt_cap_by_account = sqlMap.get("get_old_mkt_cap_by_account");
		get_old_mkt_cap_by_customer = sqlMap.get("get_old_mkt_cap_by_customer");
		get_old_mkt_cap_by_industry = sqlMap.get("get_old_mkt_cap_by_industry");
	}

	@Override
	protected final TxOutput run()
	{
		TxMarketWatchOutput txOutput = new TxMarketWatchOutput();

		final TxMarketWatchInput frm1Input = txInputGen.generateMarketWatchInput();

		if ((frm1Input.getAcctId() == 0) && (frm1Input.getCId() == 0) && StringUtils.isBlank(frm1Input.getIndustryName()))
		{
			txOutput.setStatus(-411);
			return txOutput;
		}

		try (Connection con = sql2o.beginTransaction(sqlCtx.getIsolationLevel()))
		{
			executeFrame1(con, frm1Input, txOutput);

			con.commit();
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);
			txOutput.setStatus(-1);
		}

		if (log.isDebugEnabled())
		{
			log.debug(txOutput.toString());
		}

		return txOutput;
	}

	private void executeFrame1(final Connection con, final TxMarketWatchInput frm1Input, final TxMarketWatchOutput txOutput)
	{
		if (log.isDebugEnabled())
		{
			log.debug(frm1Input.toString());
		}

		double old_mkt_cap = 0.0, new_mkt_cap = 0.0;
		if (frm1Input.getCId() != 0)
		{
			old_mkt_cap = con.createQuery(get_old_mkt_cap_by_customer)
					.addParameter("cust_id", frm1Input.getCId())
					.addParameter("start_date", frm1Input.getStartDay())
					.executeScalar(Double.class);

			new_mkt_cap = con.createQuery(get_new_mkt_cap_by_customer)
					.addParameter("cust_id", frm1Input.getCId())
					.executeScalar(Double.class);

		}
		else if (StringUtils.isNotBlank(frm1Input.getIndustryName()))
		{
			old_mkt_cap = con.createQuery(get_old_mkt_cap_by_industry)
					.addParameter("industry_name", frm1Input.getIndustryName())
					.addParameter("start_date", frm1Input.getStartDay())
					.addParameter("starting_co_id", frm1Input.getStartingCoId())
					.addParameter("ending_co_id", frm1Input.getEndingCoId())
					.executeScalar(Double.class);

			new_mkt_cap = con.createQuery(get_new_mkt_cap_by_industry)
					.addParameter("industry_name", frm1Input.getIndustryName())
					.addParameter("starting_co_id", frm1Input.getStartingCoId())
					.addParameter("ending_co_id", frm1Input.getEndingCoId())
					.executeScalar(Double.class);
		}
		else if (frm1Input.getAcctId() != 0)
		{
			old_mkt_cap = con.createQuery(get_old_mkt_cap_by_account)
					.addParameter("acct_id", frm1Input.getAcctId())
					.addParameter("start_date", frm1Input.getStartDay())
					.executeScalar(Double.class);

			new_mkt_cap = con.createQuery(get_new_mkt_cap_by_account)
					.addParameter("acct_id", frm1Input.getAcctId())
					.executeScalar(Double.class);
		}
		else
		{
			throw new IllegalArgumentException("Bad input data in the Market-Watch transaction");
		}

		double pct_change;

		if (old_mkt_cap != 0)
		{
			// value of 0.00 for pct_change is valid
			pct_change = 100 * (new_mkt_cap / old_mkt_cap - 1);
		}
		else
		{
			// no rows found, this can happen rarely when an account has no holdings
			pct_change = 0.0;
		}

		txOutput.setPctChange(pct_change);
	}
}