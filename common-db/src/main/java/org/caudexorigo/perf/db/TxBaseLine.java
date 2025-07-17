package org.caudexorigo.perf.db;


import org.caudexorigo.perf.ErrorAnalyser;
import org.caudexorigo.perf.StatsCollector;
import org.caudexorigo.perf.TxBase;
import org.caudexorigo.perf.TxOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class TxBaseLine extends TxBase
{
	private static Logger log = LoggerFactory.getLogger(TxBaseLine.class);

	private final Sql2o sql2o;
	private final String baselineQuery;

	public TxBaseLine(SqlContext sqlCtx, StatsCollector stats)
	{
		super(stats);
		sql2o = sqlCtx.getSql2o();
		baselineQuery = sqlCtx.getSqlEngine().getBaselineQuery();
	}

	@Override
	public TxOutput run()
	{
		try (Connection con = sql2o.open())
		{
			int ret = con.createQuery(baselineQuery).executeScalar(Integer.class);
			return new TxBaseLineOutput(ret);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);
			return new TxBaseLineOutput(-1);
		}
	}
}