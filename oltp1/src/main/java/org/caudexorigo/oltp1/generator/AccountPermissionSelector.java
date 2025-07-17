package org.caudexorigo.oltp1.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.caudexorigo.oltp1.model.AccountPermission;
import org.caudexorigo.perf.db.SqlContext;
import org.sql2o.Connection;
import org.sql2o.Query;


public class AccountPermissionSelector
{
	private final Map<Long, AccountPermission> accountPermissions;

	public AccountPermissionSelector(final SqlContext sqlCtx)
	{
		accountPermissions = new ConcurrentHashMap<>();

		try (Connection con = sqlCtx.getSql2o().open())
		{
			Query tx = con.createQuery("SELECT ap_ca_id, ap_tax_id, ap_l_name, ap_f_name FROM account_permission;");

			tx.executeAndFetchTable().rows().stream().forEach(r -> {

				accountPermissions.put(r.getLong("ap_ca_id"), new AccountPermission(r.getString("ap_tax_id"), r.getString("ap_l_name"), r.getString("ap_f_name")));
			});
		}
	}

	public AccountPermission getAccountPermission(long accountId)
	{
		return accountPermissions.get(accountId);
	}
}