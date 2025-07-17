package org.caudexorigo.oltp1.tx.trade_lookup;

import java.util.List;
import java.util.Map;

import org.caudexorigo.perf.TxOutput;

public class TxTradeLookupOutput extends TxOutput
{
	private List<Map<String, Object>> lstTradesFrm1;
	private List<Map<String, Object>> lstTradesFrm2;
	private List<Map<String, Object>> lstTradesFrm3;
	private List<Map<String, Object>> lstTradesFrm4;
	private List<Map<String, Object>> lstTradesHistory;
	private int frameExecuted;

	public TxTradeLookupOutput()
	{
		this(0);
	}

	public TxTradeLookupOutput(int status)
	{
		super(status);
	}

	public List<Map<String, Object>> getLstTradesFrm1()
	{
		return lstTradesFrm1;
	}

	public void setLstTradesFrm1(List<Map<String, Object>> lstTradesFrm1)
	{
		this.lstTradesFrm1 = lstTradesFrm1;
	}

	public List<Map<String, Object>> getLstTradesFrm2()
	{
		return lstTradesFrm2;
	}

	public void setLstTradesFrm2(List<Map<String, Object>> lstTradesFrm2)
	{
		this.lstTradesFrm2 = lstTradesFrm2;
	}

	public List<Map<String, Object>> getLstTradesFrm3()
	{
		return lstTradesFrm3;
	}

	public void setLstTradesFrm3(List<Map<String, Object>> lstTradesFrm3)
	{
		this.lstTradesFrm3 = lstTradesFrm3;
	}

	public List<Map<String, Object>> getLstTradesFrm4()
	{
		return lstTradesFrm4;
	}

	public void setLstTradesFrm4(List<Map<String, Object>> lstTradesFrm4)
	{
		this.lstTradesFrm4 = lstTradesFrm4;
	}

	public List<Map<String, Object>> getLstTradesHistory()
	{
		return lstTradesHistory;
	}

	public void setLstTradesHistory(List<Map<String, Object>> lstTradesHistory)
	{
		this.lstTradesHistory = lstTradesHistory;
	}

	public int getFrameExecuted()
	{
		return frameExecuted;
	}

	public void setFrameExecuted(int frameExecuted)
	{
		this.frameExecuted = frameExecuted;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeLookupOutput [%nframe_executed=%s%n, lst_trades_frm1=%s%n, lst_trades_frm2=%s%n, lst_trades_frm3=%s%n, lst_trades_frm4=%s%n, lst_trades_history=%s%n, status=%s%n, tx_time=%.3f%n]", frameExecuted, lstTradesFrm1, lstTradesFrm2, lstTradesFrm3, lstTradesFrm4, lstTradesHistory, getStatus(), getTxTime());
	}
}