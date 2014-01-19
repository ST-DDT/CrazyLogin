package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import de.st_ddt.crazylogin.CrazyLogin;

public final class DynamicVehicleListener implements Listener
{

	private final CrazyLogin plugin;

	public DynamicVehicleListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler
	public void VehicleEnter(final VehicleEnterEvent event)
	{
		if (!(event.getVehicle().getPassenger() instanceof Player))
			return;
		final Player player = (Player) event.getVehicle().getPassenger();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler
	public void VehicleDamage(final VehicleDamageEvent event)
	{
		if (!(event.getAttacker() instanceof Player))
			return;
		final Player player = (Player) event.getAttacker();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}

	@EventHandler
	public void VehicleDestroy(final VehicleDestroyEvent event)
	{
		if (!(event.getAttacker() instanceof Player))
			return;
		final Player player = (Player) event.getAttacker();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendAuthReminderMessage(player);
	}
}
