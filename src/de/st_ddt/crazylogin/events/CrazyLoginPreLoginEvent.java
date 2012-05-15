package de.st_ddt.crazylogin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.LoginPlayerData;

public class CrazyLoginPreLoginEvent extends CrazyLoginEvent implements Cancellable
{

	protected final Player player;
	protected boolean cancelled = false;

	public CrazyLoginPreLoginEvent(CrazyLogin plugin, Player player, LoginPlayerData data)
	{
		super(plugin);
		this.player = player;
	}

	public Player getPlayer()
	{
		return player;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		this.cancelled = cancel;
	}
}
