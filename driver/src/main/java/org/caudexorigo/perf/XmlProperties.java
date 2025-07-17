package org.caudexorigo.perf;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlProperties
{
	private static Logger log = LoggerFactory.getLogger(XmlProperties.class);

	public static final Map<String, String> read(String path)
	{
		log.debug("Load properties from: '{}'", path);
		Assert.notBlank("path", path);
		try
		{
			Properties prop = new Properties();
			prop.loadFromXML(XmlProperties.class.getResourceAsStream(path));

			final Map<String, String> r = new HashMap<>();

			prop.entrySet().forEach(e -> {
				r.put(e.getKey().toString(), StringUtils.trimToNull(e.getValue().toString()));
			});

			return r;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
}