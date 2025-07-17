package org.caudexorigo.oltp1.model;

public class Company
{
	private final String symbol;
	private final String issue;
	private final String coName;

	public Company(String symbol, String issue, String coName)
	{
		this.symbol = symbol;
		this.issue = issue;
		this.coName = coName;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public String getIssue()
	{
		return issue;
	}

	public String getCoName()
	{
		return coName;
	}

	@Override
	public String toString()
	{
		return String.format("Company [s_symb=%s, s_issue=%s, co_name=%s]", symbol, issue, coName);
	}
}