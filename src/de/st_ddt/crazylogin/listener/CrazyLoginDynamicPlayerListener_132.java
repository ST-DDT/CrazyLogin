package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;

@SuppressWarnings("deprecation")
public class CrazyLoginDynamicPlayerListener_132 extends CrazyLoginDynamicPlayerListener
{

	public CrazyLoginDynamicPlayerListener_132(final CrazyLogin plugin, final CrazyLoginPlayerListener playerListener)
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
		plugin.requestLogin(player);
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
		plugin.requestLogin(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerChat(final AsyncPlayerChatEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.hasPlayerData(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
				if (playerdata.isLoggedIn())
				{
					playerdata.notifyAction();
					plugin.getCrazyDatabase().save(playerdata);
					return;
				}
		}
		else if (!plugin.isBlockingGuestChatEnabled())
			return;
		if (event.getMessage() != null)
			plugin.getCrazyLogger().log("ChatBlocked", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to chat", event.getMessage());
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}
}
