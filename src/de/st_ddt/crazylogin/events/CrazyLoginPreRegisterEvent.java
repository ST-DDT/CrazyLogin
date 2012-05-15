package de.st_ddt.crazylogin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.LoginPlayerData;

public class CrazyLoginPreRegisterEvent extends CrazyLoginEvent implements Cancellable
{

	protected final Player player;
	protected final LoginPlayerData data;
	protected boolean cancelled = false;

	public CrazyLoginPreRegisterEvent(CrazyLogin plugin, Player player, LoginPlayerData data)
	{
		super(plugin);
		this.player = player;
		this.data = data;
	}

	public Player getPlayer()
	{
		return player;
	}

	public LoginPlayerData getData()
	{
		return data;
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
