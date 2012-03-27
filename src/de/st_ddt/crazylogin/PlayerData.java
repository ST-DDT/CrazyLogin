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
			this.password = crypt(password);
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

	public boolean isPassword(String password)
	{
		try
		{
			return this.password.equals(crypt(password));
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private String crypt(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		return EncryptPassword.SHA512("ÜÄaeut//&/=I" + password + "7421€547" + player + "__+IÄIH§%NK" + password);
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
