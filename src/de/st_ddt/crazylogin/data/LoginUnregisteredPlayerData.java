package de.st_ddt.crazylogin.data;

import java.util.Date;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.CrazyLightPluginInterface;
import de.st_ddt.crazyplugin.data.PlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.locales.CrazyLocale;
import de.st_ddt.crazyutil.source.Localized;

public final class LoginUnregisteredPlayerData extends PlayerData<LoginUnregisteredPlayerData> implements LoginData
{

	public LoginUnregisteredPlayerData(final String name)
	{
		super(name);
	}

	public LoginUnregisteredPlayerData(final OfflinePlayer player)
	{
		this(player.getName());
	}

	protected String getPassword()
	{
		return null;
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
		return name.toLowerCase().hashCode();
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
		else
			return new Date(player.getLastPlayed());
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
		final String ip = getLatestIP();
		if (ip.equals(""))
			return name + " " + CrazyLightPluginInterface.DATETIMEFORMAT.format(getLastActionTime());
		else
			return name + " " + CrazyLightPluginInterface.DATETIMEFORMAT.format(getLastActionTime()) + " @ " + ip;
	}

	@Override
	public String getParameter(final CommandSender sender, final int index)
	{
		switch (index)
		{
			case 0:
				return getName();
			case 1:
				return CrazyLightPluginInterface.DATETIMEFORMAT.format(new Date());
			case 2:
				return isOnline() ? "Online" : "Offline";
			case 3:
				return getLatestIP();
			case 4:
				return "-";
			case 5:
				return isOnline() ? ChatColor.YELLOW.toString() : ChatColor.WHITE.toString();
			case 6:
				return ChatColor.DARK_GREEN.toString();
		}
		return "";
	}

	@Override
	public int getParameterCount()
	{
		return 4;
	}

	@Override
	public boolean isLoggedIn()
	{
		return false;
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
	@Localized({ "CRAZYLOGIN.PLAYERINFO.IPADDRESS $IP$", "CRAZYLOGIN.PLAYERINFO.ASSOCIATES $Associates$" })
	public void showDetailed(final CommandSender target, final String chatHeader)
	{
		if (!isOnline())
			return;
		final CrazyLocale locale = CrazyLocale.getLocaleHead().getSecureLanguageEntry("CRAZYLOGIN.PLAYERINFO");
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("IPADDRESS"), getLatestIP());
		final HashSet<String> associates = new HashSet<String>();
		for (final LoginPlayerData data : getPlugin().getPlayerDatasPerIP(getLatestIP()))
			associates.add(data.getName());
		ChatHelper.sendMessage(target, chatHeader, locale.getLanguageEntry("ASSOCIATES"), ChatHelper.listingString(associates));
	}
}
