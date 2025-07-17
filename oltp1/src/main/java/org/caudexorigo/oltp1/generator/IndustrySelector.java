package org.caudexorigo.oltp1.generator;

import java.util.List;

import org.caudexorigo.oltp1.model.Industry;
import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;

public class IndustrySelector
{
	private final List<Industry> lstIndustry;
	private final DbRandom drand = new DbRandom();

	public IndustrySelector(SqlContext sqlCtx)
	{
		super();

		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query tx = con.createQuery("SELECT in_id, in_name, in_sc_id FROM industry;");

			ResultSetHandler<Industry> rsh = (rs) -> new Industry(rs.getString("in_id"), rs.getString("in_name"), rs.getString("in_sc_id"));
			lstIndustry = tx.executeAndFetch(rsh);

		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public Industry getRandom()
	{
		int r = drand.rndIntRange(0, lstIndustry.size());
		return lstIndustry.get(r);
	}
}