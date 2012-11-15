package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerChatEvent;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;

@SuppressWarnings("deprecation")
public class CrazyLoginDynamicPlayerListener_125 extends CrazyLoginDynamicPlayerListener
{

	public CrazyLoginDynamicPlayerListener_125(final CrazyLogin plugin, final CrazyLoginPlayerListener playerListener)
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
	public void PlayerChat(final PlayerChatEvent event)
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
		plugin.getCrazyLogger().log("ChatBlocked", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to execute", event.getMessage());
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}
}
