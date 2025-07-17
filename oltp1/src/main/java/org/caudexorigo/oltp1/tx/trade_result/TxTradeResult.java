package org.caudexorigo.oltp1.tx.trade_result;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

public class TxTradeResult extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxTradeResult.class);
	
	private final BlockingQueue<Map<String, Object>> mq = new LinkedBlockingQueue<>();

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final String get_trade_info;
	private final String get_customer_account;
	private final String insert_holding_summary;
	private final String update_holding_summary;
	private final String get_holding_asc;
	private final String get_holding_desc;
	private final String insert_holding_history;
	private final String update_holding;
	private final String delete_holding;
	private final String delete_holding_summary;
	private final String insert_holding;
	private final String update_trade_tax;
	private final String get_tax_rate;
	private final String get_commission_rate;
	private final String update_broker;
	private final String insert_trade_history;
	private final String update_trade;
	private final String insert_settlement;
	private final String update_customer_account;
	private final String insert_cash_transaction;
	private final String get_account_balance;

	public TxTradeResult( SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);
		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();
		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm1.xml");

		Map<SqlEngine, String> queryMapFrm2 = new HashMap<SqlEngine, String>();
		queryMapFrm2.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm2.xml");
		queryMapFrm2.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm2.xml");

		Map<SqlEngine, String> queryMapFrm3 = new HashMap<SqlEngine, String>();
		queryMapFrm3.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm3.xml");
		queryMapFrm3.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm3.xml");

		Map<SqlEngine, String> queryMapFrm4 = new HashMap<SqlEngine, String>();
		queryMapFrm4.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm4.xml");
		queryMapFrm4.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm4.xml");

		Map<SqlEngine, String> queryMapFrm5 = new HashMap<SqlEngine, String>();
		queryMapFrm5.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm5.xml");
		queryMapFrm5.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm5.xml");

		Map<SqlEngine, String> queryMapFrm6 = new HashMap<SqlEngine, String>();
		queryMapFrm6.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm6.xml");
		queryMapFrm6.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/trade_result/trade_result_frm6.xml");

		Map<String, String> sqlFrm1Map = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm2Map = XmlProperties.read(queryMapFrm2.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm3Map = XmlProperties.read(queryMapFrm3.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm4Map = XmlProperties.read(queryMapFrm4.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm5Map = XmlProperties.read(queryMapFrm5.get(sqlCtx.getSqlEngine()));
		Map<String, String> sqlFrm6Map = XmlProperties.read(queryMapFrm6.get(sqlCtx.getSqlEngine()));

		get_trade_info = sqlFrm1Map.get("get_trade_info");
		get_customer_account = sqlFrm2Map.get("get_customer_account");
		insert_holding_summary = sqlFrm2Map.get("insert_holding_summary");
		update_holding_summary = sqlFrm2Map.get("update_holding_summary");
		get_holding_asc = sqlFrm2Map.get("get_holding_asc");
		get_holding_desc = sqlFrm2Map.get("get_holding_desc");
		insert_holding_history = sqlFrm2Map.get("insert_holding_history");
		update_holding = sqlFrm2Map.get("update_holding");
		delete_holding = sqlFrm2Map.get("delete_holding");
		delete_holding_summary = sqlFrm2Map.get("delete_holding_summary");
		insert_holding = sqlFrm2Map.get("insert_holding");
		update_trade_tax = sqlFrm3Map.get("update_trade_tax");
		get_tax_rate = sqlFrm3Map.get("get_tax_rate");
		get_commission_rate = sqlFrm4Map.get("get_commission_rate");
		update_broker = sqlFrm5Map.get("update_broker");
		insert_trade_history = sqlFrm5Map.get("insert_trade_history");
		update_trade = sqlFrm5Map.get("update_trade");
		insert_settlement = sqlFrm6Map.get("insert_settlement");
		update_customer_account = sqlFrm6Map.get("update_customer_account");
		insert_cash_transaction = sqlFrm6Map.get("insert_cash_transaction");
		get_account_balance = sqlFrm6Map.get("get_account_balance");
	}
	
	public TxOutput run(Map<String, Object> txInput)
	{
		mq.offer(txInput);
		return execute();	
	}

	@Override
	protected final TxOutput run()
	{
		TxTradeResultOutput txOutput = new TxTradeResultOutput();

		Map<String, Object> txInput = mq.poll();

		if (txInput == null)
		{
			log.warn("Input should not be null");
			txOutput.setStatus(-1);
			return txOutput;
		}

		// {tax_amount=1777.8816, sell_value=22906.0, trade_id=200000087352825, buy_value=18880.0}

		final TradeResultSession session = new TradeResultSession();
		session.putAll(txInput);

		try (Connection con = sql2o.beginTransaction(sqlCtx.getIsolationLevel()))
		{
			executeFrame1(con, txOutput, session);

			if (txOutput.getStatus() < 0)
			{
				return txOutput;
			}

			executeFrame2(con, txOutput, session);

			executeFrame3(con, txOutput, session);

			if (session.getAsDouble("tax_amount") <= 0.00)
			{
				txOutput.setStatus(-831);
				return txOutput;
			}

			executeFrame4(con, txOutput, session);

			if (session.getAsDouble("comm_rate") <= 0)
			{
				txOutput.setStatus(-841);
				return txOutput;
			}

			executeFrame5(con, txOutput, session);

			executeFrame6(con, txOutput, session);

			Map<String, Object> out = new HashMap<>();
			out.put("acct_id", session.get("acct_id"));
			out.put("acct_bal", session.get("acct_bal"));
			out.put("status", "CMPT");

			txOutput.setOutput(out);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);
			txOutput.setStatus(-1);
		}

		return txOutput;
	}

	private void executeFrame1(Connection con, TxOutput txOutput, TradeResultSession session)
	{
		List<Map<String, Object>> tradeInfo = con
				.createQuery(get_trade_info)
				.addParameter("trade_id", session.get("trade_id"))
				.executeAndFetchTable()
				.asList();

		if (tradeInfo.size() != 1)
		{
			txOutput.setStatus(-811);
		}
		else
		{
			session.put("num_found", tradeInfo.size());
			session.putAll(tradeInfo.get(0));
		}
	}

	private void executeFrame2(Connection con, TxOutput txOutput, TradeResultSession session)
	{
		Map<String, Object> customerAccount = con
				.createQuery(get_customer_account)
				.addParameter("acct_id", session.get("acct_id"))
				.executeAndFetchTable()
				.asList()
				.stream()
				.findFirst()
				.orElse(Collections.emptyMap());

		session.putAll(customerAccount);

		// long brokerId = session.getAsLong("broker_id");
		// long custId = session.getAsLong("cust_id");
		// int taxStatus = session.getAsInt("tax_status");

		int neededQty = session.getAsInt("trade_qty");
		double tradePrice = session.getAsDouble("requested_price");
		double buyValue = 0;
		double sellValue = 0;
		Date tradeDts = new Date();
		int hsQty = session.getAsInt("hs_qty");
		int tradeQty = session.getAsInt("trade_qty");

		if (session.getAsBoolean("type_is_sell"))
		{
			if (hsQty == 0)
			{
				con.createQuery(insert_holding_summary)
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.addParameter("trade_qty", tradeQty)
						.executeUpdate();
			}
			else if (hsQty != session.getAsInt("trade_qty"))
			{
				con.createQuery(update_holding_summary)
						.addParameter("hs_qty", session.get("hs_qty"))
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.addParameter("trade_qty", tradeQty)
						.executeUpdate();
			}

			if (hsQty > 0)
			{
				List<Row> holdingList = populateHoldingList(con, session);
				for (Row holdingItem : holdingList)
				{
					if (neededQty <= 0)
					{
						break;
					}

					long holdId = holdingItem.getLong("hold_id");
					int holdQty = holdingItem.getInteger("hold_qty");
					double holdPrice = holdingItem.getBigDecimal("hold_price").doubleValue();

					if (holdQty > neededQty)
					{
						con.createQuery(insert_holding_history)
								.addParameter("hold_id", holdId)
								.addParameter("trade_id", session.get("trade_id"))
								.addParameter("hold_qty", holdQty)
								.addParameter("after_qty", holdQty - neededQty)
								.executeUpdate();

						con.createQuery(update_holding)
								.addParameter("qty", holdQty - neededQty)
								.addParameter("hold_id", holdId)
								.executeUpdate();

						buyValue += neededQty * holdPrice;
						sellValue += neededQty * tradePrice;
						neededQty = 0;
					}
					else
					{
						con.createQuery(insert_holding_history)
								.addParameter("hold_id", holdId)
								.addParameter("trade_id", session.get("trade_id"))
								.addParameter("hold_qty", holdQty)
								.addParameter("after_qty", 0)
								.executeUpdate();

						con.createQuery(delete_holding)
								.addParameter("hold_id", holdId)
								.executeUpdate();

						buyValue += holdQty * holdPrice;
						sellValue += holdQty * tradePrice;
						neededQty = neededQty - holdQty;
					}
				}
			}

			// need to sell more? go short
			if (neededQty > 0)
			{
				con.createQuery(insert_holding_history)
						.addParameter("hold_id", session.get("trade_id"))
						.addParameter("trade_id", session.get("trade_id"))
						.addParameter("hold_qty", 0)
						.addParameter("after_qty", -neededQty)
						.executeUpdate();

				con.createQuery(insert_holding)
						.addParameter("trade_id", session.get("trade_id"))
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.addParameter("trade_dts", tradeDts)
						.addParameter("trade_price", tradePrice)
						.addParameter("qty", -neededQty)
						.executeUpdate();

			}
			else if (hsQty == tradeQty)
			{
				con.createQuery(delete_holding_summary)
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.executeUpdate();
			}
		}
		else
		{ // buy trade
			if (hsQty == 0)
			{
				con.createQuery(insert_holding_summary)
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.addParameter("trade_qty", tradeQty)
						.executeUpdate();

			}
			else if (-hsQty != tradeQty)
			{
				con.createQuery(update_holding_summary)
						.addParameter("hs_qty", session.get("hs_qty"))
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.addParameter("trade_qty", hsQty + tradeQty)
						.executeUpdate();

			}

			if (hsQty < 0)
			{
				List<Row> holdingList = populateHoldingList(con, session);
				for (Row holdingItem : holdingList)
				{
					if (neededQty <= 0)
					{
						break;
					}

					long holdId = holdingItem.getLong("hold_id");
					int holdQty = holdingItem.getInteger("hold_qty");
					double holdPrice = holdingItem.getBigDecimal("hold_price").doubleValue();

					if (holdQty + neededQty < 0)
					{
						con.createQuery(insert_holding_history)
								.addParameter("hold_id", holdId)
								.addParameter("trade_id", session.get("trade_id"))
								.addParameter("hold_qty", holdQty)
								.addParameter("after_qty", holdQty + neededQty)
								.executeUpdate();

						con.createQuery(update_holding)
								.addParameter("qty", holdQty + neededQty)
								.addParameter("hold_id", holdId)
								.executeUpdate();

						sellValue += neededQty * holdPrice;
						buyValue += neededQty * tradePrice;
						neededQty = 0;
					}
					else
					{
						con.createQuery(insert_holding_history)
								.addParameter("hold_id", holdId)
								.addParameter("trade_id", session.get("trade_id"))
								.addParameter("hold_qty", holdQty)
								.addParameter("after_qty", 0)
								.executeUpdate();

						con.createQuery(delete_holding)
								.addParameter("hold_id", holdId)
								.executeUpdate();

						holdQty = -holdQty;
						sellValue += holdQty * holdPrice;
						buyValue += holdQty * tradePrice;
						neededQty = neededQty - holdQty;
					}
				}
				// execute all updates from the above loop

			}

			// all shorts are covered? a new long is created
			if (neededQty > 0)
			{
				con.createQuery(insert_holding_history)
						.addParameter("hold_id", session.get("trade_id"))
						.addParameter("trade_id", session.get("trade_id"))
						.addParameter("hold_qty", 0)
						.addParameter("after_qty", neededQty)
						.executeUpdate();

				con.createQuery(insert_holding)
						.addParameter("trade_id", session.get("trade_id"))
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.addParameter("trade_dts", tradeDts)
						.addParameter("trade_price", tradePrice)
						.addParameter("qty", neededQty)
						.executeUpdate();

			}
			else if (-hsQty == tradeQty)
			{
				con.createQuery(delete_holding_summary)
						.addParameter("acct_id", session.get("acct_id"))
						.addParameter("symbol", session.get("symbol"))
						.executeUpdate();
			}
		}

		session.put("buy_value", buyValue);
		session.put("sell_value", sellValue);
		session.put("trade_dts", tradeDts);
	}

	private void executeFrame3(Connection con, TxOutput txOutput, TradeResultSession session)
	{
		double taxRate = con.createQuery(get_tax_rate)
				.addParameter("cust_id", session.get("cust_id"))
				.executeScalar(Double.class);

		double capitalGain = session.getAsDouble("sell_value") - session.getAsDouble("buy_value");
		double taxAmount = capitalGain > 0 ? capitalGain * taxRate : 0;

		con.createQuery(update_trade_tax)
				.addParameter("trade_id", session.get("trade_id"))
				.addParameter("tax_amount", taxAmount)
				.executeUpdate();

		session.put("tax_amount", taxAmount);
	}

	private void executeFrame4(Connection con, TxOutput txOutput, TradeResultSession session)
	{
		Map<String, Object> cmr = con.createQuery(get_commission_rate)
				.addParameter("cust_id", session.get("cust_id"))
				.addParameter("symbol", session.get("symbol"))
				.addParameter("trade_qty", session.get("trade_qty"))
				.addParameter("type_id", session.get("type_id"))
				.executeAndFetchTable()
				.asList()
				.stream()
				.findFirst()
				.orElse(Collections.emptyMap());

		session.putAll(cmr);
	}

	private void executeFrame5(Connection con, TxOutput txOutput, TradeResultSession session)
	{
		double commAmount = (session.getAsDouble("comm_rate") / 100) * (session.getAsInt("trade_qty") * session.getAsDouble("requested_price"));

		con.createQuery(update_trade)
				.addParameter("comm_amount", commAmount)
				.addParameter("trade_dts", session.get("trade_dts"))
				.addParameter("st_completed_id", "CMPT")
				.addParameter("trade_price", session.get("requested_price"))
				.addParameter("trade_id", session.get("trade_id"))
				.executeUpdate();

		con.createQuery(insert_trade_history)
				.addParameter("trade_id", session.get("trade_id"))
				.addParameter("trade_dts", session.get("trade_dts"))
				.addParameter("st_completed_id", "CMPT")
				.executeUpdate();

		con.createQuery(update_broker)
				.addParameter("comm_amount", commAmount)
				.addParameter("broker_id", session.get("broker_id"))
				.executeUpdate();

		session.put("comm_amount", commAmount);
	}

	private void executeFrame6(Connection con, TxOutput txOutput, TradeResultSession session)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) session.get("trade_dts"));
		cal.add(Calendar.DATE, 2); // the settlement is due in two days

		Date dueDate = cal.getTime();

		boolean tradeIsCash = session.getAsBoolean("trade_is_cash");
		boolean tradeIsSell = session.getAsBoolean("type_is_sell");
		double tradePrice = session.getAsDouble("requested_price");
		int tradeQty = session.getAsInt("trade_qty");
		double charge = session.getAsDouble("charge");
		double taxAmount = session.getAsDouble("tax_amount");
		double commAmount = session.getAsDouble("comm_amount");
		int taxStatus = session.getAsInt("tax_status");

		double seAmount;

		if (tradeIsSell)
		{
			seAmount = (tradeQty * tradePrice) - charge - commAmount;
		}
		else
		{
			seAmount = -((tradeQty * tradePrice) + charge + commAmount);
		}

		if (taxStatus == 1)
		{
			seAmount = seAmount - taxAmount;
		}

		String cashType = tradeIsCash ? "Cash Account" : "Margin";

		con.createQuery(insert_settlement)
				.addParameter("trade_id", session.get("trade_id"))
				.addParameter("cash_type", cashType)
				.addParameter("due_date", dueDate)
				.addParameter("se_amount", seAmount)
				.executeUpdate();

		if (tradeIsCash)
		{
			con.createQuery(update_customer_account)
					.addParameter("acct_id", session.get("acct_id"))
					.addParameter("se_amount", seAmount)
					.executeUpdate();

			con.createQuery(insert_cash_transaction)
					.addParameter("trade_id", session.get("trade_id"))
					.addParameter("trade_dts", session.get("trade_dts"))
					.addParameter("se_amount", seAmount)
					.addParameter("ct_name", String.format("%s %d shares of %s", session.getAsString("type_name"), tradeQty, session.getAsString("s_name")))
					.executeUpdate();
		}

		double balance = con.createQuery(get_account_balance)
				.addParameter("acct_id", session.get("acct_id"))
				.executeScalar(BigDecimal.class).doubleValue();

		session.put("acct_bal", balance);
	}

	private List<Row> populateHoldingList(final Connection con, final TradeResultSession session)
	{
		String holdingQStmt;
		if (session.getAsBoolean("is_lifo"))
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

		return con.createQuery(holdingQStmt)
				.addParameter("acct_id", session.get("acct_id"))
				.addParameter("symbol", session.get("symbol"))
				.executeAndFetchTable()
				.rows();

	}
}