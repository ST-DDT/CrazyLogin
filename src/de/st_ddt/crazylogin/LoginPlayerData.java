package de.st_ddt.crazylogin;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.databases.ConfigurationDatabaseEntry;
import de.st_ddt.crazyutil.databases.FlatDatabaseEntry;
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabaseEntry;

public class LoginPlayerData implements ConfigurationDatabaseEntry, MySQLDatabaseEntry, FlatDatabaseEntry
{

	private final String player;
	private String password;
	private final ArrayList<String> ips = new ArrayList<String>();
	private boolean online;

	public LoginPlayerData(Player player)
	{
		this(player.getName(), player.getAddress().getAddress().getHostAddress());
		online = true;
	}

	public LoginPlayerData(String player, String ip)
	{
		super();
		this.player = player;
		ips.add(ip);
		online = false;
	}

	// aus Config-Datenbank laden
	public LoginPlayerData(ConfigurationSection config, String[] columnNames)
	{
		super();
		String colName = columnNames[0];
		String colPassword = columnNames[1];
		String colIPs = columnNames[2];
		this.player = config.getString(colName);
		this.password = config.getString(colPassword);
		for (String ip : config.getStringList(colIPs))
			ips.add(ip);
		online = false;
	}

	// in Config-Datenbank speichern
	@Override
	public void saveToConfigDatabase(ConfigurationSection config, String path, String[] columnNames)
	{
		String colName = columnNames[0];
		String colPassword = columnNames[1];
		String colIPs = columnNames[2];
		config.set(path + colName, this.player);
		config.set(path + colPassword, this.password);
		config.set(path + colIPs, this.ips);
	}

	// aus MySQL-Datenbank laden
	public LoginPlayerData(ResultSet rawData, String[] columnNames)
	{
		super();
		String colName = columnNames[0];
		String colPassword = columnNames[1];
		String colIPs = columnNames[2];
		String name = null;
		try
		{
			name = rawData.getString(colName);
		}
		catch (Exception e)
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
		catch (SQLException e)
		{
			password = "FAILEDLOADING";
			e.printStackTrace();
		}
		try
		{
			String ipsString = rawData.getString(colIPs);
			String[] ips = ipsString.split(",");
			for (String ip : ips)
				this.ips.add(ip);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		online = false;
	}

	// in MySQL-Datenbank speichern
	@Override
	public void saveToMySQLDatabase(MySQLConnection connection, String table, String[] columnNames)
	{
		Statement query = null;
		String colName = columnNames[0];
		String colPassword = columnNames[1];
		String colIPs = columnNames[2];
		String IPs = ChatHelper.listToString(ips, ",");
		try
		{
			query = connection.getConnection().createStatement();
			query.executeUpdate("INSERT INTO " + table + " (" + colName + "," + colPassword + "," + colIPs + ") VALUES ('" + player + "','" + password + "','" + IPs + "') " + " ON DUPLICATE KEY UPDATE " + colPassword + "='" + password + "', " + colIPs + "='" + IPs + "'");
		}
		catch (SQLException e)
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
				catch (SQLException e)
				{}
			connection.closeConnection();
		}
	}

	// aus Flat-Datenbank laden
	public LoginPlayerData(String[] rawData)
	{
		super();
		this.player = rawData[0];
		this.password = rawData[1];
		try
		{
			String[] ips = rawData[2].split(",");
			for (String ip : ips)
				this.ips.add(ip);
		}
		catch (IndexOutOfBoundsException e)
		{}
		online = false;
	}

	// in Flat-Datenbank speichern
	@Override
	public String[] saveToFlatDatabase()
	{
		String[] strings = new String[3];
		strings[0] = player;
		strings[1] = password;
		strings[2] = ChatHelper.listToString(ips, ",");
		if (strings[2].equals(""))
			strings[2] = ".";
		return strings;
	}

	@Override
	public String getName()
	{
		return player;
	}

	public void setPassword(String password) throws CrazyCommandException
	{
		try
		{
			this.password = CrazyLogin.getPlugin().getEncryptor().encrypt(player, genSeed(), password);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new CrazyCommandErrorException(e);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new CrazyCommandErrorException(e);
		}
	}

	private String genSeed()
	{
		while (true)
		{
			long value = Math.round(Math.random() * Long.MAX_VALUE);
			String seed = String.valueOf(value);
			if (seed.length() > 10)
				return seed.substring(0, 9);
		}
	}

	public boolean isPassword(String password)
	{
		return CrazyLogin.getPlugin().getEncryptor().match(player, password, this.password);
	}

	public void addIP(String ip)
	{
		if (!ips.contains(ip))
			ips.add(0, ip);
		while (ips.size() > 5)
			ips.remove(5);
	}

	public boolean hasIP(String ip)
	{
		return ips.contains(ip);
	}

	public boolean isOnline()
	{
		return online;
	}

	public boolean login(String password)
	{
		if (!isPassword(password))
			return false;
		this.online = true;
		return true;
	}

	public void logout()
	{
		logout(false);
	}

	public void logout(boolean removeIPs)
	{
		this.online = false;
		ips.clear();
	}
}
