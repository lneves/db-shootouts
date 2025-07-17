package org.caudexorigo.oltp1.tx.security_detail;

public class TxSecurityDetailInput
{
	private boolean accessLobFlag;
	private int maxRowsToReturn;
	private java.sql.Date startDay;
	private String symbol;

	public TxSecurityDetailInput(boolean accessLobFlag, int maxRowsToReturn, java.sql.Date startDay, String symbol)
	{
		super();
		this.accessLobFlag = accessLobFlag;
		this.maxRowsToReturn = maxRowsToReturn;
		this.startDay = startDay;
		this.symbol = symbol;
	}

	public boolean isAccessLobFlag()
	{
		return accessLobFlag;
	}

	public int getMaxRowsToReturn()
	{
		return maxRowsToReturn;
	}

	public java.sql.Date getStartDay()
	{
		return startDay;
	}

	public String getSymbol()
	{
		return symbol;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeLookupInput [%naccess_lob_flag=%s, max_rows_to_return=%s, start_day=%s, symbol=%s%n]", accessLobFlag, maxRowsToReturn, startDay, symbol);
	}
}