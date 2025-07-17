package org.caudexorigo.oltp1.tx.security_detail;

import java.util.List;
import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxSecurityDetailOutput extends TxOutput
{
	private Map<String, Object> sdInfo1;
	private List<Map<String, Object>> lstSdInfo2;
	private List<Map<String, Object>> lstSdInfo3;
	private List<Map<String, Object>> lstSdInfo4;
	private Map<String, Object> sdInfo5;
	private List<Map<String, Object>> lstSdInfo6;
	private List<Map<String, Object>> lstSdInfo7;

	protected TxSecurityDetailOutput()
	{
		this(0);
	}

	protected TxSecurityDetailOutput(int status)
	{
		super(status);
	}
	

	public Map<String, Object> getSdInfo1()
	{
		return sdInfo1;
	}

	public void setSdInfo1(Map<String, Object> sdInfo1)
	{
		this.sdInfo1 = sdInfo1;
	}

	public List<Map<String, Object>> getLstSdInfo2()
	{
		return lstSdInfo2;
	}

	public void setLstSdInfo2(List<Map<String, Object>> lstSdInfo2)
	{
		this.lstSdInfo2 = lstSdInfo2;
	}

	public List<Map<String, Object>> getLstSdInfo3()
	{
		return lstSdInfo3;
	}

	public void setLstSdInfo3(List<Map<String, Object>> lstSdInfo3)
	{
		this.lstSdInfo3 = lstSdInfo3;
	}

	public List<Map<String, Object>> getLstSdInfo4()
	{
		return lstSdInfo4;
	}

	public void setLstSdInfo4(List<Map<String, Object>> lstSdInfo4)
	{
		this.lstSdInfo4 = lstSdInfo4;
	}

	public Map<String, Object> getSdInfo5()
	{
		return sdInfo5;
	}

	public void setSdInfo5(Map<String, Object> sdInfo5)
	{
		this.sdInfo5 = sdInfo5;
	}

	public List<Map<String, Object>> getLstSdInfo6()
	{
		return lstSdInfo6;
	}

	public void setLstSdInfo6(List<Map<String, Object>> lstSdInfo6)
	{
		this.lstSdInfo6 = lstSdInfo6;
	}

	public List<Map<String, Object>> getLstSdInfo7()
	{
		return lstSdInfo7;
	}

	public void setLstSdInfo7(List<Map<String, Object>> lstSdInfo7)
	{
		this.lstSdInfo7 = lstSdInfo7;
	}

	@Override
	public String toString()
	{
		return String.format("TxSecurityDetailOutput [%nsd_info_1=%s%n, lst_sd_info_2=%s%n, lst_sd_info_3=%s%n, lst_sd_info_4=%s%n, sd_info_5=%s%n, lst_sd_info_6=%s%n, lst_sd_info_7=%s%n, status=%s%n, tx_time=%.3f%n]", sdInfo1, lstSdInfo2, lstSdInfo3, lstSdInfo4, sdInfo5, lstSdInfo6, lstSdInfo7, getStatus(), getTxTime());
	}
}