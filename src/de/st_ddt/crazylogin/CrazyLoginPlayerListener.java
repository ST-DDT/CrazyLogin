package de.st_ddt.crazylogin;

import java.util.Date;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CrazyLoginPlayerListener implements Listener
{

	private final CrazyLogin plugin;
	private final HashMap<String, Location> savelogin = new HashMap<String, Location>();

	public CrazyLoginPlayerListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void PlayerLogin(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (!plugin.checkNameLength(event.getPlayer().getName()))
		{
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "NAME.INVALIDLENGTH", plugin.getMinNameLength(), plugin.getMaxNameLength()));
			return;
		}
		if (plugin.isForceSingleSessionEnabled())
			if (player.isOnline())
			{
				if (plugin.isForceSingleSessionSameIPBypassEnabled())
					if (player.getAddress() != null)
						if (event.getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
							if (event.getAddress().getHostName().equals(player.getAddress().getAddress().getHostName()))
								return;
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "SESSION.DUPLICATE"));
				plugin.broadcastLocaleMessage(true, "crazylogin.warnsession", "SESSION.DUPLICATEWARN", event.getAddress().getHostAddress(), player.getName());
				plugin.sendLocaleMessage("SESSION.DUPLICATEWARN", player, event.getAddress().getHostAddress(), player.getName());
			}
	}

	@EventHandler
	public void PlayerJoin(final PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		if (savelogin.get(player.getName().toLowerCase()) != null)
			player.teleport(savelogin.get(player.getName().toLowerCase()), TeleportCause.PLUGIN);
		if (!plugin.hasAccount(player))
		{
			if (plugin.isResettingGuestLocationsEnabled())
				if (savelogin.get(player.getName().toLowerCase()) == null)
					savelogin.put(player.getName().toLowerCase(), player.getLocation());
			if (plugin.isAlwaysNeedPassword())
				plugin.sendLocaleMessage("REGISTER.HEADER", player);
			else
				plugin.sendLocaleMessage("REGISTER.HEADER2", player);
			plugin.sendLocaleMessage("REGISTER.MESSAGE", player);
			final int autoKick = plugin.getAutoKickUnregistered();
			if (autoKick != -1)
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("REGISTER.REQUEST"), true), autoKick * 20);
			return;
		}
		final LoginData playerdata = plugin.getPlayerData(player);
		if (!playerdata.hasIP(player.getAddress().getAddress().getHostAddress()))
			playerdata.logout();
		if (plugin.isAutoLogoutEnabled())
			if (plugin.getAutoLogoutTime() * 1000 + playerdata.getLastActionTime().getTime() < new Date().getTime())
				playerdata.logout();
		if (plugin.isLoggedIn(player))
			return;
		if (savelogin.get(player.getName().toLowerCase()) == null)
			savelogin.put(player.getName().toLowerCase(), player.getLocation());
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
		final int autoKick = plugin.getAutoKick();
		if (autoKick >= 10)
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("LOGIN.REQUEST")), autoKick * 20);
	}

	@EventHandler
	public void PlayerQuit(final PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		final LoginData playerdata = plugin.getPlayerData(player);
		if (playerdata != null)
		{
			if (!plugin.isLoggedIn(player))
				return;
			playerdata.notifyAction();
			if (plugin.isInstantAutoLogoutEnabled())
				playerdata.logout();
		}
	}

	@EventHandler
	public void PlayerDropItem(final PlayerDropItemEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerInteract(final PlayerInteractEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerInteractEntity(final PlayerInteractEntityEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerMove(final PlayerMoveEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		final Location current = savelogin.get(player.getName().toLowerCase());
		if (current != null)
			if (current.getWorld() == event.getTo().getWorld())
				if (current.distance(event.getTo()) < plugin.getMoveRange())
					return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerTeleport(final PlayerTeleportEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		if (savelogin.get(player.getName().toLowerCase()) == null)
			return;
		if (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.UNKNOWN)
		{
			savelogin.put(player.getName().toLowerCase(), event.getTo());
			return;
		}
		final Location target = event.getTo();
		if (target.distance(target.getWorld().getSpawnLocation()) < 10)
		{
			savelogin.put(player.getName().toLowerCase(), event.getTo());
			return;
		}
		if (player.getBedSpawnLocation() != null)
			if (target.getWorld() == player.getBedSpawnLocation().getWorld())
				if (target.distance(player.getBedSpawnLocation()) < 10)
				{
					savelogin.put(player.getName().toLowerCase(), event.getTo());
					return;
				}
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerDamage(final EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerDamage(final EntityDamageByBlockEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getBedSpawnLocation();
		if (location == null)
			location = player.getWorld().getSpawnLocation();
		player.teleport(location, TeleportCause.PLUGIN);
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerDamage(final EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getBedSpawnLocation();
		if (location == null)
			location = player.getWorld().getSpawnLocation();
		player.teleport(location, TeleportCause.PLUGIN);
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerPreCommand(final PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		if (!plugin.isBlockingGuestCommandsEnabled() || plugin.hasAccount(player))
			if (plugin.isLoggedIn(player))
				return;
		final String message = event.getMessage().toLowerCase();
		if (message.startsWith("/"))
		{
			if (message.startsWith("/login") || message.startsWith("/crazylogin password") || message.startsWith("/crazylanguage") || message.startsWith("/language") || message.startsWith("/register"))
				return;
			for (final String command : plugin.getCommandWhiteList())
				if (message.startsWith(command))
					return;
			event.setCancelled(true);
			plugin.broadcastLocaleMessage(true, "crazylogin.warncommandexploits", "COMMAND.EXPLOITWARN", player.getName(), player.getAddress().getAddress().getHostAddress(), message);
			if (plugin.isAutoKickCommandUsers())
			{
				player.kickPlayer(plugin.getLocale().getLocaleMessage(player, "LOGIN.REQUEST"));
				return;
			}
			plugin.requestLogin(event.getPlayer());
			return;
		}
	}

	@EventHandler
	public void PlayerCommand(final PlayerChatEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	public void notifyLogin(Player player)
	{
		savelogin.remove(player.getName().toLowerCase());
	}

	public void clearSaveLogin(boolean guestsOnly)
	{
		if (guestsOnly)
		{
			for (String name : savelogin.keySet())
				if (!plugin.hasAccount(name))
					savelogin.remove(name);
		}
		else
			savelogin.clear();
	}
}
