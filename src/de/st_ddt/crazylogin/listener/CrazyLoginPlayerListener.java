package de.st_ddt.crazylogin.listener;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.tasks.ScheduledKickTask;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyutil.PlayerSaver;

public class CrazyLoginPlayerListener implements Listener
{

	private final CrazyLogin plugin;
	private final HashMap<String, Location> movementBlocker = new HashMap<String, Location>();
	private final HashMap<String, Location> savelogin = new HashMap<String, Location>();
	private final HashMap<String, PlayerSaver> hidenInventory = new HashMap<String, PlayerSaver>();

	public CrazyLoginPlayerListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerLoginBanCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isTempBanned(event.getAddress().getHostAddress()))
		{
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage(ChatColor.RED + "Banned until : " + ChatColor.YELLOW + plugin.getTempBannedString(event.getAddress().getHostAddress()));
			plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of a temporary ban");
			return;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void PlayerLoginNameCharCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (!plugin.checkNameChars(player.getName()))
		{
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "NAME.INVALIDCHARS"));
			plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid chars");
			return;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void PlayerLoginNameLengthCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (!plugin.checkNameLength(event.getPlayer().getName()))
		{
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "NAME.INVALIDLENGTH", plugin.getMinNameLength(), plugin.getMaxNameLength()));
			plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid name length");
			return;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void PlayerLoginSessionCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isForceSingleSessionEnabled())
			if (player.isOnline())
			{
				if (plugin.isForceSingleSessionSameIPBypassEnabled())
				{
					final LoginPlayerData data = plugin.getPlayerData(player);
					if (data != null)
						if (event.getAddress().getHostAddress().equals(data.getLatestIP()))
							return;
				}
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "SESSION.DUPLICATE"));
				plugin.broadcastLocaleMessage(true, "crazylogin.warnsession", "SESSION.DUPLICATEWARN", event.getAddress().getHostAddress(), player.getName());
				plugin.sendLocaleMessage("SESSION.DUPLICATEWARN", player, event.getAddress().getHostAddress(), player.getName());
				plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of a player with this name being already online");
				return;
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void PlayerLoginConnectionCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		final int maxOnlinesPerIP = plugin.getMaxOnlinesPerIP();
		if (maxOnlinesPerIP != -1)
			if (plugin.getOnlinePlayersPerIP(event.getAddress().getHostAddress()).size() >= maxOnlinesPerIP)
			{
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(ChatColor.RED + "Too many connections");
				plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of to many connections for this IP");
				return;
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void PlayerLoginDataUpdate(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		plugin.updateAccount(player.getName());
		if (!plugin.isBlockingGuestJoinEnabled() || plugin.hasPlayerData(player))
			return;
		event.setResult(Result.KICK_WHITELIST);
		event.setKickMessage(ChatColor.RED + "You need an account to join this server.");
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of he has no account!");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void PlayerJoin(final PlayerJoinEvent event)
	{
		PlayerJoin(event.getPlayer());
	}

	public void PlayerJoin(final Player player)
	{
		if (movementBlocker.get(player.getName().toLowerCase()) != null)
			player.teleport(movementBlocker.get(player.getName().toLowerCase()), TeleportCause.PLUGIN);
		if (!plugin.hasPlayerData(player))
		{
			if (plugin.isAlwaysNeedPassword())
			{
				plugin.sendLocaleMessage("REGISTER.HEADER", player);
				if (movementBlocker.get(player.getName().toLowerCase()) == null)
					movementBlocker.put(player.getName().toLowerCase(), player.getLocation());
			}
			else
				plugin.sendLocaleMessage("REGISTER.HEADER2", player);
			plugin.sendLocaleMessage("REGISTER.MESSAGE", player);
			final int autoKick = plugin.getAutoKickUnregistered();
			if (autoKick != -1)
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("REGISTER.REQUEST"), true), autoKick * 20);
			plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server (No Account)");
			return;
		}
		final LoginPlayerData playerdata = plugin.getPlayerData(player);
		if (!playerdata.hasIP(player.getAddress().getAddress().getHostAddress()))
			playerdata.logout();
		playerdata.checkTimeOut(plugin, false);
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getLocation();
		if (plugin.isForceSaveLoginEnabled())
		{
			triggerSaveLogin(player);
			location = player.getWorld().getSpawnLocation();
		}
		if (plugin.isHidingInventoryEnabled())
		{
			triggerHidenInventory(player);
		}
		if (movementBlocker.get(player.getName().toLowerCase()) == null)
			movementBlocker.put(player.getName().toLowerCase(), location);
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
		plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server");
		final int autoKick = plugin.getAutoKick();
		if (autoKick >= 10)
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("LOGIN.REQUEST"), plugin.getAutoTempBan()), autoKick * 20);
	}

	@EventHandler
	public void PlayerQuit(final PlayerQuitEvent event)
	{
		PlayerQuit(event.getPlayer());
	}

	public void PlayerQuit(final Player player)
	{
		plugin.getCrazyLogger().log("Quit", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " left the server");
		disableSaveLogin(player);
		disableHidenInventory(player);
		final LoginPlayerData playerdata = plugin.getPlayerData(player);
		if (playerdata == null)
		{
			if (plugin.isRemovingGuestDataEnabled())
				new CrazyPlayerRemoveEvent(plugin, player).callAsyncEvent();
		}
		else
		{
			if (!playerdata.isLoggedIn())
				return;
			playerdata.notifyAction();
			if (plugin.isInstantAutoLogoutEnabled())
				playerdata.logout();
			plugin.getCrazyDatabase().save(playerdata);
		}
	}

	public void PlayerQuit2(final Player player)
	{
		plugin.getCrazyLogger().log("Quit", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " left the server");
		disableSaveLogin(player);
		disableHidenInventory(player);
		final LoginPlayerData playerdata = plugin.getPlayerData(player);
		if (playerdata != null)
		{
			if (!playerdata.isLoggedIn())
				return;
			playerdata.notifyAction();
			if (plugin.isInstantAutoLogoutEnabled())
				playerdata.logout();
			plugin.getCrazyDatabase().save(playerdata);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInventoryOpen(final InventoryOpenEvent event)
	{
		if (!(event.getPlayer() instanceof Player))
			return;
		final Player player = (Player) event.getPlayer();
		if (plugin.isLoggedIn(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
			{
				playerdata.notifyAction();
				plugin.getCrazyDatabase().save(playerdata);
			}
			return;
		}
		event.setCancelled(true);
		player.closeInventory();
		plugin.requestLogin(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInventoryClick(final InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
			return;
		final Player player = (Player) event.getWhoClicked();
		if (plugin.isLoggedIn(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
			{
				playerdata.notifyAction();
				plugin.getCrazyDatabase().save(playerdata);
			}
			return;
		}
		event.setCancelled(true);
		player.closeInventory();
		plugin.requestLogin(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerPickupItem(final PlayerPickupItemEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		player.closeInventory();
		event.setCancelled(true);
		plugin.requestLogin(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerDropItem(final PlayerDropItemEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.requestLogin(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInteract(final PlayerInteractEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInteractEntity(final PlayerInteractEntityEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerMove(final PlayerMoveEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		final Location current = movementBlocker.get(player.getName().toLowerCase());
		if (current == null)
			return;
		double dist = Double.MAX_VALUE;
		final double moveRange = plugin.getMoveRange();
		if (current.getWorld() == event.getTo().getWorld())
			dist = current.distance(event.getTo());
		if (dist >= moveRange)
		{
			if (dist >= moveRange * 2)
				player.teleport(current, TeleportCause.PLUGIN);
			event.setCancelled(true);
			plugin.requestLogin(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerTeleport(final PlayerTeleportEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
			{
				playerdata.notifyAction();
				plugin.getCrazyDatabase().save(playerdata);
			}
			return;
		}
		if (movementBlocker.get(player.getName().toLowerCase()) == null)
			return;
		if (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.UNKNOWN)
		{
			movementBlocker.put(player.getName().toLowerCase(), event.getTo());
			return;
		}
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerHeal(final EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerFood(final FoodLevelChangeEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerDamage(final EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerDamage(final EntityDamageByBlockEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		triggerSaveLogin(player);
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerDamage(final EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		triggerSaveLogin(player);
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerDamageDeal(final EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;
		final Player player = (Player) event.getDamager();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerPreCommand(final PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.hasPlayerData(player))
		{
			if (plugin.isLoggedIn(player))
				return;
		}
		else if (!plugin.isBlockingGuestCommandsEnabled())
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
			final String IP = player.getAddress().getAddress().getHostAddress();
			if (plugin.isAutoKickCommandUsers())
			{
				player.kickPlayer(plugin.getLocale().getLocaleMessage(player, "LOGIN.REQUEST"));
				plugin.getCrazyLogger().log("CommandBlocked", player.getName() + " @ " + IP + " has been kicked for trying to execute", event.getMessage());
				plugin.broadcastLocaleMessage(true, "crazylogin.warncommandexploits", "COMMAND.EXPLOITWARN", player.getName(), IP, event.getMessage().replaceAll("\\$", "_"));
				return;
			}
			plugin.requestLogin(event.getPlayer());
			plugin.getCrazyLogger().log("CommandBlocked", player.getName() + " @ " + IP + " tried to execute", event.getMessage());
			plugin.broadcastLocaleMessage(true, "crazylogin.warncommandexploits", "COMMAND.EXPLOITWARN", player.getName(), IP, event.getMessage().replaceAll("\\$", "_"));
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerChat(final AsyncPlayerChatEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.hasPlayerData(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
			{
				playerdata.notifyAction();
				plugin.getCrazyDatabase().save(playerdata);
			}
			return;
		}
		else if (!plugin.isBlockingGuestChatEnabled())
			return;
		plugin.getCrazyLogger().log("ChatBlocked", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to execute", event.getMessage());
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	public void addToMovementBlocker(final Player player)
	{
		addToMovementBlocker(player.getName(), player.getLocation());
	}

	public void addToMovementBlocker(final String player, final Location location)
	{
		movementBlocker.put(player.toLowerCase(), location);
	}

	public boolean removeFromMovementBlocker(final OfflinePlayer player)
	{
		return removeFromMovementBlocker(player.getName());
	}

	public boolean removeFromMovementBlocker(final String player)
	{
		return movementBlocker.remove(player.toLowerCase()) != null;
	}

	public void clearMovementBlocker(final boolean guestsOnly)
	{
		if (guestsOnly)
		{
			for (final String name : movementBlocker.keySet())
				if (!plugin.hasPlayerData(name))
					movementBlocker.remove(name);
		}
		else
			movementBlocker.clear();
	}

	public void triggerSaveLogin(final Player player)
	{
		if (savelogin.get(player.getName().toLowerCase()) == null)
			savelogin.put(player.getName().toLowerCase(), player.getLocation());
		player.teleport(player.getWorld().getSpawnLocation(), TeleportCause.PLUGIN);
	}

	public void disableSaveLogin(final Player player)
	{
		final Location location = savelogin.remove(player.getName().toLowerCase());
		if (location == null)
			return;
		player.teleport(location, TeleportCause.PLUGIN);
	}

	public void triggerHidenInventory(final Player player)
	{
		if (hidenInventory.get(player.getName().toLowerCase()) == null)
		{
			final PlayerSaver saver = new PlayerSaver(player, true);
			hidenInventory.put(player.getName().toLowerCase(), saver);
		}
	}

	public void disableHidenInventory(final Player player)
	{
		final PlayerSaver saver = hidenInventory.remove(player.getName().toLowerCase());
		if (saver == null)
			return;
		saver.restore(player);
	}

	public boolean dropPlayerData(final String player)
	{
		return (savelogin.remove(player.toLowerCase()) != null) || (hidenInventory.remove(player.toLowerCase()) != null);
	}
}
