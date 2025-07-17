package org.caudexorigo.oltp1.tx.trade_order;

public class TxTradeOrderInput
{
	private double requestedPrice;
	private long acctId;
	private boolean isLifo;
	private boolean rollItBack;
	private long tradeQty;
	private boolean typeIsMargin;
	private long tradeId;
	private String coName;
	private String execFName;
	private String execLName;
	private String execTaxId;
	private String issue;
	private String stPendingId;
	private String stSubmittedId;
	private String symbol;
	private String tradeTypeId;

	public TxTradeOrderInput()
	{
		super();
	}

	public TxTradeOrderInput(double requestedPrice, long acctId, boolean isLifo, boolean rollItBack, long tradeQty, boolean typeIsMargin, long tradeId, String coName, String execFName, String execLName, String execTaxId, String issue, String stPendingId, String stSubmittedId, String symbol, String tradeTypeId)
	{
		super();
		this.requestedPrice = requestedPrice;
		this.acctId = acctId;
		this.isLifo = isLifo;
		this.rollItBack = rollItBack;
		this.tradeQty = tradeQty;
		this.typeIsMargin = typeIsMargin;
		this.tradeId = tradeId;
		this.coName = coName;
		this.execFName = execFName;
		this.execLName = execLName;
		this.execTaxId = execTaxId;
		this.issue = issue;
		this.stPendingId = stPendingId;
		this.stSubmittedId = stSubmittedId;
		this.symbol = symbol;
		this.tradeTypeId = tradeTypeId;
	}

	public double getRequestedPrice()
	{
		return requestedPrice;
	}

	public void setRequestedPrice(double requestedPrice)
	{
		this.requestedPrice = requestedPrice;
	}

	public long getAcctId()
	{
		return acctId;
	}

	public void setAcctId(long acctId)
	{
		this.acctId = acctId;
	}

	public boolean getIsLifo()
	{
		return isLifo;
	}

	public void setIsLifo(boolean isLifo)
	{
		this.isLifo = isLifo;
	}

	public boolean isRollItBack()
	{
		return rollItBack;
	}

	public void setRollItBack(boolean rollItBack)
	{
		this.rollItBack = rollItBack;
	}

	public long getTradeQty()
	{
		return tradeQty;
	}

	public void setTradeQty(long tradeQty)
	{
		this.tradeQty = tradeQty;
	}

	public boolean getTypeIsMargin()
	{
		return typeIsMargin;
	}

	public void setTypeIsMargin(boolean typeIsMargin)
	{
		this.typeIsMargin = typeIsMargin;
	}

	public long getTradeId()
	{
		return tradeId;
	}

	public void setTradeId(long tradeId)
	{
		this.tradeId = tradeId;
	}

	public String getCoName()
	{
		return coName;
	}

	public void setCoName(String coName)
	{
		this.coName = coName;
	}

	public String getExecFName()
	{
		return execFName;
	}

	public void setExecFName(String execFName)
	{
		this.execFName = execFName;
	}

	public String getExecLName()
	{
		return execLName;
	}

	public void setExecLName(String execLName)
	{
		this.execLName = execLName;
	}

	public String getExecTaxId()
	{
		return execTaxId;
	}

	public void setExecTaxId(String execTaxId)
	{
		this.execTaxId = execTaxId;
	}

	public String getIssue()
	{
		return issue;
	}

	public void setIssue(String issue)
	{
		this.issue = issue;
	}

	public String getStPendingId()
	{
		return stPendingId;
	}

	public void setStPendingId(String stPendingId)
	{
		this.stPendingId = stPendingId;
	}

	public String getStSubmittedId()
	{
		return stSubmittedId;
	}

	public void setStSubmittedId(String stSubmittedId)
	{
		this.stSubmittedId = stSubmittedId;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	public String getTradeTypeId()
	{
		return tradeTypeId;
	}

	public void setTradeTypeId(String tradeTypeId)
	{
		this.tradeTypeId = tradeTypeId;
	}

	@Override
	public String toString()
	{
		return String.format("TxTradeOrderInput [requested_price=%s, acct_id=%s, is_lifo=%s, roll_it_back=%s, trade_qty=%s, type_is_margin=%s, trade_id=%s, co_name=%s, exec_f_name=%s, exec_l_name=%s, exec_tax_id=%s, issue=%s, st_pending_id=%s, st_submitted_id=%s, symbol=%s, trade_type_id=%s]", requestedPrice, acctId, isLifo, rollItBack, tradeQty, typeIsMargin, tradeId, coName, execFName, execLName, execTaxId, issue, stPendingId, stSubmittedId, symbol, tradeTypeId);
	}
}