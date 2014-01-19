package de.st_ddt.crazylogin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import de.st_ddt.crazylogin.CrazyLogin;

public class DynamicPlayerListener_1_5 extends DynamicPlayerListener_1_4_2
{

	public DynamicPlayerListener_1_5(final CrazyLogin plugin, final PlayerListener playerListener)
	{
		super(plugin, playerListener);
	}

	@EventHandler(ignoreCancelled = true)
	public void PlayerItemConsume(final PlayerItemConsumeEvent event)
	{
		if (!plugin.isLoggedIn(event.getPlayer()))
			event.setCancelled(true);
	}
}
