package org.caudexorigo.oltp1.generator;


import java.util.List;

import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;


public class SymbolSelector
{
	private final List<String> symbols;

	private final DbRandom drand = new DbRandom();

	protected SymbolSelector(SqlContext sqlCtx)
	{
		super();
		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query sTx = con.createQuery("SELECT DISTINCT s_symb FROM security;");

			symbols = sTx.executeScalarList(String.class);
		}
	}

	public String getRandom()
	{
		int r = drand.rndIntRange(0, symbols.size());
		return symbols.get(r);
	}

	public String getNonUniformtRandom(int A, int S)
	{
		int max = symbols.size() - 1;
		int r = (int) drand.nonUniformRandom(0, max, A, S);
		return symbols.get(r);
	}
}