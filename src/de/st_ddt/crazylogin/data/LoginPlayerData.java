package de.st_ddt.crazylogin.data;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.CrazyPluginInterface;
import de.st_ddt.crazyplugin.data.PlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.databases.ConfigurationDatabaseEntry;
import de.st_ddt.crazyutil.databases.FlatDatabaseEntry;
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabase;
import de.st_ddt.crazyutil.databases.MySQLDatabaseEntry;
import de.st_ddt.crazyutil.locales.CrazyLocale;

public class LoginPlayerData extends PlayerData<LoginPlayerData> implements ConfigurationDatabaseEntry, MySQLDatabaseEntry, FlatDatabaseEntry, LoginData
{

	private String password;
	private final ArrayList<String> ips = new ArrayList<String>();
	private boolean online;
	private Date lastAction;

	public LoginPlayerData(final String name)
	{
		super(name);
		online = false;
		lastAction = new Date();
		password = "FAILEDLOADING";
	}

	public LoginPlayerData(final String name, final String ip)
	{
		this(name);
		ips.add(ip);
	}

	public LoginPlayerData(final Player player)
	{
		this(player.getName(), player.getAddress().getAddress().getHostAddress());
		online = true;
	}

	// aus Config-Datenbank laden
	public LoginPlayerData(final ConfigurationSection config, final String[] columnNames)
	{
		super(config.getString(columnNames[0]));
		this.password = config.getString(columnNames[1]);
		for (final String ip : config.getStringList(columnNames[2]))
			ips.add(ip);
		lastAction = ObjectSaveLoadHelper.StringToDate(config.getString(columnNames[3]), new Date());
		online = false;
	}

	// in Config-Datenbank speichern
	@Override
	public void saveToConfigDatabase(final ConfigurationSection config, final String path, final String[] columnNames)
	{
		config.set(path + columnNames[0], name);
		config.set(path + columnNames[1], password);
		config.set(path + columnNames[2], ips);
		config.set(path + columnNames[3], lastAction);
	}

	// aus MySQL-Datenbank laden
	public LoginPlayerData(final ResultSet rawData, final String[] columnNames)
	{
		super(MySQLDatabase.readName(rawData, columnNames[0]));
		try
		{
			password = rawData.getString(columnNames[1]);
		}
		catch (final SQLException e)
		{
			password = "FAILEDLOADING";
			e.printStackTrace();
		}
		try
		{
			final String ipsString = rawData.getString(columnNames[2]);
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
			lastAction = rawData.getTimestamp(columnNames[3]);
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
		final String IPs = ChatHelper.listingString(",", ips);
		try
		{
			query = connection.getConnection().createStatement();
			final Timestamp timestamp = new Timestamp(lastAction.getTime());
			query.executeUpdate("INSERT INTO " + table + " (" + columnNames[0] + "," + columnNames[1] + "," + columnNames[2] + "," + columnNames[3] + ") VALUES ('" + name + "','" + password + "','" + IPs + "','" + timestamp + "') " + " ON DUPLICATE KEY UPDATE " + columnNames[1] + "='" + password + "', " + columnNames[2] + "='" + IPs + "'," + columnNames[3] + "='" + timestamp + "'");
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
		super(rawData[0]);
		this.password = rawData[1];
		try
		{
			if (!rawData[2].equals("."))
			{
				final String[] ips = rawData[2].split(",");
				for (final String ip : ips)
					this.ips.add(ip);
			}
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
		strings[0] = name;
		strings[1] = password;
		strings[2] = ChatHelper.listingString(",", ips);
		if (strings[2].equals(""))
			strings[2] = ".";
		strings[3] = ObjectSaveLoadHelper.DateToString(lastAction);
		return strings;
	}

	protected String getPassword()
	{
		return password;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof LoginData)
			return equals((LoginData) obj);
		return false;
	}

	@Override
	public boolean equals(final LoginData data)
	{
		return getName().equals(data.getName()) && data.isPasswordHash(password);
	}

	@Override
	public int hashCode()
	{
		return name.toLowerCase().hashCode() + password.hashCode();
	}

	@Override
	public void setPassword(final String password) throws CrazyCommandException
	{
		try
		{
			this.password = CrazyLogin.getPlugin().getEncryptor().encrypt(name, genSeed(), password);
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
		if (password.equals("FAILEDLOADING"))
			return false;
		return CrazyLogin.getPlugin().getEncryptor().match(name, password, this.password);
	}

	@Override
	public boolean isPasswordHash(final String hashedPassword)
	{
		return password.equals(hashedPassword);
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
	public boolean isLoggedIn()
	{
		return online;
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
		String IP = getLatestIP();
		if (!IP.equals(""))
			IP = " @" + IP;
		return (online ? ChatColor.GREEN : ChatColor.WHITE) + getName() + ChatColor.WHITE + " " + CrazyPluginInterface.DateFormat.format(lastAction) + IP;
	}

	public void checkTimeOut(final CrazyLogin plugin, final boolean ignoreIfOnline)
	{
		final Date timeOut = new Date();
		timeOut.setTime(timeOut.getTime() - plugin.getAutoLogoutTime() * 1000);
		checkTimeOut(plugin, timeOut, ignoreIfOnline);
	}

	public void checkTimeOut(final CrazyLogin plugin, final Date timeOut, final boolean ignoreIfOnline)
	{
		if (ignoreIfOnline)
			if (isOnline())
				return;
		if (timeOut.after(lastAction))
			this.online = false;
	}

	public void setOnline(final boolean online)
	{
		this.online = online;
	}

	@Override
	public String getParameter(final int index)
	{
		switch (index)
		{
			case 0:
				return getName();
			case 1:
				return CrazyPluginInterface.DateFormat.format(lastAction);
			case 2:
				return online ? "Online" : "Offline";
			case 3:
				return getLatestIP();
		}
		return "";
	}

	@Override
	public int getParameterCount()
	{
		return 4;
	}

	public CrazyLogin getPlugin()
	{
		return CrazyLogin.getPlugin();
	}

	@Override
	protected String getChatHeader()
	{
		return getPlugin().getChatHeader();
	}

	@Override
	public void showDetailed(CommandSender target, String chatHeader)
	{
		final CrazyLocale locale = CrazyLocale.getLocaleHead().getSecureLanguageEntry("CRAZYLOGIN.PLAYERINFO");
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("IPADDRESS"), ChatHelper.listingString(ips));
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("LASTACTION"), CrazyPluginInterface.DateFormat.format(getLastActionTime()));
		HashSet<String> associates = new HashSet<String>();
		for (String ip : ips)
			for (LoginPlayerData data : getPlugin().getPlayerDatasPerIP(ip))
				associates.add(data.getName());
		associates.remove(name);
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("ASSOCIATES"), ChatHelper.listingString(associates));
	}
}
