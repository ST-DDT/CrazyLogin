package de.st_ddt.crazylogin;

import org.bukkit.common.login.LoginPlugin;

import de.st_ddt.crazylogin.data.LoginPlayerData;

public class CommonLoginAPIBridge implements LoginPlugin
{

	private final CrazyLogin plugin;

	public CommonLoginAPIBridge(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@Override
	public boolean hasAccount(final String player)
	{
		return plugin.hasPlayerData(player);
	}

	@Override
	public boolean isAccountRequired()
	{
		return plugin.isAlwaysNeedPassword();
	}

	@Override
	public boolean isLoggedIn(final String player)
	{
		final LoginPlayerData data = plugin.getPlayerData(player);
		if (data == null)
			return false;
		return data.isLoggedIn() && data.isOnline();
	}

	@Override
	public boolean checkPassword(final String player, final String password)
	{
		final LoginPlayerData data = plugin.getPlayerData(player);
		if (data == null)
			return false;
		return data.isPassword(password);
	}

	@Override
	public CrazyLogin getPlugin()
	{
		return plugin;
	}
}
