package org.caudexorigo.oltp1.tx.broker_volume;

import java.sql.Array;
import java.sql.SQLException;
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
import org.sql2o.Query;
import org.sql2o.Sql2o;

public class TxBrokerVolume extends TxBase
{
	private static final int min_broker_list_len = 20;
	private static final int max_broker_list_len = 40;

	private static Logger log = LoggerFactory.getLogger(TxBrokerVolume.class);

	private final SqlContext sqlCtx;
	private final Sql2o sql2o;
	private final String get_volume;
	private final TxInputGenerator txInputGen;
	private final StatsCollector stats;

	public TxBrokerVolume(TxInputGenerator txInputGen, SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);
		this.txInputGen = txInputGen;
		this.sqlCtx = sqlCtx;
		this.stats = stats;
		this.sql2o = sqlCtx.getSql2o();

		Map<SqlEngine, String> queryMapFrm1 = new HashMap<SqlEngine, String>();

		queryMapFrm1.put(SqlEngine.MSSQL, "/org/caudexorigo/oltp1/tx/broker_volume/mssql_broker_volume_frm1.xml");
		queryMapFrm1.put(SqlEngine.POSTGRESQL, "/org/caudexorigo/oltp1/tx/broker_volume/pgsql_broker_volume_frm1.xml");

		Map<String, String> sqlMapFrm1 = XmlProperties.read(queryMapFrm1.get(sqlCtx.getSqlEngine()));
		get_volume = sqlMapFrm1.get("get_volume");
	}

	@Override
	protected final TxOutput run()
	{
		final TxBrokerVolumeInput txInput = txInputGen.generateBrokerVolumeInput(min_broker_list_len, max_broker_list_len);

		final TxBrokerVolumeOutput txOutput = new TxBrokerVolumeOutput();
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

	private void executeFrame1(Connection con, final TxBrokerVolumeInput txInput, TxBrokerVolumeOutput txOutput) throws SQLException
	{
		Query tx = con.createQuery(get_volume);

		if (sqlCtx.getSqlEngine() == SqlEngine.POSTGRESQL)
		{
			Array brokerList = con.getJdbcConnection().createArrayOf("varchar", txInput.getBrokerList());
			
			tx
			.addParameter("broker_list", brokerList)
			.addParameter("sector_name", txInput.getSectorName());
		}
		else if (sqlCtx.getSqlEngine() == SqlEngine.MSSQL)
		{
			String brokerList = StringUtils.join(txInput.getBrokerList(), ",");
			
			tx
			.addParameter("broker_list", brokerList)
			.addParameter("sector_name", txInput.getSectorName());
		}

		final List<Map<String, Object>> lstBrokerVolume = tx.executeAndFetchTable().asList();

		// row_count will frequently be zero near the start of a Test Run when
		// TRADE_REQUEST table is mostly empty
		final int status = (lstBrokerVolume.size() > max_broker_list_len) ? -111 : 0;

		txOutput.setBrokerVolume(lstBrokerVolume);
		txOutput.setStatus(status);
	}
}