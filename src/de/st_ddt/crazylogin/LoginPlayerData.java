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
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabaseEntry;

public class LoginPlayerData implements ConfigurationDatabaseEntry, MySQLDatabaseEntry
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

	public LoginPlayerData(ConfigurationSection config)
	{
		super();
		this.player = config.getString("name");
		for (String entry : config.getStringList("ips"))
			ips.add(entry);
		this.password = config.getString("password");
		online = false;
	}

	public void save(ConfigurationSection config, String path)
	{
		config.set(path + "name", this.player);
		config.set(path + "password", this.password);
		config.set(path + "ips", this.ips);
	}

	public LoginPlayerData(ResultSet rawData)
	{
		super();
		String name = null;
		try
		{
			name = rawData.getString(CrazyLogin.getPlugin().getColName());
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
			password = rawData.getString(CrazyLogin.getPlugin().getColPassword());
		}
		catch (SQLException e)
		{
			password = "FAILEDLOADING";
			e.printStackTrace();
		}
		try
		{
			String ipsString = rawData.getString(CrazyLogin.getPlugin().getColIPs());
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

	@Override
	public void save(MySQLConnection connection, String table)
	{
		Statement query;
		try
		{
			query = connection.getConnection().createStatement();
			query.executeUpdate("INSERT INTO " + table + " (" + CrazyLogin.getPlugin().getColName() + "," + CrazyLogin.getPlugin().getColPassword() + "," + CrazyLogin.getPlugin().getColIPs() + ") VALUES ('" + player + "','" + password + "','" + ChatHelper.listToString(ips, ",") + "')" + " ON DUPLICATE KEY UPDATE " + CrazyLogin.getPlugin().getColPassword() + "=" + password + ", " + CrazyLogin.getPlugin().getColIPs() + "=" + ChatHelper.listToString(ips, ","));
			query.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
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
