package de.st_ddt.crazylogin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class CrazyLoginVehicleListener implements Listener
{

	private final CrazyLogin plugin;

	public CrazyLoginVehicleListener(CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler
	public void VehicleEnter(VehicleEnterEvent event)
	{
		if (!(event.getVehicle().getPassenger() instanceof Player))
			return;
		Player player = (Player) event.getVehicle().getPassenger();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
	}
}
