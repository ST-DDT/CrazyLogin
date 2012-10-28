package de.st_ddt.crazylogin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.events.CrazyPlayerAssociatesEvent;
import de.st_ddt.crazyplugin.events.CrazyPlayerIPsConnectedToNameEvent;
import de.st_ddt.crazyplugin.events.CrazyPlayerNamesConnectedToIPEvent;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyutil.Named;

public final class CrazyLoginCrazyListener implements Listener
{

	protected final CrazyLogin plugin;
	protected final CrazyLoginPlayerListener playerListener;

	public CrazyLoginCrazyListener(final CrazyLogin plugin, final CrazyLoginPlayerListener playerListener)
	{
		super();
		this.plugin = plugin;
		this.playerListener = playerListener;
	}

	public LoginPlugin<?> getPlugin()
	{
		return plugin;
	}

	@EventHandler
	public void CrazyPlayerRemoveEvent(final CrazyPlayerRemoveEvent event)
	{
		if (plugin.deletePlayerData(event.getPlayer()))
			event.markDeletion((Named) plugin);
		if (playerListener.removeFromMovementBlocker(event.getPlayer()))
			event.markDeletion((Named) plugin);
		if (playerListener.dropPlayerData(event.getPlayer()))
			event.markDeletion((Named) plugin);
	}

	@EventHandler
	public void CrazyPlayerAssociatesEvent(final CrazyPlayerAssociatesEvent event)
	{
		final LoginPlayerData data = plugin.getPlayerData(event.getSearchedName());
		if (data == null)
			return;
		for (final String ip : data.getIPs())
			for (final LoginPlayerData players : plugin.getPlayerDatasPerIP(ip))
				event.add(players.getName());
	}

	@EventHandler
	public void CrazyPlayerIPsConnectedToNameEvent(final CrazyPlayerIPsConnectedToNameEvent event)
	{
		final LoginPlayerData data = plugin.getPlayerData(event.getSearchedName());
		if (data == null)
			return;
		event.addAll(data.getIPs());
	}

	@EventHandler
	public void CrazyPlayerNamesConnectedToIPEvent(final CrazyPlayerNamesConnectedToIPEvent event)
	{
		for (final LoginPlayerData players : plugin.getPlayerDatasPerIP(event.getSearchedIP()))
			event.add(players.getName());
	}
}
