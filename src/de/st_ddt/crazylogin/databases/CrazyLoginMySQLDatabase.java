package de.st_ddt.crazylogin.databases;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.MySQLColumn;
import de.st_ddt.crazyutil.databases.MySQLDatabase;
import de.st_ddt.crazyutil.databases.PlayerDataDatabase;

public class CrazyLoginMySQLDatabase extends MySQLDatabase<LoginPlayerData> implements PlayerDataDatabase<LoginPlayerData>
{

	public CrazyLoginMySQLDatabase(final String tableName, final ConfigurationSection config)
	{
		super(LoginPlayerData.class, tableName, config, getColumns(config), 0);
	}

	private static MySQLColumn[] getColumns(final ConfigurationSection config)
	{
		final MySQLColumn[] columns = new MySQLColumn[4];
		columns[0] = new MySQLColumn(config.getString("column.name", "name"), "CHAR(50)", true, false);
		columns[1] = new MySQLColumn(config.getString("column.password", "password"), "CHAR(255)", null, false, false);
		columns[2] = new MySQLColumn(config.getString("column.ips", "ips"), "CHAR(255)", null, false, false);
		columns[3] = new MySQLColumn(config.getString("column.lastAction", "lastAction"), "TIMESTAMP", null, false, false);
		return columns;
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
