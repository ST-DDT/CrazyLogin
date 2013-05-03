package de.st_ddt.crazylogin.listener;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.source.Localized;

public class DynamicPlayerListener implements Listener
{

	protected final static Pattern PATTERN_SPACE = Pattern.compile(" ");
	protected final CrazyLogin plugin;
	private final PlayerListener playerListener;

	public DynamicPlayerListener(final CrazyLogin plugin, final PlayerListener playerListener)
	{
		super();
		this.plugin = plugin;
		this.playerListener = playerListener;
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
				playerdata.notifyAction();
			return;
		}
		event.setCancelled(true);
		player.closeInventory();
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInventoryClick(final InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
			return;
		final Player player = (Player) event.getWhoClicked();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		player.closeInventory();
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerPickupItem(final PlayerPickupItemEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerDropItem(final PlayerDropItemEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInteract(final PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerInteractEntity(final PlayerInteractEntityEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerMove(final PlayerMoveEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		final Location current = playerListener.getMovementBlocker(player);
		if (current == null)
			return;
		final Location target = event.getTo();
		final double moveRange = plugin.getMoveRange();
		if (moveRange == 0)
		{
			final Location validLocation = playerListener.getLastValidLocation(player);
			if (validLocation == null)
			{
				event.setTo(current.clone());
				playerListener.setLastValidLocation(player, current);
			}
			else
				event.setTo(validLocation.clone());
			plugin.sendAuthReminderMessage(event.getPlayer());
		}
		else
		{
			final double dist;
			if (current.getWorld() == target.getWorld())
				dist = current.distance(target);
			else
				dist = Double.MAX_VALUE;
			if (dist >= moveRange)
			{
				final Location validLocation = playerListener.getLastValidLocation(player);
				if (validLocation == null)
				{
					event.setTo(current.clone());
					playerListener.setLastValidLocation(player, current);
				}
				else
				{
					validLocation.setYaw(target.getYaw());
					validLocation.setPitch(target.getPitch());
					event.setTo(validLocation.clone());
				}
				plugin.sendAuthReminderMessage(event.getPlayer());
			}
			else
				playerListener.setLastValidLocation(player, target);
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
				playerdata.notifyAction();
			return;
		}
		if (playerListener.getMovementBlocker(player) == null)
			return;
		switch (event.getCause())
		{
			case PLUGIN:
				playerListener.setMovementBlocker(player, event.getTo());
			case UNKNOWN:
				return;
			default:
				event.setCancelled(true);
				plugin.sendAuthReminderMessage(event.getPlayer());
		}
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
	public void PlayerShear(final PlayerShearEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		final Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerBedEnter(final PlayerBedEnterEvent event)
	{
		if (!plugin.isLoggedIn(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void PlayerFish(final PlayerFishEvent event)
	{
		if (!plugin.isLoggedIn(event.getPlayer()))
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
		playerListener.triggerSaveLogin(player);
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

	@EventHandler(priority = EventPriority.HIGH)
	@Localized({ "CRAZYLOGIN.KICKED.COMMANDUSAGE", "CRAZYLOGIN.COMMAND.EXPLOITWARN $Name$ $IP$ $Command$" })
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
			for (final String command : plugin.getCommandWhiteList())
				if (message.matches(command))
					return;
			event.setCancelled(true);
			final String IP = player.getAddress().getAddress().getHostAddress();
			if (plugin.isAutoKickCommandUsers())
			{
				player.kickPlayer(plugin.getLocale().getLocaleMessage(player, "KICKED.COMMANDUSAGE"));
				plugin.getCrazyLogger().log("CommandBlocked", player.getName() + " @ " + IP + " has been kicked for trying to execute", event.getMessage());
			}
			else
			{
				plugin.sendAuthReminderMessage(player);
				plugin.getCrazyLogger().log("CommandBlocked", player.getName() + " @ " + IP + " tried to execute", event.getMessage());
			}
			if (!plugin.isHidingWarningsEnabled())
				plugin.broadcastLocaleMessage(true, "crazylogin.warncommandexploits", true, "COMMAND.EXPLOITWARN", player.getName(), IP, event.getMessage().replaceAll("\\$", "_"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void PlayerPreCommandHiddenPassword(final PlayerCommandPreprocessEvent event)
	{
		if (!plugin.isHidingPasswordsFromConsoleEnabled())
			return;
		final Player player = event.getPlayer();
		final String message = event.getMessage().substring(1).toLowerCase();
		final String[] split = PATTERN_SPACE.split(message);
		final PluginCommand login = plugin.getCommand("login");
		if (event.isCancelled())
			return;
		if ("login".equals(split[0]) || login.getAliases().contains(split[0]))
		{
			login.execute(player, split[0], ChatHelperExtended.shiftArray(split, 1));
			event.setCancelled(true);
			return;
		}
		final PluginCommand register = plugin.getCommand("register");
		if ("register".equals(split[0]) || register.getAliases().contains(split[0]) || message.startsWith("cl password") || message.startsWith("crazylogin password"))
		{
			register.execute(player, split[0], ChatHelperExtended.shiftArray(split, 1));
			event.setCancelled(true);
			return;
		}
	}

	protected final boolean PlayerChat(final Player player, final String message)
	{
		if (plugin.hasPlayerData(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
				if (playerdata.isLoggedIn())
				{
					playerdata.notifyAction();
					return true;
				}
		}
		else if (!plugin.isBlockingGuestChatEnabled())
			return true;
		if (message != null)
			plugin.getCrazyLogger().log("ChatBlocked", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to chat", message);
		plugin.sendAuthReminderMessage(player);
		return false;
	}

	protected final void PlayerChatHide(final Player player, final Set<Player> recipients)
	{
		try
		{
			final Iterator<Player> it = recipients.iterator();
			while (it.hasNext())
			{
				final Player recipient = it.next();
				if (!plugin.isLoggedIn(recipient))
					it.remove();
			}
			recipients.add(player.getPlayer());
		}
		catch (final UnsupportedOperationException e)
		{}
	}
}
