package org.caudexorigo.oltp1.generator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caudexorigo.oltp1.model.AccountPermission;
import org.caudexorigo.oltp1.model.Company;
import org.caudexorigo.oltp1.model.Customer;
import org.caudexorigo.oltp1.tx.broker_volume.TxBrokerVolumeInput;
import org.caudexorigo.oltp1.tx.customer_position.TxCustomerPositionInput;
import org.caudexorigo.oltp1.tx.market_watch.TxMarketWatchInput;
import org.caudexorigo.oltp1.tx.security_detail.TxSecurityDetailInput;
import org.caudexorigo.oltp1.tx.trade_lookup.TxTradeLookupInput;
import org.caudexorigo.oltp1.tx.trade_order.TxTradeOrderInput;
import org.caudexorigo.oltp1.tx.trade_status.TxTradeStatusInput;
import org.caudexorigo.oltp1.tx.trade_update.TxTradeUpdateInput;
import org.caudexorigo.perf.db.SqlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxInputGenerator
{
	private static final Logger log = LoggerFactory.getLogger(TxInputGenerator.class);

	public static final int BASE_COMPANY_COUNT = 5000; // number of base companies in the flat file
	public static final int BASE_COMPANY_COMPETITOR_COUNT = 3 * BASE_COMPANY_COUNT; // number of base company competitor rows
	public static final int ABORTED_TRADE_MOD_FACTOR = 51;
	public static final int ABORT_TRADE = 101;
	public static final long T_TRADE_SHIFT = 200000000000000L;

	private static final int cp_by_cust_id_percent = 50;
	private static final int cp_by_tax_id_percent = 50;
	private static final int cp_get_history_percent = 50;

	private static final int mw_by_acct_id = 35;
	private static final int mw_by_industry = 5;
	private static final int mw_by_watch_list = 60;
	private static final String mw_blank_industry_name = "";

	private static final int tl_do_frame1 = 30;
	private static final int tl_do_frame2 = 30;
	private static final int tl_do_frame3 = 30;
	private static final int tl_do_frame4 = 10;
	private static final int tl_max_rows = 20;

	private static final int tl_a_value_for_trade_id_gen_frame1 = 65535;
	private static final int tl_s_value_for_trade_id_gen_frame1 = 7;
	private static final int tl_a_value_for_time_gen_frame2 = 4095;
	private static final int tl_s_value_for_time_gen_frame2 = 16;
	private static final int tl_a_value_for_symbol_frame3 = 0;
	private static final int tl_s_value_for_symbol_frame3 = 0;
	private static final int tl_a_value_for_time_gen_frame3 = 4095;
	private static final int tl_s_value_for_time_gen_frame3 = 16;
	private static final int tl_a_value_for_time_gen_frame4 = 4095;
	private static final int tl_s_value_for_time_gen_frame4 = 16;

	private static final List<Integer> to_trade_qty = Arrays.asList(100, 200, 400, 800);
	private static final MathContext mc = new MathContext(4);
	private static final int to_security_by_symbol = 60;
	private static final int to_rollback = 1;
	private static final int to_type_is_margin = 8;
	private static final double to_min_sec_price = 20.00;
	private static final double to_max_sec_price = 30.00;

	private static final int tu_do_frame1 = 33;
	private static final int tu_do_frame2 = 33;
	private static final int tu_do_frame3 = 34;
	private static final int tu_avalue_tradeid_frm1 = 65535;
	private static final int tu_svalue_tradeid_frm1 = 7;
	private static final Date tu_void_date = Date.from(Instant.EPOCH);
	public static final int tu_avalue_tradeid_frm2 = 4095;
	public static final int tu_svalue_tradeid_frm2 = 16;

	private final DbRandom drand = new DbRandom();

	private final BrokerSelector brokerSelector;
	private final CompanySelector companySelector;
	private final CustomerSelector customerSelector;
	private final IndustrySelector industrySelector;
	private final SectorSelector sectorSelector;
	private final DailyMarketSelector dailyMarketSelector;
	private final CompanyIdSelector companyIdSelector;
	private final SymbolSelector symbolSelector;
	private final AccountPermissionSelector accountPermissionSelector;
	private final AccountHoldingSelector accountHoldingSelector;

	private final int configuredCustomerCount; // number of configured customers in the database
	private final int activeCustomerCount; // number of active customers in the database
	private final int scaleFactor; // scale factor (number of customers per 1 tpsE) of the database
	private final int daysOfInitialTrades; // number of hours of the initial trades portion of the database
	private final int hoursOfInitialTrades; // number of seconds of the initial trades portion of the database
	private final int secondsOfInitialTrades; // number of hours of the initial trades portion of the database

	private final long maxActivePrePopulatedTradeId;

	public TxInputGenerator(SqlContext sqlCtx)
	{
		this(sqlCtx, 5000, 5000, 500, 300);
	}

	public TxInputGenerator(SqlContext sqlCtx, int cCostumerCount, int aCostumerCount, int scaleFactor, int daysOfInitialTrades)
	{
		super();
		log.info("data generators - begin");
		this.configuredCustomerCount = cCostumerCount;
		this.activeCustomerCount = aCostumerCount;
		this.scaleFactor = scaleFactor;
		this.daysOfInitialTrades = daysOfInitialTrades;
		hoursOfInitialTrades = daysOfInitialTrades * 24;
		secondsOfInitialTrades = hoursOfInitialTrades * 3600;
		maxActivePrePopulatedTradeId = ((secondsOfInitialTrades * (aCostumerCount / scaleFactor)) * 101 / 100);

		brokerSelector = new BrokerSelector(sqlCtx);
		companySelector = new CompanySelector(sqlCtx);
		customerSelector = new CustomerSelector(sqlCtx);
		industrySelector = new IndustrySelector(sqlCtx);
		sectorSelector = new SectorSelector(sqlCtx);
		dailyMarketSelector = new DailyMarketSelector(sqlCtx);
		companyIdSelector = new CompanyIdSelector(sqlCtx);
		symbolSelector = new SymbolSelector(sqlCtx);
		accountPermissionSelector = new AccountPermissionSelector(sqlCtx);
		accountHoldingSelector = new AccountHoldingSelector(sqlCtx);
		log.info("data generators - done");
	}

	public TxBrokerVolumeInput generateBrokerVolumeInput(int minBrokerLstLen, int maxBrokerLstLen)
	{
		int brokerLstLen = drand.rndIntRange(minBrokerLstLen, maxBrokerLstLen);
		String[] aBrk = brokerSelector.getList(brokerLstLen)
				.stream()
				.map(b -> b.getName())
				.toArray(String[]::new);
		String sectorName = sectorSelector.get().getName();

		TxBrokerVolumeInput txInput = new TxBrokerVolumeInput(aBrk, sectorName);
		debug(txInput);

		return txInput;
	}

	public TxCustomerPositionInput generateCustomerPositionInput()
	{
		TxCustomerPositionInput txInput = new TxCustomerPositionInput();

		Customer c = customerSelector.randomCustomer();

		if (drand.rndPercent(cp_by_tax_id_percent))
		{
			txInput.setTaxId(c.getTaxId()); // send tax id instead of customer id
			txInput.setCustomerId(0); // don't need customer id since filled in the tax sid
		}
		else
		{
			txInput.setCustomerId(c.getId()); // send customer id and not the tax id
			txInput.setTaxId("");
		}

		txInput.setHistory(drand.rndPercent(cp_get_history_percent));

		if (txInput.getHistory())
		{
			int maxAccounts = customerSelector.getNumberofAccounts(c);
			txInput.setAcctIdIdx(drand.rndIntRange(0, maxAccounts));
		}
		else
		{
			txInput.setAcctIdIdx(-1);
		}

		debug(txInput);

		return txInput;
	}

	public TxMarketWatchInput generateMarketWatchInput()
	{
		TxMarketWatchInput txInput = new TxMarketWatchInput();

		int threshold = drand.rndPercentage();

		if (threshold <= mw_by_industry)
		{
			txInput.setCId(0);
			txInput.setAcctId(0);
			txInput.setIndustryName(industrySelector.getRandom().getInName());

			if (companyIdSelector.getActiveCompanyCount() > BASE_COMPANY_COUNT)
			{
				txInput.setStartingCoId(drand.rndLongRange(companyIdSelector.getMinCoId(), companyIdSelector.getMaxCoId() - BASE_COMPANY_COUNT));
				txInput.setEndingCoId(txInput.getStartingCoId() + (BASE_COMPANY_COUNT - 1));
			}
			else
			{
				txInput.setStartingCoId(companyIdSelector.getMinCoId());
				txInput.setEndingCoId(companyIdSelector.getMaxCoId());
			}
		}
		else
		{
			txInput.setStartingCoId(0);
			txInput.setEndingCoId(0);
			txInput.setIndustryName(mw_blank_industry_name);

			Customer c = customerSelector.randomCustomer();

			if (threshold <= (mw_by_industry + mw_by_watch_list))
			{
				txInput.setCId(c.getId());
				txInput.setAcctId(0);
			}
			else
			{
				txInput.setAcctId(customerSelector.randomAccId(c));
				txInput.setCId(0);
			}
		}

		java.sql.Date date = dailyMarketSelector.getRandom();

		txInput.setStartDay(new Date(date.getTime()));

		debug(txInput);

		return txInput;
	}

	public TxSecurityDetailInput generateSecurityDetailInput()
	{
		final boolean accessLobFlag = drand.rndPercent(1);
		final int maxRowsToReturn = drand.rndIntRange(5, 20);
		int sday = dailyMarketSelector.getRandomIndex() - maxRowsToReturn;
		final java.sql.Date startDay = dailyMarketSelector.getByIndex(sday);
		String symbol = symbolSelector.getRandom();

		TxSecurityDetailInput txInput = new TxSecurityDetailInput(accessLobFlag, maxRowsToReturn, startDay, symbol);
		debug(txInput);

		return txInput;
	}

	public TxTradeLookupInput generateTradeLookupInput()
	{
		int frame_to_execute; // which of the frames to execute
		long[] trade_id;
		long acct_id;
		long max_acct_id;

		Date start_trade_dts;
		Date end_trade_dts;
		String symbol;

		int f1 = tl_do_frame1;
		int f2 = tl_do_frame1 + tl_do_frame2;
		int f3 = tl_do_frame1 + tl_do_frame2 + tl_do_frame3;

		// long min_t_tds = 1104742800L;
		// long max_t_tds = min_t_tds + (24L * 3600L);

		long dtsOffset = 1104742800000L;

		int iThreshold = drand.rndIntRange(1, 100);

		if (iThreshold <= f1)
		{
			frame_to_execute = 1;

			acct_id = 0;
			max_acct_id = 0;
			start_trade_dts = null;
			end_trade_dts = null;
			symbol = "";

			Set<Long> tradeIdSet = new HashSet<Long>();

			while (tradeIdSet.size() < tl_max_rows)
			{
				long t = generateNonUniformTradeId(tl_a_value_for_trade_id_gen_frame1, tl_s_value_for_trade_id_gen_frame1);
				tradeIdSet.add(t);
			}

			trade_id = new long[tradeIdSet.size()];

			int i = 0;
			for (Long l : tradeIdSet)
			{
				trade_id[i++] = l;
			}
		}
		else if ((iThreshold <= f2))
		{
			frame_to_execute = 2;

			Customer c = customerSelector.randomCustomer();
			acct_id = customerSelector.randomAccId(c);

			long start_trade_dts_time = 0L;

			long backOffFromEndTimeFrame2 = 4 * 8 * 3600; // four 8-hour days or 32 hours
			long frm2MaxDtsMs = (secondsOfInitialTrades - backOffFromEndTimeFrame2) * 1000;

			do
			{
				start_trade_dts_time = dtsOffset + drand.nonUniformRandom(1, frm2MaxDtsMs, tl_a_value_for_time_gen_frame2, tl_s_value_for_time_gen_frame2);
			}
			while (isNotValidTradeDate(start_trade_dts_time));

			start_trade_dts = new Date(start_trade_dts_time);

			// 15 minute interval
			end_trade_dts = new Date(start_trade_dts_time + (15L * 60L * 1000L));

			trade_id = new long[0];
			max_acct_id = 0;
			symbol = "";

		}
		else if ((iThreshold <= f3))
		{
			frame_to_execute = 3;

			trade_id = new long[0];
			acct_id = 0;
			symbol = symbolSelector.getNonUniformtRandom(tl_a_value_for_symbol_frame3, tl_s_value_for_symbol_frame3);

			// FROM TPCE Spec -> The max_acct_id "where" clause is a hook used
			// for engineering purposes only and is not required for benchmark
			// publication purposes.
			max_acct_id = 0;

			long start_trade_dts_time = 0L;
			long backOffFromEndTimeFrame3 = 200 * 60; // 200 minutes
			long frm3MaxDtsMs = (secondsOfInitialTrades - backOffFromEndTimeFrame3) * 1000;

			do
			{
				start_trade_dts_time = dtsOffset + drand.nonUniformRandom(1, frm3MaxDtsMs, tl_a_value_for_time_gen_frame3, tl_s_value_for_time_gen_frame3);
			}
			while (isNotValidTradeDate(start_trade_dts_time));

			start_trade_dts = new Date(start_trade_dts_time);

			// 15 minute interval
			end_trade_dts = new Date(start_trade_dts_time + (15L * 60L * 1000L));
		}
		else
		{
			frame_to_execute = 4;

			Customer c = customerSelector.randomCustomer();
			acct_id = customerSelector.randomAccId(c);

			long backOffFromEndTimeFrame4 = 500 * 60; // 500 minutes
			long frm4MaxDtsMs = (secondsOfInitialTrades - backOffFromEndTimeFrame4) * 1000;

			long start_trade_dts_time = dtsOffset + drand.nonUniformRandom(1, frm4MaxDtsMs, tl_a_value_for_time_gen_frame4, tl_s_value_for_time_gen_frame4);
			start_trade_dts = new Date(start_trade_dts_time);

			trade_id = new long[0];
			max_acct_id = 0;
			symbol = "";
			end_trade_dts = null;
		}

		TxTradeLookupInput txInput = new TxTradeLookupInput(frame_to_execute, trade_id, acct_id, max_acct_id, tl_max_rows, start_trade_dts, end_trade_dts, symbol);

		debug(txInput);
		return txInput;
	}

	private boolean isNotValidTradeDate(long startTradeDtsTime)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTradeDtsTime);

		int dow = cal.get(Calendar.DAY_OF_WEEK);
		int hod = cal.get(Calendar.HOUR_OF_DAY);

		if ((dow == Calendar.SATURDAY) || (dow == Calendar.SUNDAY))
		{
			return true;
		}

		if ((hod < 9) || (hod > 17))
		{
			return true;
		}

		return false;
	}

	public TxTradeStatusInput generateTradeStatusInput()
	{
		long acctId = customerSelector.randomAccId();

		TxTradeStatusInput txInput = new TxTradeStatusInput(acctId);

		debug(txInput);

		return txInput;
	}

	public TxTradeOrderInput generateTradeOrderInput()
	{
		TxTradeOrderInput txInput = new TxTradeOrderInput();

		long acctId = customerSelector.randomAccId();
		AccountPermission acctPer = accountPermissionSelector.getAccountPermission(acctId);

		txInput.setAcctId(acctId);

		txInput.setExecFName(acctPer.getfName());
		txInput.setExecLName(acctPer.getlName());
		txInput.setExecTaxId(acctPer.getTaxId());

		String genSymbol = accountHoldingSelector.randomAccountHolding(acctId);
		Company company = companySelector.forSymbol(genSymbol);

		if (drand.rndPercent(to_security_by_symbol))
		{
			txInput.setSymbol(company.getSymbol());
		}
		else
		{
			txInput.setIssue(company.getIssue());
			txInput.setCoName(company.getCoName());
			txInput.setSymbol(" ");
		}

		if (drand.rndPercent(to_rollback))
		{
			txInput.setRollItBack(true);
		}

		if (drand.rndPercent(35))
		{
			txInput.setIsLifo(true);
		}

		txInput.setTradeQty(drand.rndChoice(to_trade_qty));
		double reqPrice = (new BigDecimal(drand.rndDoubleRange(to_min_sec_price, to_max_sec_price))).round(mc).doubleValue();
		txInput.setRequestedPrice(reqPrice);

		int pTradeTypeChoice = drand.rndPercentage();

		if (pTradeTypeChoice < 30)
		{
			txInput.setTradeTypeId("TMB");
		}
		else if (pTradeTypeChoice < 60)
		{
			txInput.setTradeTypeId("TMS");
		}
		else if (pTradeTypeChoice < 80)
		{
			txInput.setTradeTypeId("TLB");
		}
		else if (pTradeTypeChoice < 90)
		{
			txInput.setTradeTypeId("TLS");
		}
		else
		{
			txInput.setTradeTypeId("TSL");
		}

		txInput.setStPendingId("PNDG");
		txInput.setStSubmittedId("SBMT");

		txInput.setTypeIsMargin(drand.rndPercent(to_type_is_margin));

		debug(txInput);
		return txInput;
	}

	public TxTradeUpdateInput generateTradeUpdateInput()
	{
		int f1 = tl_do_frame1;
		int f2 = tl_do_frame1 + tl_do_frame2;
		int f3 = tl_do_frame1 + tl_do_frame2 + tl_do_frame3;

		int iThreshold = drand.rndIntRange(1, 100);

		TxTradeUpdateInput inputStructure = new TxTradeUpdateInput();

		if (iThreshold <= f1)
		{
			inputStructure.setFrameToExecute(1);
			inputStructure.setMaxTrades(20);
			inputStructure.setMaxUpdates(20);

			Set<Long> tradeIdSet = new HashSet<Long>();

			while (tradeIdSet.size() < inputStructure.getMaxTrades())
			{
				long t = generateNonUniformTradeId(tu_avalue_tradeid_frm1, tu_svalue_tradeid_frm1);
				tradeIdSet.add(t);
			}

			long[] tradeIdArr = tradeIdSet.stream().mapToLong(l -> l).toArray();

			inputStructure.setAcctId(0);
			inputStructure.setMaxAcctId(0);
			inputStructure.setSymbol(new String());
			inputStructure.setTradeId(tradeIdArr);
			inputStructure.setStartTradeDts(tu_void_date);
			inputStructure.setEndTradeDts(tu_void_date);
		}
		else if (iThreshold <= f2)
		{
//			inputStructure.setFrameToExecute(2);
//			inputStructure.setMaxTrades(20);
//			inputStructure.setMaxUpdates(20);
//			inputStructure.setAcctId(customerSelector.randomAccId());
//			
//			inputStructure.setStartTradeDts(generateNonUniformTradeDTS(tradeUpdateFrame2MaxTimeInMilliSeconds,
//					TPCEConstants.TradeUpdateAValueForTimeGenFrame2, TPCEConstants.TradeUpdateSValueForTimeGenFrame2));
//			
//			inputStructure.setEndTradeDts(EGenDate.getTimeStamp(endTime));
//
//			inputStructure.setMaxAcctId(0);
//			inputStructure.setSymbol(new String());
//			Arrays.fill(inputStructure.getTradeId(), 0);
		}
		else
		{

		}

		return inputStructure;
	}

	public long generateNonUniformTradeId(int aValue, int sValue)
	{
		long t = drand.nonUniformRandom(1, maxActivePrePopulatedTradeId, aValue, sValue);

		if (ABORTED_TRADE_MOD_FACTOR == t % ABORT_TRADE)
		{
			t++;
		}

		return (t + T_TRADE_SHIFT);
	}

	protected void debug(Object o)
	{
		if (log.isDebugEnabled())
		{
			log.debug(o.toString());
		}
	}
}