package de.st_ddt.crazylogin;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazyplugin.CrazyPluginInterface;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.databases.ConfigurationDatabaseEntry;
import de.st_ddt.crazyutil.databases.FlatDatabaseEntry;
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabaseEntry;

public class LoginPlayerData implements ConfigurationDatabaseEntry, MySQLDatabaseEntry, FlatDatabaseEntry, LoginData
{

	private final String player;
	private String password;
	private final ArrayList<String> ips = new ArrayList<String>();
	private boolean online;
	private Date lastAction;

	public LoginPlayerData(final Player player)
	{
		this(player.getName(), player.getAddress().getAddress().getHostAddress());
		online = true;
		lastAction = new Date();
	}

	public LoginPlayerData(final String player, final String ip)
	{
		super();
		this.player = player;
		ips.add(ip);
		online = false;
		lastAction = new Date();
	}

	// aus Config-Datenbank laden
	public LoginPlayerData(final ConfigurationSection config, final String[] columnNames)
	{
		super();
		final String colName = columnNames[0];
		final String colPassword = columnNames[1];
		final String colIPs = columnNames[2];
		final String colAction = columnNames[3];
		this.player = config.getString(colName);
		this.password = config.getString(colPassword);
		for (final String ip : config.getStringList(colIPs))
			ips.add(ip);
		lastAction = ObjectSaveLoadHelper.StringToDate(config.getString(colAction), new Date());
		online = false;
	}

	// in Config-Datenbank speichern
	@Override
	public void saveToConfigDatabase(final ConfigurationSection config, final String path, final String[] columnNames)
	{
		final String colName = columnNames[0];
		final String colPassword = columnNames[1];
		final String colIPs = columnNames[2];
		final String colAction = columnNames[3];
		config.set(path + colName, this.player);
		config.set(path + colPassword, this.password);
		config.set(path + colIPs, this.ips);
		config.set(path + colAction, lastAction);
	}

	// aus MySQL-Datenbank laden
	public LoginPlayerData(final ResultSet rawData, final String[] columnNames)
	{
		super();
		final String colName = columnNames[0];
		final String colPassword = columnNames[1];
		final String colIPs = columnNames[2];
		final String colAction = columnNames[3];
		String name = null;
		try
		{
			name = rawData.getString(colName);
		}
		catch (final Exception e)
		{
			name = "ERROR";
			e.printStackTrace();
		}
		finally
		{
			this.player = name;
		}
		try
		{
			password = rawData.getString(colPassword);
		}
		catch (final SQLException e)
		{
			password = "FAILEDLOADING";
			e.printStackTrace();
		}
		try
		{
			final String ipsString = rawData.getString(colIPs);
			if (ipsString != null)
			{
				final String[] ips = ipsString.split(",");
				for (final String ip : ips)
					this.ips.add(ip);
			}
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			lastAction = rawData.getTimestamp(colAction);
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
			lastAction = new Timestamp(new Date().getTime());
		}
		online = false;
	}

	// in MySQL-Datenbank speichern
	@Override
	public void saveToMySQLDatabase(final MySQLConnection connection, final String table, final String[] columnNames)
	{
		Statement query = null;
		final String colName = columnNames[0];
		final String colPassword = columnNames[1];
		final String colIPs = columnNames[2];
		final String colAction = columnNames[3];
		final String IPs = ChatHelper.listingString(",", ips);
		try
		{
			query = connection.getConnection().createStatement();
			final Timestamp timestamp = new Timestamp(lastAction.getTime());
			query.executeUpdate("INSERT INTO " + table + " (" + colName + "," + colPassword + "," + colIPs + "," + colAction + ") VALUES ('" + player + "','" + password + "','" + IPs + "','" + timestamp + "') " + " ON DUPLICATE KEY UPDATE " + colPassword + "='" + password + "', " + colIPs + "='" + IPs + "'," + colAction + "='" + timestamp + "'");
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
			connection.closeConnection();
		}
	}

	// aus Flat-Datenbank laden
	public LoginPlayerData(final String[] rawData)
	{
		super();
		this.player = rawData[0];
		this.password = rawData[1];
		try
		{
			final String[] ips = rawData[2].split(",");
			for (final String ip : ips)
				this.ips.add(ip);
			lastAction = ObjectSaveLoadHelper.StringToDate(rawData[3], new Date());
		}
		catch (final IndexOutOfBoundsException e)
		{
			lastAction = new Date();
		}
		online = false;
	}

	// in Flat-Datenbank speichern
	@Override
	public String[] saveToFlatDatabase()
	{
		final String[] strings = new String[4];
		strings[0] = player;
		strings[1] = password;
		strings[2] = ChatHelper.listingString(",", ips);
		if (strings[2].equals(""))
			strings[2] = ".";
		strings[3] = ObjectSaveLoadHelper.DateToString(lastAction);
		return strings;
	}

	@Override
	public String getName()
	{
		return player;
	}

	@Override
	public Player getPlayer()
	{
		return Bukkit.getPlayer(player);
	}

	@Override
	public OfflinePlayer getOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(player);
	}

	@Override
	public int hashCode()
	{
		return player.toLowerCase().hashCode();
	}

	@Override
	public void setPassword(final String password) throws CrazyCommandException
	{
		try
		{
			this.password = CrazyLogin.getPlugin().getEncryptor().encrypt(player, genSeed(), password);
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new CrazyCommandErrorException(e);
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new CrazyCommandErrorException(e);
		}
	}

	private String genSeed()
	{
		while (true)
		{
			final long value = Math.round(Math.random() * Long.MAX_VALUE);
			final String seed = String.valueOf(value);
			if (seed.length() > 11)
				return seed.substring(1, 10);
		}
	}

	@Override
	public boolean isPassword(final String password)
	{
		return CrazyLogin.getPlugin().getEncryptor().match(player, password, this.password);
	}

	@Override
	public void addIP(final String ip)
	{
		ips.remove(ip);
		ips.add(0, ip);
		while (ips.size() > 5)
			ips.remove(5);
	}

	@Override
	public boolean hasIP(final String ip)
	{
		return ips.contains(ip);
	}

	@Override
	public String getLatestIP()
	{
		if (ips.isEmpty())
			return "";
		return ips.get(0);
	}

	@Override
	public void notifyAction()
	{
		lastAction = new Date();
	}

	@Override
	public Date getLastActionTime()
	{
		return lastAction;
	}

	@Override
	public boolean isOnline()
	{
		return online;
	}

	@Override
	public boolean isPlayerOnline()
	{
		final Player player = getPlayer();
		if (player == null)
			return false;
		return player.isOnline();
	}

	@Override
	public boolean login(final String password)
	{
		this.online = isPassword(password);
		if (online)
			lastAction = new Date();
		return online;
	}

	@Override
	public void logout()
	{
		logout(false);
	}

	@Override
	public void logout(final boolean removeIPs)
	{
		this.online = false;
		lastAction = new Date();
		if (removeIPs)
			ips.clear();
	}

	@Override
	public String toString()
	{
		if (ips.size() == 0)
			return ChatColor.WHITE + getName() + " " + CrazyPluginInterface.DateFormat.format(lastAction);
		return (online ? ChatColor.GREEN.toString() : "") + getName() + ChatColor.WHITE + " " + CrazyPluginInterface.DateFormat.format(lastAction) + " @" + ips.get(0);
	}

	public void checkTimeOut(final CrazyLogin plugin)
	{
		final Date timeOut = new Date();
		timeOut.setTime(timeOut.getTime() - plugin.getAutoLogoutTime() * 1000);
		checkTimeOut(plugin, timeOut);
	}

	public void checkTimeOut(final CrazyLogin plugin, final Date timeOut)
	{
		if (isPlayerOnline())
			return;
		if (timeOut.after(lastAction))
			this.online = false;
	}
}
