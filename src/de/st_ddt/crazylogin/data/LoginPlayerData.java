package de.st_ddt.crazylogin.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.exceptions.CrazyLoginUnsupportedPasswordException;
import de.st_ddt.crazyplugin.CrazyLightPluginInterface;
import de.st_ddt.crazyplugin.data.PlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.databases.ConfigurationPlayerDataDatabaseEntry;
import de.st_ddt.crazyutil.databases.FlatPlayerDataDatabaseEntry;
import de.st_ddt.crazyutil.databases.MySQLPlayerDataDatabaseEntry;
import de.st_ddt.crazyutil.databases.SQLDatabase;
import de.st_ddt.crazyutil.databases.SQLitePlayerDataDatabaseEntry;
import de.st_ddt.crazyutil.locales.CrazyLocale;
import de.st_ddt.crazyutil.source.Localized;

public class LoginPlayerData extends PlayerData<LoginPlayerData> implements ConfigurationPlayerDataDatabaseEntry, MySQLPlayerDataDatabaseEntry, SQLitePlayerDataDatabaseEntry, FlatPlayerDataDatabaseEntry, LoginData
{

	protected String password;
	protected final ArrayList<String> ips = new ArrayList<String>(6);
	protected boolean loggedIn;
	protected Date lastAction;
	protected int loginFails;

	public LoginPlayerData(final String name)
	{
		super(name);
		password = "FAILEDLOADING";
		loggedIn = false;
		lastAction = new Date();
	}

	public LoginPlayerData(final String name, final String ip)
	{
		this(name);
		ips.add(ip);
	}

	public LoginPlayerData(final Player player)
	{
		this(player.getName(), player.getAddress().getAddress().getHostAddress());
		loggedIn = true;
	}

	// Used for Imports
	public LoginPlayerData(final String name, final String password, final Date lastAction)
	{
		super(name);
		this.password = password;
		this.loggedIn = false;
		this.lastAction = lastAction;
	}

	// aus Config-Datenbank laden
	public LoginPlayerData(final ConfigurationSection config, final String[] columnNames)
	{
		super(config.getString(columnNames[0]));
		this.password = config.getString(columnNames[1]);
		for (final String ip : config.getStringList(columnNames[2]))
			ips.add(ip);
		lastAction = ObjectSaveLoadHelper.StringToDate(config.getString(columnNames[3]), new Date());
		loggedIn = false;
	}

	// in Config-Datenbank speichern
	@Override
	public void saveToConfigDatabase(final ConfigurationSection config, final String path, final String[] columnNames)
	{
		config.set(path + columnNames[0], name);
		config.set(path + columnNames[1], password);
		config.set(path + columnNames[2], ips);
		config.set(path + columnNames[3], ObjectSaveLoadHelper.DateToString(lastAction));
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
		loggedIn = false;
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

	// aus MySQL/SQLite-Datenbank laden
	public LoginPlayerData(final ResultSet rawData, final String[] columnNames)
	{
		super(SQLDatabase.readName(rawData, columnNames[0]));
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
			lastAction = new Date();
		}
		loggedIn = false;
	}

	// in MySQL-Datenbank speichern
	@Override
	public String saveToMySQLDatabase(final String[] columnNames)
	{
		final String IPs = ChatHelper.listingString(",", ips);
		final Timestamp timestamp = new Timestamp(lastAction.getTime());
		return columnNames[1] + "='" + password + "', " + columnNames[2] + "='" + IPs + "', " + columnNames[3] + "='" + timestamp + "'";
	}

	public String saveToMySQLDatabaseLight(final String[] columnNames)
	{
		final String IPs = ChatHelper.listingString(",", ips);
		final Timestamp timestamp = new Timestamp(lastAction.getTime());
		return columnNames[2] + "='" + IPs + "', " + columnNames[3] + "='" + timestamp + "'";
	}

	@Override
	public String saveInsertToSQLiteDatabase(final String[] columnNames)
	{
		final String IPs = ChatHelper.listingString(",", ips);
		final Timestamp timestamp = new Timestamp(lastAction.getTime());
		return "(" + columnNames[0] + ", " + columnNames[1] + ", " + columnNames[2] + ", " + columnNames[3] + ") VALUES ('" + name + "','" + password + "', '" + IPs + "', '" + timestamp + "')";
	}

	@Override
	public String saveUpdateToSQLiteDatabase(final String[] columnNames)
	{
		final String IPs = ChatHelper.listingString(",", ips);
		final Timestamp timestamp = new Timestamp(lastAction.getTime());
		return columnNames[1] + "='" + password + "', " + columnNames[2] + "='" + IPs + "', " + columnNames[3] + "='" + timestamp + "'";
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
			this.password = getPlugin().getEncryptor().encrypt(name, genSeed(), password);
		}
		catch (final Exception e)
		{
			throw new CrazyLoginUnsupportedPasswordException();
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
		final int max = getPlugin().getMaxStoredIPs();
		while (ips.remove(ip))
			;
		ips.add(0, ip);
		while (ips.size() > max)
			ips.remove(max);
	}

	@Override
	public boolean hasIP(final String ip)
	{
		return ips.contains(ip);
	}

	@Override
	public boolean isLatestIP(final String ip)
	{
		if (ips.isEmpty() || ip == null)
			return false;
		else
			return ips.get(0).equals(ip);
	}

	public List<String> getIPs()
	{
		return ips;
	}

	@Override
	public String getLatestIP()
	{
		if (ips.isEmpty())
			return "";
		else
			return ips.get(0);
	}

	@Override
	public void notifyAction()
	{
		lastAction.setTime(System.currentTimeMillis());
	}

	@Override
	public Date getLastActionTime()
	{
		return lastAction;
	}

	@Override
	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	@Override
	public boolean login(final String password)
	{
		this.loggedIn = isPassword(password);
		if (loggedIn)
			notifyAction();
		else
			loginFails++;
		return loggedIn;
	}

	public int getLoginFails()
	{
		return loginFails;
	}

	public void resetLoginFails()
	{
		loginFails = 0;
	}

	@Override
	public void logout()
	{
		logout(false);
	}

	@Override
	public void logout(final boolean removeIPs)
	{
		this.loggedIn = false;
		notifyAction();
		if (removeIPs)
			ips.clear();
	}

	@Override
	public String toString()
	{
		final String ip = getLatestIP();
		if (ip.equals(""))
			return name + " " + CrazyLightPluginInterface.DATETIMEFORMAT.format(lastAction);
		else
			return name + " " + CrazyLightPluginInterface.DATETIMEFORMAT.format(lastAction) + " @ " + ip;
	}

	public boolean checkTimeOut()
	{
		return checkTimeOut(new Date(System.currentTimeMillis() - getPlugin().getAutoLogoutTime() * 1000));
	}

	public boolean checkTimeOut(final Date timeOut)
	{
		if (timeOut.after(lastAction))
			loggedIn = false;
		return loggedIn;
	}

	public void setLoggedIn(final boolean loggedIn)
	{
		this.loggedIn = loggedIn;
	}

	@Override
	public String getParameter(final CommandSender sender, final int index)
	{
		switch (index)
		{
			case 0:
				return getName();
			case 1:
				return CrazyLightPluginInterface.DATETIMEFORMAT.format(lastAction);
			case 2:
				return loggedIn ? "Online" : "Offline";
			case 3:
				return getLatestIP();
			case 4:
				return "+";
			case 5:
				return loggedIn ? ChatColor.YELLOW.toString() : ChatColor.WHITE.toString();
			case 6:
				return ChatColor.GREEN.toString();
		}
		return "";
	}

	@Override
	public int getParameterCount()
	{
		return 7;
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
	@Localized({ "CRAZYLOGIN.PLAYERINFO.IPADDRESS $IP$", "CRAZYLOGIN.PLAYERINFO.LASTACTION $LastAction$", "CRAZYLOGIN.PLAYERINFO.ASSOCIATES $Associates$" })
	public void showDetailed(final CommandSender target, final String chatHeader)
	{
		final CrazyLocale locale = CrazyLocale.getLocaleHead().getSecureLanguageEntry("CRAZYLOGIN.PLAYERINFO");
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("IPADDRESS"), ChatHelper.listingString(ips));
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("LASTACTION"), CrazyLightPluginInterface.DATETIMEFORMAT.format(getLastActionTime()));
		final HashSet<String> associates = new HashSet<String>();
		for (final String ip : ips)
			for (final LoginPlayerData data : getPlugin().getPlayerDatasPerIP(ip))
				associates.add(data.getName());
		associates.remove(name);
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("ASSOCIATES"), ChatHelper.listingString(associates));
	}
}
