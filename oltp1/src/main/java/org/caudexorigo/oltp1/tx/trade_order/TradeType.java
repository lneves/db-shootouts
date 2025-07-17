package org.caudexorigo.oltp1.tx.trade_order;

import org.sql2o.ResultSetHandler;

public class TradeType
{	
	public static ResultSetHandler<TradeType> rsHandler = (rs) -> new TradeType(rs.getBoolean("tt_is_mrkt"), rs.getBoolean("tt_is_sell"));
	
	private final boolean isSell;
	private final boolean isMarket;

	public TradeType(boolean isSell, boolean isMarket)
	{
		super();
		this.isSell = isSell;
		this.isMarket = isMarket;
	}

	public boolean isSell()
	{
		return isSell;
	}

	public boolean isMarket()
	{
		return isMarket;
	}
}