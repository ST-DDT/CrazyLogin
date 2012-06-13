package de.st_ddt.crazylogin.databases;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.ConfigurationDatabase;

public class CrazyLoginConfigurationDatabase extends ConfigurationDatabase<LoginPlayerData>
{

	public CrazyLoginConfigurationDatabase(final ConfigurationSection config, final String table, final String colName, final String colPassword, final String colIPs, final String colLastAction)
	{
		super(LoginPlayerData.class, config, table, new String[] { colName, colPassword, colIPs, colLastAction });
	}
}
