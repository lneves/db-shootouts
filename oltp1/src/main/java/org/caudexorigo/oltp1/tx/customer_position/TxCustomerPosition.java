package org.caudexorigo.oltp1.tx.customer_position;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class TxCustomerPosition extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxCustomerPosition.class);
	private static final int max_acct_len = 10;
	private static final int max_hist_len = 30;

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final String get_customer_by_cid;
	private final String get_custommer_accounts;
	private final String get_trade_history;
	private final String get_customer_by_taxid;
	private final TxInputGenerator txInputGen;

	public TxCustomerPosition(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);
		this.txInputGen = txInputGen;
		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();

		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/customer_position/mssql_customer_position_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/customer_position/pgsql_customer_position_frm1.xml");

		Map<String, String> sqlMapFrm1 = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		get_customer_by_cid = sqlMapFrm1.get("get_customer_by_cid");
		get_custommer_accounts = sqlMapFrm1.get("get_custommer_accounts");
		get_trade_history = sqlMapFrm1.get("get_trade_history");
		get_customer_by_taxid = sqlMapFrm1.get("get_customer_by_taxid");
	}

	@Override
	protected final TxOutput run()
	{
		final TxCustomerPositionInput txInput = txInputGen.generateCustomerPositionInput();

		final TxCustomerPositionOutput txOutput = new TxCustomerPositionOutput();

		try (Connection con = sql2o.beginTransaction(sqlCtx.getIsolationLevel()))
		{
			executeFrame1(con, txInput, txOutput);

			if (txOutput.getStatus() > -1)
			{
				if (txInput.getHistory())
				{
					executeFrame2(con, txInput, txOutput);
				}
				else
				{
					executeFrame3(con);
				}
			}
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);
			txOutput.setStatus(-1);
		}

		return txOutput;
	}

	private void executeFrame1(final Connection con, final TxCustomerPositionInput frm1Input, final TxCustomerPositionOutput txOutput)
	{
		long customerId = getCustomerId(con, frm1Input);

		Map<String, Object> customer = con
				.createQuery(get_customer_by_cid)
				.addParameter("c_id", customerId)
				.executeAndFetchTable()
				.asList().stream().findFirst().orElse(null);

		List<Map<String, Object>> customerAccounts;

		if (customer != null)
		{
			txOutput.setCustomer(customer);

			customerAccounts = con
					.createQuery(get_custommer_accounts)
					.addParameter("ca_c_id", customer.get("c_id"))
					.executeAndFetchTable()
					.asList();
		}
		else
		{
			customerAccounts = Collections.emptyList();
		}

		txOutput.setCustomerAccounts(customerAccounts);

		if ((customerAccounts.size() < 1) || (customerAccounts.size() > max_acct_len))
		{
			txOutput.setStatus(-221);
		}
	}

	private void executeFrame2(final Connection con, final TxCustomerPositionInput txInput, final TxCustomerPositionOutput txOutput)
	{
		Map<String, Object> customerAsset = txOutput.getCustomerAccounts().get(txInput.getAcctIdIdx());

		List<Map<String, Object>> tradeHistory = con
				.createQuery(get_trade_history)
				.addParameter("t_ca_id", (Long) customerAsset.get("acct_id"))
				.executeAndFetchTable().asList();

		if ((tradeHistory.size() < 1) || (tradeHistory.size() > max_hist_len))
		{
			txOutput.setStatus(-221);
			txOutput.setTradeHistory(Collections.emptyList());
		}
		else
		{
			txOutput.setTradeHistory(tradeHistory);
		}

		con.commit();
	}

	private void executeFrame3(final Connection con)
	{
		con.commit();
	}

	private long getCustomerId(final Connection con, final TxCustomerPositionInput frm1Input)
	{
		if (frm1Input.getCustomerId() > 0)
		{
			return frm1Input.getCustomerId();
		}
		else if (StringUtils.isNotBlank(frm1Input.getTaxId()))
		{
			Long oCid = con
					.createQuery(get_customer_by_taxid)
					.addParameter("c_tax_id", frm1Input.getTaxId())
					.executeScalar(Long.class);

			return oCid != null ? oCid.longValue() : -1;
		}
		else
		{
			throw new IllegalStateException("An invalid TxCustomerPositionInput argument was generated");
		}
	}
}