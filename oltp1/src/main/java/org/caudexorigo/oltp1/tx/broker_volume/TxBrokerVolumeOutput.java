package org.caudexorigo.oltp1.tx.broker_volume;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxBrokerVolumeOutput extends TxOutput
{
	private List<Map<String, Object>> lstBrokerVolume;
	private int listLen;

	public TxBrokerVolumeOutput()
	{
		this(0);
	}

	public TxBrokerVolumeOutput(int status)
	{
		super(status);
		setBrokerVolume(Collections.emptyList());
	}

	public List<Map<String, Object>> getBrokerVolume()
	{
		return lstBrokerVolume;
	}

	public void setBrokerVolume(List<Map<String, Object>> lstBrokerVolume)
	{
		this.lstBrokerVolume = lstBrokerVolume;
		this.listLen = lstBrokerVolume.size();
	}

	public int getListLen()
	{
		return listLen;
	}

	@Override
	public String toString()
	{
		return String.format("TxBrokerVolumeOutput [%nbroker_volume=%s%n, list_len=%s%n, status=%s%n, tx_time=%.3f%n]", lstBrokerVolume, listLen, getStatus(), getTxTime());
	}
}