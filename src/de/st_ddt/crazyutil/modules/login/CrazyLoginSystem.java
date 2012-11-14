package de.st_ddt.crazyutil.modules.login;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyutil.modules.Module.Named;
import de.st_ddt.crazyutil.modules.Module.PluginDepency;

@Named(name = "CrazyLogin")
@PluginDepency(depend = "CrazyLogin")
public class CrazyLoginSystem implements LoginSystem
{

	private final CrazyLogin plugin;

	public CrazyLoginSystem()
	{
		super();
		plugin = CrazyLogin.getPlugin();
	}

	@Override
	public String getName()
	{
		return "CrazyLogin";
	}

	@Override
	public boolean hasAccount(final OfflinePlayer player)
	{
		return plugin.hasPlayerData(player);
	}

	@Override
	public boolean isLoggedIn(final Player player)
	{
		return plugin.isLoggedIn(player);
	}
}
