package de.st_ddt.crazylogin.databases;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.MySQLColumn;
import de.st_ddt.crazyutil.databases.MySQLPlayerDataDatabase;

public final class CrazyLoginMySQLDatabase extends MySQLPlayerDataDatabase<LoginPlayerData>
{

	public CrazyLoginMySQLDatabase(final ConfigurationSection config)
	{
		super(LoginPlayerData.class, getLoginColumns(), "CrazyLogin_accounts", config);
	}

	public CrazyLoginMySQLDatabase(final String tableName, final String[] columnNames, final String host, final String port, final String database, final String user, final String password, final boolean cached)
	{
		super(LoginPlayerData.class, getLoginColumns(), tableName, columnNames, host, port, database, user, password, cached);
	}

	private static MySQLColumn[] getLoginColumns()
	{
		final MySQLColumn[] columns = new MySQLColumn[4];
		columns[0] = new MySQLColumn("name", "CHAR(255)", true, false);
		columns[1] = new MySQLColumn("password", "CHAR(255)", null, false, false);
		columns[2] = new MySQLColumn("ips", "CHAR(255)", null, false, false);
		columns[3] = new MySQLColumn("lastAction", "TIMESTAMP", null, false, false);
		return columns;
	}

	@Override
	public LoginPlayerData updateEntry(final String key)
	{
		LoginPlayerData data = getEntry(key);
		if (data == null)
			return loadEntry(key);
		boolean online = false;
		online = data.isLoggedIn();
		data = loadEntry(key);
		if (data != null)
			data.setOnline(online);
		return data;
	}
}
