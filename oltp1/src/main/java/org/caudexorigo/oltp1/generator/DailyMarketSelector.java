package org.caudexorigo.oltp1.generator;

import java.sql.Date;
import java.util.List;

import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;

public class DailyMarketSelector
{
	private List<java.sql.Date> dmDays;

	private final DbRandom drand = new DbRandom();

	protected DailyMarketSelector(SqlContext sqlCtx)
	{
		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query dmTx = con.createQuery("SELECT DISTINCT dm_date FROM daily_market;");

			ResultSetHandler<java.sql.Date> rsh = (rs) -> rs.getDate("dm_date");

			dmDays = dmTx.executeAndFetch(rsh);
		}
	}

	public Date getByIndex(int ix)
	{
		return dmDays.get(Math.max(0, ix));
	}

	public Date getRandom()
	{
		return dmDays.get(getRandomIndex());
	}

	public int getRandomIndex()
	{
		return drand.rndIntRange(0, dmDays.size() - 1);
	}

	// public Date getNonUniformRandom()
	// {
	// long r = drand.nonUniformRandom(1, dmDays.size());
	// int s = (int) r - 1;
	// return dmDays.get(s);
	// }
}
