package de.st_ddt.crazylogin.databases;

import java.io.File;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.FlatDatabase;
import de.st_ddt.crazyutil.databases.PlayerDataDatabase;

public class CrazyLoginFlatDatabase extends FlatDatabase<LoginPlayerData> implements PlayerDataDatabase<LoginPlayerData>
{

	public CrazyLoginFlatDatabase(final String tableName, final ConfigurationSection config, final File file)
	{
		super(LoginPlayerData.class, tableName, config, new String[] { "name", "password", "ips", "lastAction" }, file);
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
