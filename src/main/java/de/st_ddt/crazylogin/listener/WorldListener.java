package de.st_ddt.crazylogin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import de.st_ddt.crazylogin.CrazyLogin;

public final class WorldListener implements Listener
{

	protected final CrazyLogin plugin;

	public WorldListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler
	public void WorldLoadEvent(final WorldLoadEvent event)
	{
		plugin.loadConfigurationForWorld(event.getWorld());
	}
}
