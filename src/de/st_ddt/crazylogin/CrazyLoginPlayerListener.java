package de.st_ddt.crazylogin;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.st_ddt.crazyutil.PairList;

public class CrazyLoginPlayerListener implements Listener
{

	private final CrazyLogin plugin;
	private final PairList<String, LoginPlayerData> datas;
	private final PairList<Player, Location> savelogin = new PairList<Player, Location>();

	public CrazyLoginPlayerListener(CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
		this.datas = plugin.getPlayerData();
	}

	@EventHandler
	public void PlayerLogin(PlayerPreLoginEvent event)
	{
		if (!plugin.isForceSingleSessionEnabled())
			return;
		String name = event.getName();
		Player player = plugin.getServer().getPlayerExact(name);
		if (player == null)
			return;
		if (!player.isOnline())
			return;
		// if (player.getAddress().getAddress().getHostAddress().equals(event.getAddress().getHostAddress()))
		// return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage("You are already online! Please wait for client timeout or contact an operator.");
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		savelogin.setDataVia1(player, player.getLocation());
		LoginPlayerData playerdata = datas.findDataVia1(player.getName().toLowerCase());
		if (playerdata == null)
		{
			if (plugin.isAlwaysNeedPassword())
				plugin.sendLocaleMessage("REGISTER.HEADER", player);
			else
				plugin.sendLocaleMessage("REGISTER.HEADER2", player);
			plugin.sendLocaleMessage("REGISTER.MESSAGE", player);
			return;
		}
		if (!playerdata.hasIP(player.getAddress().getAddress().getHostAddress()))
			playerdata.logout();
		if (plugin.getAutoLogoutTime() * 1000 + playerdata.getLastActionTime().getTime() < new Date().getTime())
			playerdata.logout();
		if (plugin.isLoggedIn(player))
			return;
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
		int autoKick = plugin.getAutoKick();
		if (autoKick >= 10)
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("LOGIN.REQUEST")), autoKick * 20);
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		LoginPlayerData playerdata = datas.findDataVia1(player.getName().toLowerCase());
		if (playerdata != null)
		{
			if (!plugin.isLoggedIn(player))
				return;
			playerdata.notifyAction();
			playerdata.logout();
		}
	}

	@EventHandler
	public void PlayerDropItem(PlayerDropItemEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerInteract(PlayerInteractEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		Location current = savelogin.findDataVia1(player);
		if (current != null)
			if (current.getWorld() == event.getTo().getWorld())
				if (current.distance(event.getTo()) < 5)
					return;
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerTeleport(PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		if (event.getCause() == TeleportCause.PLUGIN)
			return;
		Location target = event.getTo();
		if (target.distance(target.getWorld().getSpawnLocation()) < 10)
		{
			savelogin.setDataVia1(player, event.getTo());
			return;
		}
		if (player.getBedSpawnLocation() != null)
			if (target.getWorld() == player.getBedSpawnLocation().getWorld())
				if (target.distance(player.getBedSpawnLocation()) < 10)
				{
					savelogin.setDataVia1(player, event.getTo());
					return;
				}
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}

	@EventHandler
	public void PlayerDamage(EntityDamageByBlockEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getBedSpawnLocation();
		if (location == null)
			location = player.getWorld().getSpawnLocation();
		player.teleport(location, TeleportCause.PLUGIN);
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getBedSpawnLocation();
		if (location == null)
			location = player.getWorld().getSpawnLocation();
		player.teleport(location, TeleportCause.PLUGIN);
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerPreCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		String message = event.getMessage().toLowerCase();
		if (message.startsWith("/"))
		{
			if (message.startsWith("/login") || message.startsWith("/crazylogin password") || message.startsWith("/crazylanguage") || message.startsWith("/language") || message.startsWith("/register"))
				return;
			for (String command : plugin.getCommandWhiteList())
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
	public void PlayerCommand(PlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
		return;
	}
}
