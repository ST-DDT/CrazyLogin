package de.st_ddt.crazylogin.data;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.CrazyPluginInterface;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;

public class LoginUnregisteredPlayerData implements LoginData<LoginUnregisteredPlayerData>
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
			return equals((LoginData<?>) obj);
		return false;
	}

	@Override
	public boolean equals(final LoginData<?> data)
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
		final Player player = getPlayer();
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
		final OfflinePlayer player = getOfflinePlayer();
		if (player == null)
			return new Date(0);
		return new Date(player.getLastPlayed());
	}

	@Override
	public boolean isOnline()
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
		return (isOnline() ? ChatColor.DARK_GREEN.toString() : "") + getName() + ChatColor.WHITE + " " + CrazyPluginInterface.DateFormat.format(getLastActionTime()) + IP;
	}

	@Override
	public String getParameter(final int index)
	{
		switch (index)
		{
			case 0:
				return getName();
			case 1:
				return CrazyPluginInterface.DateFormat.format(new Date());
			case 2:
				return isOnline() ? "Online" : "Offline";
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

	@Override
	public String getShortInfo(final String... arg0)
	{
		return toString();
	}

	@Override
	public void show(final CommandSender sender)
	{
		final CrazyLogin plugin = CrazyLogin.getPlugin();
		final Player player = getPlayer();
		plugin.sendLocaleMessage("PLAYERINFO.HEAD", sender, CrazyPluginInterface.DateFormat.format(new Date()));
		plugin.sendLocaleMessage("PLAYERINFO.USERNAME", sender, getName());
		if (player == null)
		{
			plugin.sendLocaleMessage("PLAYERINFO.IPADDRESS", sender, getLatestIP());
		}
		else
		{
			plugin.sendLocaleMessage("PLAYERINFO.DISPLAYNAME", sender, player.getDisplayName());
			plugin.sendLocaleMessage("PLAYERINFO.IPADDRESS", sender, player.getAddress().getAddress().getHostAddress());
			plugin.sendLocaleMessage("PLAYERINFO.CONNECTION", sender, player.getAddress().getHostName());
			if (sender.hasPermission("crazylogin.playerinfo.extended"))
				plugin.sendLocaleMessage("PLAYERINFO.URL", sender, player.getAddress().getAddress().getHostAddress());
		}
	}

	@Override
	public void show(final CommandSender sender, final String... args)
	{
		show(sender);
	}

	@Override
	public boolean isLoggedIn()
	{
		return false;
	}
}
