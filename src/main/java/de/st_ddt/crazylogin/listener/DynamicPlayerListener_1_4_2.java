package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.st_ddt.crazylogin.CrazyLogin;

public class DynamicPlayerListener_1_4_2 extends DynamicPlayerListener
{

	public DynamicPlayerListener_1_4_2(final CrazyLogin plugin, final PlayerListener playerListener)
	{
		super(plugin, playerListener);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void HangingPlace(final HangingPlaceEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void HangingBreak(final HangingBreakByEntityEvent event)
	{
		if (!(event.getRemover() instanceof Player))
			return;
		final Player player = (Player) event.getRemover();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerChat(final AsyncPlayerChatEvent event)
	{
		if (!PlayerChat(event.getPlayer(), event.getMessage()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void PlayerChatHide(final AsyncPlayerChatEvent event)
	{
		if (plugin.isHidingChatEnabled())
			PlayerChatHide(event.getPlayer(), event.getRecipients());
	}
}
