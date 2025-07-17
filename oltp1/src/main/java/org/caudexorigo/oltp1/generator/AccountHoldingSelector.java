package org.caudexorigo.oltp1.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;

public class AccountHoldingSelector
{
	private final DbRandom drand = new DbRandom();
	private final Map<Long, List<String>> accountHoldings;

	public AccountHoldingSelector(final SqlContext sqlCtx)
	{
		accountHoldings = new ConcurrentHashMap<>();

		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query tx = con.createQuery("SELECT DISTINCT hs_ca_id, hs_s_symb FROM holding_summary");

			tx.executeAndFetchTable().rows().stream().forEach(r -> {

				Long accid = r.getLong("hs_ca_id");
				String symbol = r.getString("hs_s_symb");

				if (!accountHoldings.containsKey(accid))
				{
					accountHoldings.put(accid, new ArrayList<String>());
				}

				accountHoldings.get(accid).add(symbol);
			});
		}
	}

	private List<String> getAccountHoldings(long accountId)
	{
		return accountHoldings.get(accountId);
	}

	public String randomAccountHolding(long accountId)
	{
		List<String> l = getAccountHoldings(accountId);

		return l.get(drand.rndIntRange(0, l.size()));
	}
}
