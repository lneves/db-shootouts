package org.caudexorigo.oltp1;

import com.lexicalscope.jewel.cli.Option;

public interface BenchmarkArgs
{
	/**
	 * 
	 * @return Database Port.
	 */
	@Option(shortName = "d", longName = "duration", defaultValue = "100")
	int getDuration();

	/**
	 * 
	 * @return Database engine.
	 */
	@Option(shortName = "e", longName = "engine")
	String getEngine();

	/**
	 * 
	 * @return Database Port.
	 */
	@Option(shortName = "p", longName = "port", defaultValue = "0")
	int getPort();

	/**
	 * 
	 * @return Database Host.
	 */
	@Option(shortName = "s", longName = "server")
	String getServer();

	/**
	 * 
	 * @return Database username.
	 */
	@Option(shortName = "U", longName = "user")
	String getUsername();

	/**
	 * 
	 * @return Database password.
	 */
	@Option(shortName = "P", longName = "password")
	String getPassword();

	/**
	 * 
	 * @return Number of simulated clients/users.
	 */
	@Option(shortName = "c", longName = "clients", defaultValue = "10")
	int getClients();
}
