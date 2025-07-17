package org.caudexorigo.oltp1.generator;

import java.util.Map;

import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;

public class CompanyIdSelector
{
	private final int activeCompanyCount;
	private final long minCoId;
	private final long maxCoId;

	public CompanyIdSelector(SqlContext sqlCtx)
	{
		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query coTx = con.createQuery("SELECT COUNT(*) AS co_count , MIN(co_id) AS min_co_id, MAX(co_id) AS max_co_id FROM company;");

			Map<String, Object> tuple = coTx.executeAndFetchTable().asList().get(0);

			if (tuple.get("co_count") instanceof Long)
			{
				activeCompanyCount = ((Long) tuple.get("co_count")).intValue();
			}
			else if (tuple.get("co_count") instanceof Integer)
			{
				activeCompanyCount = (int) tuple.get("co_count");
			}
			else
			{
				activeCompanyCount = 0;
			}

			minCoId = (Long) tuple.get("min_co_id");
			maxCoId = (Long) tuple.get("max_co_id");
		}
	}

	public int getActiveCompanyCount()
	{
		return activeCompanyCount;
	}

	public long getMinCoId()
	{
		return minCoId;
	}

	public long getMaxCoId()
	{
		return maxCoId;
	}
}