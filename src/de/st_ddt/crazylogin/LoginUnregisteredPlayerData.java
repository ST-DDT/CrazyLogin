package de.st_ddt.crazylogin;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.st_ddt.crazyplugin.CrazyPluginInterface;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;

public class LoginUnregisteredPlayerData implements LoginData
{

	private final String player;

	public LoginUnregisteredPlayerData(final String name)
	{
		super();
		this.player = name;
	}

	public LoginUnregisteredPlayerData(final OfflinePlayer player)
	{
		this(player.getName());
	}

	@Override
	public String getName()
	{
		return player;
	}

	@Override
	public Player getPlayer()
	{
		return Bukkit.getPlayerExact(player);
	}

	protected String getPassword()
	{
		return null;
	}

	@Override
	public OfflinePlayer getOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(player);
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
		if (data instanceof LoginPlayerData)
			return false;
		return getName().equals(data.getName());
	}

	@Override
	public int hashCode()
	{
		return player.toLowerCase().hashCode();
	}

	@Override
	public void setPassword(final String password) throws CrazyCommandException
	{
		throw new CrazyCommandException();
	}

	@Override
	public boolean isPassword(final String password)
	{
		return false;
	}

	@Override
	public boolean isPasswordHash(final String hashedPassword)
	{
		return false;
	}

	@Override
	public void addIP(final String ip)
	{
	}

	@Override
	public boolean hasIP(final String ip)
	{
		return ip.equals(getLatestIP());
	}

	@Override
	public String getLatestIP()
	{
		Player player = getPlayer();
		if (player == null)
			return "";
		return player.getAddress().getAddress().getHostAddress();
	}

	@Override
	public void notifyAction()
	{
	}

	@Override
	public Date getLastActionTime()
	{
		OfflinePlayer player = getOfflinePlayer();
		if (player == null)
			return new Date(0);
		return new Date(player.getLastPlayed());
	}

	@Override
	public boolean isOnline()
	{
		return isPlayerOnline();
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
		return false;
	}

	@Override
	public void logout()
	{
	}

	@Override
	public void logout(final boolean removeIPs)
	{
	}

	@Override
	public String toString()
	{
		String IP = getLatestIP();
		if (!IP.equals(""))
			IP = " @" + IP;
		return (isPlayerOnline() ? ChatColor.DARK_GREEN.toString() : "") + getName() + ChatColor.WHITE + " " + CrazyPluginInterface.DateFormat.format(getLastActionTime()) + IP;
	}
}
