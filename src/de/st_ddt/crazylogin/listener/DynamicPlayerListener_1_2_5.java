package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerChatEvent;

import de.st_ddt.crazylogin.CrazyLogin;

@SuppressWarnings("deprecation")
public class DynamicPlayerListener_1_2_5 extends DynamicPlayerListener
{

	public DynamicPlayerListener_1_2_5(final CrazyLogin plugin, final PlayerListener playerListener)
	{
		super(plugin, playerListener);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PaintingPlace(final PaintingPlaceEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendLoginReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PaintingBreak(final PaintingBreakByEntityEvent event)
	{
		if (!(event.getRemover() instanceof Player))
			return;
		final Player player = (Player) event.getRemover();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendLoginReminderMessage(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerChat(final PlayerChatEvent event)
	{
		if (!PlayerChat(event.getPlayer(), event.getMessage()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void PlayerChatHide(final PlayerChatEvent event)
	{
		if (plugin.isHidingChatEnabled())
			PlayerChatHide(event.getPlayer(), event.getRecipients());
	}
}
