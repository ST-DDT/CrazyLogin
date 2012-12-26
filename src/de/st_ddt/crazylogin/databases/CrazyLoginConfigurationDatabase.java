package de.st_ddt.crazylogin.databases;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.ConfigurationPlayerDataDatabase;

public final class CrazyLoginConfigurationDatabase extends ConfigurationPlayerDataDatabase<LoginPlayerData> implements CrazyLoginDataDatabase
{

	public CrazyLoginConfigurationDatabase(final JavaPlugin plugin, final ConfigurationSection config)
	{
		super(LoginPlayerData.class, new String[] { "name", "password", "ips", "lastAction" }, "accounts", plugin, config);
	}

	public CrazyLoginConfigurationDatabase(final JavaPlugin plugin, final String path, final String[] columnNames)
	{
		super(LoginPlayerData.class, new String[] { "name", "password", "ips", "lastAction" }, plugin, path, columnNames);
	}

	@Override
	public void saveWithoutPassword(final LoginPlayerData entry)
	{
		save(entry);
	}
}
