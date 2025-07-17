package org.caudexorigo.perf;

public class TxError extends TxOutput
{
	private final String errorMessage;
	private final String stackTrace;

	public TxError(String errorMessage, String stackTrace)
	{
		super(-1);
		this.errorMessage = errorMessage;
		this.stackTrace = stackTrace;
	}

	@Override
	public String toString()
	{
		return String.format("TxError [%nerrorMessage: %s%n, stackTrace: %s%n, status: %s%n, tx_time: %s%n]", errorMessage, stackTrace, getStatus(), getTxTime());
	}
}