package org.caudexorigo.perf;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class ErrorAnalyser
{
	public static Throwable findRootCause(Throwable ex)
	{
		Throwable error_ex = new Exception(ex);
		while (error_ex.getCause() != null)
		{
			error_ex = error_ex.getCause();
		}
		return error_ex;
	}

	public static void logCause(Throwable ex, Logger log)
	{
		Throwable r = findRootCause(ex);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		r.printStackTrace(pw);
		String strace = sw.toString();
		
		r.printStackTrace();
		
		//log.error(String.format("%s%n%s", r.getMessage(), strace));
	}

	public static void shutdown(Throwable ex, Logger log)
	{
		logCause(ex, log);
		exit();
	}

	public static void shutdown(Throwable ex)
	{
		findRootCause(ex).printStackTrace();

		exit();
	}

	private static void exit()
	{
		while (true)
		{
			System.exit(-1);
		}
	}
}