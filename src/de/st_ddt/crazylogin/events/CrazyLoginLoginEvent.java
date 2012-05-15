package de.st_ddt.crazylogin.events;

import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.LoginPlayerData;

public class CrazyLoginLoginEvent extends CrazyLoginEvent
{

	protected final Player player;
	protected final LoginPlayerData data;

	public CrazyLoginLoginEvent(final CrazyLogin plugin, final LoginPlayerData data, final Player player)
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
}
