package org.caudexorigo.oltp1.generator;

import java.util.List;

import org.caudexorigo.oltp1.model.Sector;
import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;

public class SectorSelector
{
	private final DbRandom drand = new DbRandom();

	private final List<Sector> lstSector;

	public SectorSelector(SqlContext sqlCtx)
	{
		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query tx = con.createQuery("SELECT sc_id, sc_name FROM sector;");

			ResultSetHandler<Sector> rsh = (rs) -> new Sector(rs.getString("sc_id"), rs.getString("sc_name"));

			lstSector = tx.executeAndFetch(rsh);
		}
	}

	public Sector get()
	{
		return lstSector.get(drand.rndIntRange(0, lstSector.size()));
	}
}
