package org.caudexorigo.oltp1.tx.market_watch;

import java.util.Date;

public class TxMarketWatchInput
{
	private long acctId;
	private long cId;
	private long endingCoId;
	private long startingCoId;
	private Date startDay;
	private String industryName;

	public TxMarketWatchInput()
	{
		super();
	}

	public TxMarketWatchInput(long acctId, long cId, long endingCoId, long startingCoId, Date startDay, String industryName)
	{
		super();
		this.acctId = acctId;
		this.cId = cId;
		this.endingCoId = endingCoId;
		this.startingCoId = startingCoId;
		this.startDay = startDay;
		this.industryName = industryName;
	}

	public long getAcctId()
	{
		return acctId;
	}

	public void setAcctId(long acctId)
	{
		this.acctId = acctId;
	}

	public long getCId()
	{
		return cId;
	}

	public void setCId(long cId)
	{
		this.cId = cId;
	}

	public long getEndingCoId()
	{
		return endingCoId;
	}

	public void setEndingCoId(long endingCoId)
	{
		this.endingCoId = endingCoId;
	}

	public long getStartingCoId()
	{
		return startingCoId;
	}

	public void setStartingCoId(long startingCoId)
	{
		this.startingCoId = startingCoId;
	}

	public Date getStartDay()
	{
		return startDay;
	}

	public void setStartDay(Date startDay)
	{
		this.startDay = startDay;
	}

	public String getIndustryName()
	{
		return industryName;
	}

	public void setIndustryName(String industryName)
	{
		this.industryName = industryName;
	}

	@Override
	public String toString()
	{
		return String.format("TxMarketWatchInput [%nacctId=%s, cId=%s, endingCoId=%s, startingCoId=%s, startDay=%s, industryName=%s%n]", acctId, cId, endingCoId, startingCoId, startDay, industryName);
	}
	
	
}
