package de.st_ddt.crazylogin.databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.MySQLPlayerDataDatabase;
import de.st_ddt.crazyutil.databases.SQLColumn;

public final class CrazyLoginMySQLDatabase extends MySQLPlayerDataDatabase<LoginPlayerData> implements CrazyLoginDataDatabase
{

	public CrazyLoginMySQLDatabase(final ConfigurationSection config)
	{
		super(LoginPlayerData.class, getLoginColumns(), "CrazyLogin_accounts", config);
	}

	public CrazyLoginMySQLDatabase(final String tableName, final String[] columnNames, final String host, final String port, final String database, final String user, final String password, final boolean cached, final boolean doNotUpdate)
	{
		super(LoginPlayerData.class, getLoginColumns(), tableName, columnNames, host, port, database, user, password, cached, doNotUpdate);
	}

	private static SQLColumn[] getLoginColumns()
	{
		final SQLColumn[] columns = new SQLColumn[4];
		columns[0] = new SQLColumn("name", "CHAR(255)", true, false);
		columns[1] = new SQLColumn("password", "CHAR(255)", null, false, false);
		columns[2] = new SQLColumn("ips", "CHAR(255)", null, false, false);
		columns[3] = new SQLColumn("lastAction", "TIMESTAMP", null, false, false);
		return columns;
	}

	@Override
	public LoginPlayerData updateEntry(final String key)
	{
		if (isStaticDatabase())
			return getEntry(key);
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

	@Override
	public void saveWithoutPassword(final LoginPlayerData entry)
	{
		final Connection connection = connectionPool.getConnection();
		if (connection == null)
			return;
		Statement query = null;
		try
		{
			query = connection.createStatement();
			final String sql = "UPDATE `" + tableName + "` SET " + entry.saveToMySQLDatabaseLight(columnNames) + " WHERE " + columnNames[0] + "='" + entry.getName() + "'";
			if (query.executeUpdate(sql) == 0)
			{
				datas.remove(entry.getName().toLowerCase());
				final Player player = entry.getPlayer();
				if (player != null)
					player.kickPlayer("Your account has been deleted!");
			}
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (query != null)
				try
				{
					query.close();
				}
				catch (final SQLException e)
				{}
			connectionPool.releaseConnection(connection);
		}
	}
}
