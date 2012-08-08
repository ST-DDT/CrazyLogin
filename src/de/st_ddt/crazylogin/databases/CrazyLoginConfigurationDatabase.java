package de.st_ddt.crazylogin.databases;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.ConfigurationDatabase;
import de.st_ddt.crazyutil.databases.PlayerDataDatabase;

public class CrazyLoginConfigurationDatabase extends ConfigurationDatabase<LoginPlayerData> implements PlayerDataDatabase<LoginPlayerData>
{

	public CrazyLoginConfigurationDatabase(final String tableName, final ConfigurationSection config, JavaPlugin plugin)
	{
		super(LoginPlayerData.class, tableName, config, new String[] { "name", "password", "ips", "lastAction" }, plugin);
	}

	@Override
	public LoginPlayerData getEntry(final OfflinePlayer player)
	{
		return getEntry(player.getName());
	}

	@Override
	public boolean hasEntry(final OfflinePlayer player)
	{
		return hasEntry(player.getName());
	}

	@Override
	public boolean deleteEntry(final OfflinePlayer player)
	{
		return deleteEntry(player.getName());
	}
}
