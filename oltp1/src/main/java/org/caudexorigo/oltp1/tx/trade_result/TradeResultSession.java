package org.caudexorigo.oltp1.tx.trade_result;

import org.caudexorigo.oltp1.model.TxSession;

public class TradeResultSession extends TxSession
{
	@Override
	public String toString()
	{
		return String.format("TradeResultSession [%n%s%n]", getSessionData());
	}
}