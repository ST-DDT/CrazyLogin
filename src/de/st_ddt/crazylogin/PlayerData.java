package de.st_ddt.crazylogin;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;

public class PlayerData
{

	private final String player;
	private String password;
	private final ArrayList<String> ips = new ArrayList<String>();
	private boolean online;

	public PlayerData(Player player)
	{
		this(player.getName(), player.getAddress().getAddress().getHostAddress());
		online = true;
	}

	public PlayerData(String player, String ip)
	{
		super();
		this.player = player;
		ips.add(ip);
		online = false;
	}

	public PlayerData(FileConfiguration config, String path)
	{
		super();
		this.player = config.getString(path + "name");
		for (String entry : config.getStringList(path + "ips"))
			ips.add(entry);
		this.password = config.getString(path + "password");
		online = false;
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

	public void save(FileConfiguration config, String path)
	{
		config.set(path + "name", this.player);
		config.set(path + "password", this.password);
		config.set(path + "ips", this.ips);
	}
}
