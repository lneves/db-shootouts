package org.caudexorigo.oltp1.tx.security_detail;

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

public class TxSecurityDetail extends TxBase
{
	private static final Logger log = LoggerFactory.getLogger(TxSecurityDetail.class);

	private static final int min_day_len = 5;
	private static final int max_day_len = 20;
	private static final int max_fin_len = 20;
	private static final int max_news_len = 2;
	private static final int max_comp_len = 3;

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;

	private final TxInputGenerator txInputGen;
	private final String get_info_1;
	private final String get_info_2;
	private final String get_info_3;
	private final String get_info_4;
	private final String get_info_5;
	private final String get_info_6;
	private final String get_info_7;

	public TxSecurityDetail(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);

		this.txInputGen = txInputGen;
		this.sqlCtx = sqlCtx;
		sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();
		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/security_detail/security_detail_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/security_detail/security_detail_frm1.xml");

		Map<String, String> sqlMapFrm1 = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));

		get_info_1 = sqlMapFrm1.get("get_info_1");
		get_info_2 = sqlMapFrm1.get("get_info_2");
		get_info_3 = sqlMapFrm1.get("get_info_3");
		get_info_4 = sqlMapFrm1.get("get_info_4");
		get_info_5 = sqlMapFrm1.get("get_info_5");
		get_info_6 = sqlMapFrm1.get("get_info_6");
		get_info_7 = sqlMapFrm1.get("get_info_7");
	}

	@Override
	protected final TxOutput run()
	{
		TxSecurityDetailOutput txOutput = new TxSecurityDetailOutput();

		final TxSecurityDetailInput txInput = txInputGen.generateSecurityDetailInput();

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

	private void executeFrame1(final Connection con, final TxSecurityDetailInput txInput, final TxSecurityDetailOutput txOutput)
	{
		Map<String, Object> sdInfo1 = con.createQuery(get_info_1)
				.addParameter("symbol", txInput.getSymbol())
				.executeAndFetchTable()
				.asList()
				.stream()
				.findFirst()
				.orElse(Collections.emptyMap());

		if (sdInfo1.isEmpty())
		{
			txOutput.setStatus(-1);
			return;
		}

		List<Map<String, Object>> lstSdInfo2 = con.createQuery(get_info_2)
				.addParameter("co_id", sdInfo1.get("co_id"))
				.executeAndFetchTable()
				.asList();

		List<Map<String, Object>> lstSdInfo3 = con.createQuery(get_info_3)
				.addParameter("co_id", sdInfo1.get("co_id"))
				.executeAndFetchTable()
				.asList();

		List<Map<String, Object>> lstSdInfo4 = con.createQuery(get_info_4)
				.addParameter("max_rows_to_return", txInput.getMaxRowsToReturn())
				.addParameter("symbol", txInput.getSymbol())
				.addParameter("start_day", txInput.getStartDay())
				.executeAndFetchTable()
				.asList();

		Map<String, Object> sdInfo5 = con.createQuery(get_info_5)
				.addParameter("symbol", txInput.getSymbol())
				.executeAndFetchTable()
				.asList().get(0);

		List<Map<String, Object>> lstSdInfo6;
		List<Map<String, Object>> lstSdInfo7;

		if (txInput.isAccessLobFlag())
		{
			lstSdInfo6 = con.createQuery(get_info_6)
					.addParameter("co_id", sdInfo1.get("co_id"))
					.executeAndFetchTable()
					.asList();

			lstSdInfo7 = Collections.emptyList();
		}
		else
		{
			lstSdInfo6 = Collections.emptyList();
			lstSdInfo7 = con.createQuery(get_info_7)
					.addParameter("co_id", sdInfo1.get("co_id"))
					.executeAndFetchTable()
					.asList();
		}

		txOutput.setSdInfo1(sdInfo1);
		txOutput.setLstSdInfo2(lstSdInfo2);
		txOutput.setLstSdInfo3(lstSdInfo3);
		txOutput.setLstSdInfo4(lstSdInfo4);
		txOutput.setSdInfo5(sdInfo5);
		txOutput.setLstSdInfo6(lstSdInfo6);
		txOutput.setLstSdInfo7(lstSdInfo7);

		int day_len = lstSdInfo4.size();
		int fin_len = lstSdInfo3.size();
		int news_len = lstSdInfo6.size() + lstSdInfo7.size();
		int status = 0;

		if ((day_len < min_day_len) || (day_len > max_day_len))
		{
			status = -511;
		}
		else if (fin_len != max_fin_len)
		{
			status = -512;
		}
		else if (news_len != max_news_len)
		{
			status = -513;
		}

		txOutput.setStatus(status);
	}
}