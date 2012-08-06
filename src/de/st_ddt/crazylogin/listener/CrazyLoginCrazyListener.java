package de.st_ddt.crazylogin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;

public class CrazyLoginCrazyListener implements Listener
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
			event.markDeletion(plugin);
		if (playerListener.removeFromMovementBlocker(event.getPlayer()))
			event.markDeletion(plugin);
		if (playerListener.dropPlayerData(event.getPlayer()))
			event.markDeletion(plugin);
	}
}
