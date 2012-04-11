package de.st_ddt.crazylogin.databases;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.ConfigurationDatabase;

public class CrazyLoginConfigurationDatabase extends ConfigurationDatabase<LoginPlayerData>
{

	public CrazyLoginConfigurationDatabase(ConfigurationSection config, String table)
	{
		super(LoginPlayerData.class, config, table);
	}
}
