package org.caudexorigo.oltp1.generator;

import java.util.List;

import org.caudexorigo.oltp1.model.Customer;
import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;

public class CustomerSelector
{
	private static final long IDENT_SHIFT = 4300000000L; // All ids are shifted by this
	private final static int MAX_ACCOUNTS_PER_CUST = 10;
	private final static int[] MIN_ACCOUNTS_PER_CUST_RANGE = { 1, 2, 5 }; // tier based
	private final static int[] MAX_ACCOUNTS_PER_CUST_RANGE = { 4, 8, 10 }; // tier based

	private final List<Customer> lstCustomerId;

	private final DbRandom drand = new DbRandom();

	public CustomerSelector(SqlContext sqlCtx)
	{
		super();

		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query tx = con.createQuery("SELECT c_id, c_tax_id, c_tier FROM customer");

			ResultSetHandler<Customer> rsh = (rs) -> new Customer(rs.getLong("c_id"), rs.getString("c_tax_id"), rs.getShort("c_tier"));

			lstCustomerId = tx.executeAndFetch(rsh);
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public Customer randomCustomer()
	{
		int r = drand.rndIntRange(0, lstCustomerId.size());
		return lstCustomerId.get(r);
	}

	public long randomAccId()
	{
		Customer customer = randomCustomer();

		return randomAccId(customer);
	}

	public long randomAccId(Customer c)
	{
		long accCount, startAcc;

		accCount = getNumberofAccounts(c);
		startAcc = getStartingAccId(c.getId());
		return drand.rndLongRange(startAcc, startAcc + accCount - 1);
	}

	public int getNumberofAccounts(Customer c)
	{
		int minAccountCount = MIN_ACCOUNTS_PER_CUST_RANGE[c.getTier() - 1];
		int mod = MAX_ACCOUNTS_PER_CUST_RANGE[c.getTier() - 1] - minAccountCount + 1;
		int inverseCid = getInverseCid(c.getId());

		// Note: the calculations below assume load unit contains 1000 customers.
		if (inverseCid < 200)
		{ // Tier 1
			return ((inverseCid % mod) + minAccountCount);
		}
		else if (inverseCid < 800)
		{ // Tier 2
			return (((inverseCid - 200 + 1) % mod) + minAccountCount);
		}
		else
		{ // Tier 3
			return (((inverseCid - 800 + 2) % mod) + minAccountCount);
		}
	}

	private static long getStartingAccId(long cid)
	{
		// start account ids on the next boundary for the new customer
		return ((cid - 1) * MAX_ACCOUNTS_PER_CUST + 1);
	}

	private static long getEndtingAccId(long cid)
	{
		// start account ids on the next boundary for the new customer
		return ((cid + 1) * MAX_ACCOUNTS_PER_CUST);
	}

	// lower 3 digits
	private static long lowDigits(long cid)
	{
		return (cid - 1) % 1000;
	}

	// higher 3 digits
	private static long highDigits(long cid)
	{
		return (cid - 1) / 1000;
	}

	private static long permute(long low, long high)
	{
		return (677 * low + 33 * (high + 1)) % 1000;
	}

	// Inverse permutation.
	private static long inversePermute(long low, long high)
	{
		// Extra mod to make the result always positive
		return (((613 * (low - 33 * (high + 1))) % 1000) + 1000) % 1000;
	}

	// Return scrambled inverse customer id in range of 0 to 999.
	private static int getInverseCid(long cid)
	{
		int cHigh = (int) highDigits(cid);
		int inverseCid = (int) inversePermute(lowDigits(cid), cHigh);

		if (inverseCid < 200)
		{ // Tier 1: value 0 to 199
			return ((3 * inverseCid + (cHigh + 1)) % 200);
		}
		else
		{
			if (inverseCid < 800)
			{ // Tier 2: value 200 to 799
				return (((59 * inverseCid + 47 * (cHigh + 1)) % 600) + 200);
			}
			else
			{ // Tier 3: value 800 to 999
				return (((23 * inverseCid + 17 * (cHigh + 1)) % 200) + 800);
			}
		}
	}
}