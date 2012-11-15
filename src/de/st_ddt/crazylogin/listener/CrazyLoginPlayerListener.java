package de.st_ddt.crazylogin.listener;

import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.tasks.ScheduledKickTask;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyutil.PlayerSaver;
import de.st_ddt.crazyutil.locales.Localized;

public class CrazyLoginPlayerListener implements Listener
{

	protected final CrazyLogin plugin;
	private final HashMap<String, Location> movementBlocker = new HashMap<String, Location>();
	private final HashMap<String, Location> savelogin = new HashMap<String, Location>();
	private final HashMap<String, PlayerSaver> hidenInventory = new HashMap<String, PlayerSaver>();

	public CrazyLoginPlayerListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	@Localized("CRAZYLOGIN.KICKED.BANNED.UNTIL $BannedUntil$")
	public void PlayerLoginBanCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isTempBanned(event.getAddress().getHostAddress()))
		{
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.BANNED.UNTIL", plugin.getTempBannedString(event.getAddress().getHostAddress())));
			plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of a temporary ban");
			return;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	@Localized("CRAZYLOGIN.KICKED.NAME.INVALIDCHARS")
	public void PlayerLoginNameCharCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.checkNameChars(player.getName()))
			return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NAME.INVALIDCHARS"));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid chars");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	@Localized("CRAZYLOGIN.KICKED.NAME.INVALIDCASE")
	public void PlayerLoginNameCaseCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.checkNameCase(player.getName()))
			return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NAME.INVALIDCASE"));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid name case");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	@Localized("CRAZYLOGIN.KICKED.NAME.INVALIDLENGTH $MinLength$ $MaxLength$")
	public void PlayerLoginNameLengthCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.checkNameLength(event.getPlayer().getName()))
			return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NAME.INVALIDLENGTH", plugin.getMinNameLength(), plugin.getMaxNameLength()));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid name length");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	@Localized({ "CRAZYLOGIN.KICKED.SESSION.DUPLICATE", "CRAZYLOGIN.SESSION.DUPLICATEWARN $Name$ $IP$" })
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
				event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.SESSION.DUPLICATE"));
				plugin.broadcastLocaleMessage(true, "crazylogin.warnsession", true, "SESSION.DUPLICATEWARN", player.getName(), event.getAddress().getHostAddress());
				plugin.sendLocaleMessage("SESSION.DUPLICATEWARN", player, event.getAddress().getHostAddress(), player.getName());
				plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of a player with this name being already online");
				return;
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	@Localized("CRAZYLOGIN.KICKED.CONNECTIONS.TOMUCH")
	public void PlayerLoginConnectionCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		final int maxOnlinesPerIP = plugin.getMaxOnlinesPerIP();
		if (maxOnlinesPerIP != -1)
			if (plugin.getOnlinePlayersPerIP(event.getAddress().getHostAddress()).size() >= maxOnlinesPerIP)
			{
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.CONNECTIONS.TOMUCH"));
				plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of to many connections for this IP");
				return;
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	@Localized("CRAZYLOGIN.KICKED.NOACCOUNT")
	public void PlayerLoginDataUpdate(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		final LoginPlayerData data = plugin.getCrazyDatabase().updateEntry(player.getName());
		if (!plugin.isBlockingGuestJoinEnabled() || data != null)
			return;
		event.setResult(Result.KICK_WHITELIST);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NOACCOUNT"));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of he has no account!");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void PlayerJoin(final PlayerJoinEvent event)
	{
		PlayerJoin(event.getPlayer());
	}

	@Localized({ "CRAZYLOGIN.REGISTER.HEADER", "CRAZYLOGIN.REGISTER.HEADER2", "CRAZYLOGIN.REGISTER.REQUEST", "CRAZYLOGIN.LOGIN.REQUEST" })
	public void PlayerJoin(final Player player)
	{
		if (movementBlocker.get(player.getName().toLowerCase()) != null)
			player.teleport(movementBlocker.get(player.getName().toLowerCase()), TeleportCause.PLUGIN);
		if (!plugin.hasPlayerData(player))
		{
			if (plugin.isAlwaysNeedPassword())
			{
				if (plugin.isHidingPlayerEnabled())
					for (Player other : Bukkit.getOnlinePlayers())
						if (player != other)
							other.hidePlayer(player);
				Location location = player.getLocation().clone();
				if (plugin.isForceSaveLoginEnabled())
				{
					triggerSaveLogin(player);
					location = player.getWorld().getSpawnLocation().clone();
				}
				if (plugin.isHidingInventoryEnabled())
					triggerHidenInventory(player);
				if (movementBlocker.get(player.getName().toLowerCase()) == null)
					movementBlocker.put(player.getName().toLowerCase(), location);
				plugin.sendLocaleMessage("REGISTER.HEADER", player);
			}
			else if (!plugin.isAvoidingSpammedRegisterRequestsEnabled() || new Date().getTime() - player.getFirstPlayed() < 60000)
				plugin.sendLocaleMessage("REGISTER.HEADER2", player);
			final int autoKick = plugin.getAutoKickUnregistered();
			if (autoKick != -1)
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("REGISTER.REQUEST"), true), autoKick * 20);
			plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server (No Account)");
			plugin.registerDynamicHooks();
			return;
		}
		final LoginPlayerData playerdata = plugin.getPlayerData(player);
		if (!playerdata.hasIP(player.getAddress().getAddress().getHostAddress()))
			playerdata.logout();
		playerdata.checkTimeOut();
		if (plugin.isLoggedIn(player))
			return;
		if (plugin.isHidingPlayerEnabled())
			for (Player other : Bukkit.getOnlinePlayers())
				if (player != other)
					other.hidePlayer(player);
		Location location = player.getLocation().clone();
		if (plugin.isForceSaveLoginEnabled())
		{
			triggerSaveLogin(player);
			location = player.getWorld().getSpawnLocation().clone();
		}
		if (plugin.isHidingInventoryEnabled())
			triggerHidenInventory(player);
		if (movementBlocker.get(player.getName().toLowerCase()) == null)
			movementBlocker.put(player.getName().toLowerCase(), location);
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
		plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server");
		final int autoKick = plugin.getAutoKick();
		if (autoKick >= 10)
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("LOGIN.REQUEST"), plugin.getAutoTempBan()), autoKick * 20);
		plugin.registerDynamicHooks();
	}

	@EventHandler
	public void PlayerQuit(final PlayerQuitEvent event)
	{
		PlayerQuit(event.getPlayer());
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			@Override
			public void run()
			{
				plugin.unregisterDynamicHooks();
			}
		}, 5);
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

	public HashMap<String, Location> getMovementBlocker()
	{
		return movementBlocker;
	}

	public void triggerSaveLogin(final Player player)
	{
		if (savelogin.get(player.getName().toLowerCase()) == null)
			savelogin.put(player.getName().toLowerCase(), player.getLocation().clone());
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
