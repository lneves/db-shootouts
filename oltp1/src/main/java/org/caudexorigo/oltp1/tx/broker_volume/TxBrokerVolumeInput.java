package org.caudexorigo.oltp1.tx.broker_volume;

import java.util.Arrays;

public class TxBrokerVolumeInput
{
	private final String[] brokerList;
	private final String sectorName;

	public TxBrokerVolumeInput(String[] brokerList, String sectorName)
	{
		super();
		this.brokerList = brokerList;
		this.sectorName = sectorName;
	}

	public String[] getBrokerList()
	{
		return brokerList;
	}

	public String getSectorName()
	{
		return sectorName;
	}

	@Override
	public String toString()
	{
		return String.format("TxBrokerVolumeInput [%nbrokerList={%s}%n, sectorName=%s%n]", Arrays.toString(brokerList), sectorName);
	}
}