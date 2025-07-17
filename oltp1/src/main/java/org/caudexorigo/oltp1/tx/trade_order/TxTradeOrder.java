package org.caudexorigo.oltp1.tx.trade_order;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.caudexorigo.oltp1.generator.TxInputGenerator;
import org.caudexorigo.oltp1.tx.trade_result.TxTradeResult;
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
import org.sql2o.data.Row;

public class TxTradeOrder extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxTradeOrder.class);

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final TxInputGenerator txInputGen;
	private final String get_customer_info;
	private final String get_account_permissions;
	private final String get_security_info_by_co_name;
	private final String get_security_info_by_symbol;
	private final String get_last_trade;
	private final String get_trade_type;
	private final String get_holding_summmary;
	private final String get_holding_asc;
	private final String get_holding_desc;
	private final String get_tax_rate;
	private final String get_fees;
	private final String get_customer_assets;
	private final String insert_trade;
	private final String insert_trade_request;
	private final String insert_trade_history;

	private final TxTradeResult txTradeResult;

	public TxTradeOrder(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats, TxTradeResult txTradeResult)
	{
		super(stats);

		this.txInputGen = txInputGen;
		this.sqlCtx = sqlCtx;
		this.txTradeResult = txTradeResult;

		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();
		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm1.xml");

		Map<SqlEngine, String> queryMapFrm2 = new HashMap<SqlEngine, String>();
		queryMapFrm2.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm2.xml");
		queryMapFrm2.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm2.xml");

		Map<SqlEngine, String> queryMapFrm3 = new HashMap<SqlEngine, String>();
		queryMapFrm3.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm3.xml");
		queryMapFrm3.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm3.xml");

		Map<SqlEngine, String> queryMapFrm4 = new HashMap<SqlEngine, String>();
		queryMapFrm4.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm4.xml");
		queryMapFrm4.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_order/trade_order_frm4.xml");

		Map<String, String> sqlFrm1Map = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm2Map = XmlProperties.read(queryMapFrm2.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm3Map = XmlProperties.read(queryMapFrm3.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm4Map = XmlProperties.read(queryMapFrm4.get(sqlCtx.getSqlEngine()));

		get_customer_info = sqlFrm1Map.get("get_customer_info");
		get_account_permissions = sqlFrm2Map.get("get_account_permissions");
		get_security_info_by_co_name = sqlFrm3Map.get("get_security_info_by_co_name");
		get_security_info_by_symbol = sqlFrm3Map.get("get_security_info_by_symbol");
		get_last_trade = sqlFrm3Map.get("get_last_trade");
		get_trade_type = sqlFrm3Map.get("get_trade_type");
		get_holding_summmary = sqlFrm3Map.get("get_holding_summmary");
		get_holding_asc = sqlFrm3Map.get("get_holding_asc");
		get_holding_desc = sqlFrm3Map.get("get_holding_desc");
		get_tax_rate = sqlFrm3Map.get("get_tax_rate");
		get_fees = sqlFrm3Map.get("get_fees");
		get_customer_assets = sqlFrm3Map.get("get_customer_assets");
		insert_trade = sqlFrm4Map.get("insert_trade");
		insert_trade_request = sqlFrm4Map.get("insert_trade_request");
		insert_trade_history = sqlFrm4Map.get("insert_trade_history");
	}

	@Override
	protected final TxOutput run()
	{
		TxTradeOrderOutput txOutput = new TxTradeOrderOutput();

		final TxTradeOrderInput txInput = txInputGen.generateTradeOrderInput();
		final TradeOrderSession session = new TradeOrderSession();

		try (Connection con = sql2o.beginTransaction(sqlCtx.getIsolationLevel()))
		{
			executeFrame1(con, txInput, txOutput, session);

			if (txOutput.getStatus() < 0)
			{
				return txOutput;
			}

			if (notEquals(txInput.getExecLName(), session.getAsString("c_l_name"))
					|| notEquals(txInput.getExecFName(), session.getAsString("c_f_name"))
					|| notEquals(txInput.getExecTaxId(), session.getAsString("c_tax_id")))
			{
				executeFrame2(con, txInput, txOutput, session);

				if (txOutput.getStatus() < 0)
				{
					return txOutput;
				}
			}

			executeFrame3(con, txInput, txOutput, session);

			double commRate = session.getAsDouble("comm_rate");
			long tradeQty = txInput.getTradeQty();
			double requestedPrice = txInput.getRequestedPrice();

			double commAmount = (commRate / 100) * tradeQty * requestedPrice;
			String execName = StringUtils.trim(txInput.getExecFName() + " " + txInput.getExecLName());
			boolean isCash = !txInput.getTypeIsMargin();

			session.put("comm_amount", commAmount);
			session.put("exec_name", execName);
			session.put("is_cash", isCash);

			executeFrame4(con, txInput, txOutput, session);

			if (txInput.isRollItBack())
			{
				executeFrame5(con);
				txOutput.setOutput(Collections.singletonMap("is_rollback", true));
			}
			else
			{
				executeFrame6(con, txInput, txOutput, session);

				// TxTradeOrderInput [requested_price=20.56, acct_id=43000046511, is_lifo=false, roll_it_back=false
				// , trade_qty=100, type_is_margin=false, trade_id=0, co_name=Omega Protein Corporation
				// , exec_f_name=Richard, exec_l_name=Genas, exec_tax_id=759IS8011CO098, issue=PREF_B
				// , st_pending_id=PNDG, st_submitted_id=SBMT, symbol= , trade_type_id=TSL]
				//
				// TradeOrderSession [
				// ca_c_id=4300004652, requested_price=20.56, tax_amount=0.0, symbol=OMEPRB, c_tax_id=759IS8011CO098
				// , buy_value=0.0, ca_tax_st=1, status_id=PNDG, comm_amount=8.224, c_tier=3, co_name=Omega Protein Corporation
				// , c_f_name=Richard, b_name=Walter M. Attaway, acct_assets=0.0, sell_value=0.0, comm_rate=0.4, type_is_sell=true
				// , c_l_name=Genas, ca_b_id=4300000050, t_id=200000087354446, ca_name=Richard Genas Custodial Account, market_price=20.02
				// , exec_name=Richard Genas, type_is_market=false, s_name=PREF_B of Omega Protein Corporation
				// , is_cash=true, charge_amount=3.5, status=PNDG}
				// ]
				// TxTradeOrderOutput [{tax_amount=0.0, sell_value=0.0, trade_id=200000087354506, buy_value=0.0}]

				Map<String, Object> meeMsg = new HashMap<>();

				meeMsg.put("requested_price", session.get("market_price"));
				meeMsg.put("symbol", session.get("symbol"));
				meeMsg.put("trade_id", session.get("t_id"));
				meeMsg.put("trade_qty", session.get("trade_qty"));
				meeMsg.put("trade_type_id", txInput.getTradeTypeId());

				if (session.getAsBoolean("type_is_market"))
				{
					meeMsg.put("eAction", "eMEEProcessOrder");
				}
				else
				{
					meeMsg.put("eAction", "eMEESetLimitOrderTrigger");
				}

				txTradeResult.run(meeMsg);
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

	private void executeFrame1(final Connection con, final TxTradeOrderInput txInput, final TxTradeOrderOutput txOutput, final TradeOrderSession session)
	{
		Map<String, Object> customerInfo = con
				.createQuery(get_customer_info)
				.addParameter("acct_id", txInput.getAcctId())
				.executeAndFetchTable()
				.asList()
				.stream()
				.findFirst()
				.orElse(Collections.emptyMap());

		if (customerInfo.isEmpty())
		{
			txOutput.setStatus(-711);
		}

		session.putAll(customerInfo);
	}

	private void executeFrame2(final Connection con, final TxTradeOrderInput txInput, final TxTradeOrderOutput txOutput, final TradeOrderSession session)
	{
		String acl = con
				.createQuery(get_account_permissions)
				.addParameter("acct_id", txInput.getAcctId())
				.addParameter("exec_l_name", txInput.getExecLName())
				.addParameter("exec_f_name", txInput.getExecFName())
				.addParameter("exec_tax_id", txInput.getExecTaxId())
				.executeAndFetchFirst(String.class);

		if (StringUtils.isBlank(acl))
		{
			txOutput.setStatus(-721);
		}
	}

	private void executeFrame3(final Connection con, final TxTradeOrderInput txInput, final TxTradeOrderOutput txOutput, final TradeOrderSession session)
	{
		Map<String, Object> securityInfo;

		// Get information on the security
		if (StringUtils.isBlank(txInput.getSymbol()))
		{
			securityInfo = con
					.createQuery(get_security_info_by_co_name)
					.addParameter("co_name", txInput.getCoName())
					.addParameter("issue", txInput.getIssue())
					.executeAndFetchTable()
					.asList()
					.stream()
					.findFirst()
					.orElse(Collections.emptyMap());
		}
		else
		{
			securityInfo = con
					.createQuery(get_security_info_by_symbol)
					.addParameter("symbol", txInput.getSymbol())
					.executeAndFetchTable()
					.asList()
					.stream()
					.findFirst()
					.orElse(Collections.emptyMap());
		}

		// Get current pricing information for the security
		double marketPrice = con
				.createQuery(get_last_trade)
				.addParameter("symbol", securityInfo.get("s_symb"))
				.executeAndFetch(Double.class)
				.stream()
				.findFirst()
				.orElse(0.0);

		// Set trade characteristics based on the type of trade.
		Row tt = con.createQuery(get_trade_type)
				.addParameter("trade_type_id", txInput.getTradeTypeId())
				.executeAndFetchTable().rows().get(0);
		boolean isMarket = tt.getBoolean("tt_is_mrkt");
		boolean isSell = tt.getBoolean("tt_is_sell");

		// If this is a limit-order, then the requestedPrice was passed in to the frame,
		// but if this a market-order, then the requestedPrice needs to be set to the
		// current market price.
		if (isMarket)
		{
			txInput.setRequestedPrice(marketPrice);
		}

		// Local frame variables used when estimating impact of this trade on
		// any current holdings of the same security.
		final MutableDouble buyValue = new MutableDouble();
		final MutableDouble sellValue = new MutableDouble();
		final MutableDouble requestedPrice = new MutableDouble(txInput.getRequestedPrice());
		final MutableInt neededQty = new MutableInt(txInput.getTradeQty());

		int hsQty = con
				.createQuery(get_holding_summmary)
				.addParameter("acct_id", txInput.getAcctId())
				.addParameter("symbol", securityInfo.get("s_symb"))
				.executeAndFetch(Integer.class)
				.stream()
				.findFirst()
				.orElse(0);

		if (isSell)
		{
			// This is a sell transaction, so estimate the impact to any currently held
			// long positions in the security.
			if (hsQty > 0)
			{
				// Estimate, based on the requested price, any profit that may be realized
				// by selling current holdings for this security. The customer may have
				// multiple holdings at different prices for this security (representing
				// multiple purchases different times).
				List<Row> holdingList = getHoldingList(con, txInput);

				holdingList.forEach(r -> {

					if (neededQty.intValue() != 0)
					{
						int holdQty = r.getInteger("h_qty");
						double holdPrice = r.getBigDecimal("h_price").doubleValue();

						if (holdQty > neededQty.intValue())
						{
							// Only a portion of this holding would be sold as a result of the trade
							buyValue.add(neededQty.doubleValue() * holdPrice);
							sellValue.add(neededQty.doubleValue() * requestedPrice.doubleValue());
							neededQty.setValue(0);
						}
						else
						{
							// All of this holding would be sold as a result of this trade.
							buyValue.add(holdQty * holdPrice);
							sellValue.add(holdQty * requestedPrice.doubleValue());
							neededQty.subtract(holdQty);
						}
					}
				});

				// NOTE: If needed_qty is still greater than 0 at this point, then the
				// customer would be liquidating all current holdings for this security, and
				// then creating a new short position for the remaining balance of
				// this transaction.
			}
		}
		else
		{
			// This is a buy transaction, so estimate the impact to any currently held
			// short positions in the security. These are represented as negative H_QTY
			// holdings. Short positions will be covered before opening a long postion in
			// this security.

			if (hsQty < 0) // Existing short position to buy
			{
				// Estimate, based on the requested price, any profit that may be realized
				// by covering short positions currently held for this security. The customer
				// may have multiple holdings at different prices for this security
				// (representing multiple purchases at different times).
				List<Row> holdingList = getHoldingList(con, txInput);

				holdingList.forEach(r -> {

					if (neededQty.intValue() != 0)
					{
						int holdQty = r.getInteger("h_qty");
						double holdPrice = r.getBigDecimal("h_price").doubleValue();

						if (holdQty + neededQty.intValue() < 0)
						{
							// Only a portion of this holding would be covered (bought back) as
							// a result of this trade.
							buyValue.add(neededQty.doubleValue() * requestedPrice.doubleValue());
							sellValue.add(neededQty.doubleValue() * holdPrice);
							neededQty.setValue(0);
						}
						else
						{
							// All of this holding would be covered (bought back) as
							// a result of this trade.
							holdQty = holdQty * -1;
							buyValue.add(holdQty * requestedPrice.doubleValue());
							sellValue.add(holdQty * holdPrice);
							neededQty.subtract(holdQty);
						}
					}
				});

				// NOTE: If needed_qty is still greater than 0 at this point, then the
				// customer would cover all current short positions (if any) for this security,
				// and then open a new long position for the remaining balance
				// of this transaction.
			}
		}

		// Estimate any capital gains tax that would be incurred as a result of this transaction.

		int taxStatus = session.getAsInt("ca_tax_st");

		double taxAmount = 0.0;
		if ((sellValue.doubleValue() > buyValue.doubleValue()) && (taxStatus == 1 || taxStatus == 2))
		{
			double taxRate = con
					.createQuery(get_tax_rate)
					.addParameter("cust_id", session.getAsLong("ca_c_id"))
					.executeAndFetch(Double.class)
					.stream()
					.findFirst()
					.orElse(0.0);

			taxAmount = (sellValue.doubleValue() - buyValue.doubleValue()) * taxRate;
		}

		// Get administrative fees (e.g. trading charge, commision rate)
		Row frow = con
				.createQuery(get_fees)
				.addParameter("cust_tier", session.get("c_tier"))
				.addParameter("trade_type_id", txInput.getTradeTypeId())
				.addParameter("exch_id", securityInfo.get("s_ex_id"))
				.addParameter("f_trade_qty", txInput.getTradeQty())
				.addParameter("t_trade_qty", txInput.getTradeQty())
				.executeAndFetchTable().rows().get(0);

		double commissionRate = frow.getBigDecimal("cr_rate").doubleValue();
		double chargeAmount = frow.getBigDecimal("ch_chrg").doubleValue();

		// Compute assets on margin trades
		double accountAssets = 0.0;
		if (txInput.getTypeIsMargin())
		{
			accountAssets = con
					.createQuery(get_customer_assets)
					.addParameter("acct_id", txInput.getAcctId())
					.executeAndFetch(Double.class)
					.stream()
					.findFirst()
					.orElse(0.0);
		}

		String statusId = isMarket ? txInput.getStSubmittedId() : txInput.getStPendingId();

		session.put("co_name", StringUtils.trimToEmpty(txInput.getCoName()));
		session.put("symbol", securityInfo.get("s_symb"));
		session.put("requested_price", txInput.getRequestedPrice());
		session.put("buy_value", buyValue.doubleValue());
		session.put("charge_amount", chargeAmount);
		session.put("comm_rate", commissionRate);
		session.put("acct_assets", accountAssets);
		session.put("market_price", marketPrice);
		session.put("s_name", securityInfo.get("s_name"));
		session.put("sell_value", sellValue.doubleValue());
		session.put("status_id", statusId);
		session.put("tax_amount", taxAmount);
		session.put("type_is_market", isMarket);
		session.put("type_is_sell", isSell);
	}

	private void executeFrame4(final Connection con, final TxTradeOrderInput txInput, final TxTradeOrderOutput txOutput, final TradeOrderSession session)
	{
		Date now = new Date();

		long tid = con.createQuery(insert_trade, true)
				.addParameter("trade_dts", now)
				.addParameter("status_id", session.get("status_id"))
				.addParameter("trade_type_id", txInput.getTradeTypeId())
				.addParameter("is_cash", session.get("is_cash"))
				.addParameter("symbol", session.get("symbol"))
				.addParameter("trade_qty", txInput.getTradeQty())
				.addParameter("requested_price", txInput.getRequestedPrice())
				.addParameter("acct_id", txInput.getAcctId())
				.addParameter("exec_name", session.get("exec_name"))
				.addParameter("charge_amount", session.get("charge_amount"))
				.addParameter("comm_amount", session.get("comm_amount"))
				.addParameter("is_lifo", txInput.getIsLifo())
				.executeUpdate()
				.getKeys(Long.class).get(0);

		con.createQuery(insert_trade_request)
				.addParameter("t_id", tid)
				.addParameter("trade_type_id", txInput.getTradeTypeId())
				.addParameter("symbol", session.get("symbol"))
				.addParameter("trade_qty", txInput.getTradeQty())
				.addParameter("requested_price", txInput.getRequestedPrice())
				.addParameter("broker_id", session.getAsLong("ca_b_id"))
				.executeUpdate();

		con.createQuery(insert_trade_history)
				.addParameter("t_id", tid)
				.addParameter("trade_dts", now)
				.addParameter("status_id", session.get("status_id"))
				.executeUpdate();

		session.put("t_id", tid);
	}

	private void executeFrame5(final Connection con)
	{
		con.rollback();
	}

	private void executeFrame6(final Connection con, final TxTradeOrderInput txInput, final TxTradeOrderOutput txOutput, final TradeOrderSession session)
	{
		con.commit();

		Map<String, Object> tradeOrder = new HashMap<>();
		tradeOrder.put("buy_value", session.get("buy_value"));
		tradeOrder.put("sell_value", session.get("sell_value"));
		session.put("status", session.get("status_id"));
		tradeOrder.put("tax_amount", session.get("tax_amount"));
		tradeOrder.put("trade_id", session.get("t_id"));

		txOutput.setOutput(tradeOrder);
	}

	private List<Row> getHoldingList(final Connection con, final TxTradeOrderInput txInput)
	{
		String holdingQStmt;
		if (txInput.getIsLifo())
		{
			// Estimates will be based on closing most recently acquired holdings
			// Could return 0, 1 or many rows
			holdingQStmt = get_holding_desc;
		}
		else
		{
			// Estimates will be based on closing oldest holdings
			// Could return 0, 1 or many rows
			holdingQStmt = get_holding_asc;
		}

		return con
				.createQuery(holdingQStmt)
				.addParameter("acct_id", txInput.getAcctId())
				.addParameter("symbol", txInput.getSymbol())
				.executeAndFetchTable()
				.rows();
	}

	private static boolean notEquals(CharSequence a, CharSequence b)
	{
		return !StringUtils.equalsIgnoreCase(a, b);
	}

}