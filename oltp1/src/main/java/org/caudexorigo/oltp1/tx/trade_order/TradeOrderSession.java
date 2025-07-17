package org.caudexorigo.oltp1.tx.trade_order;

import org.caudexorigo.oltp1.model.TxSession;

public class TradeOrderSession extends TxSession
{
	@Override
	public String toString()
	{
		return String.format("TradeOrderSession [%n%s%n]", getSessionData());
	}
}