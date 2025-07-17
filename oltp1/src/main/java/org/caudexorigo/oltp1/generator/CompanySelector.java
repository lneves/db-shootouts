package org.caudexorigo.oltp1.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caudexorigo.oltp1.model.Company;
import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;

public class CompanySelector
{
	private final List<Company> lstCompany = new ArrayList<>();
	private final Map<String, Company> symbolMap = new HashMap<>();

	private final DbRandom drand = new DbRandom();

	public CompanySelector(SqlContext sqlCtx)
	{
		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query tx = con.createQuery("SELECT s_symb, s_issue, co_name FROM \"security\" INNER JOIN company ON s_co_id = co_id;");

			tx.executeAndFetchTable().rows().stream().forEach(r -> {

				Company c = new Company(r.getString("s_symb"), r.getString("s_issue"), r.getString("co_name"));
				lstCompany.add(c);
				symbolMap.put(r.getString("s_symb"), c);
			});
		}
	}

	public Company randomCompany()
	{
		return lstCompany.get(drand.rndIntRange(0, lstCompany.size()));
	}

	public Company forSymbol(String symbol)
	{
		return symbolMap.get(symbol);
	}
}