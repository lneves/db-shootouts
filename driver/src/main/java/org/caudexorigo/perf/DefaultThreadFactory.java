package org.caudexorigo.perf;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

public class DefaultThreadFactory implements ThreadFactory
{
	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public DefaultThreadFactory(String prefix)
	{
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		if (StringUtils.isNotBlank(prefix))
		{
			namePrefix = prefix + "-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}
		else
		{
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}
	}

	public Thread newThread(Runnable r)
	{
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}
}
