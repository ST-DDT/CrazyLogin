package de.st_ddt.crazylogin.events;

import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.LoginPlayerData;

public class CrazyLoginLoginFailEvent extends CrazyLoginEvent
{

	protected final Player player;
	protected final LoginPlayerData data;
	protected final LoginFailReason reason;

	public CrazyLoginLoginFailEvent(final CrazyLogin plugin, final LoginPlayerData data, final Player player, final LoginFailReason reason)
	{
		super(plugin);
		this.player = player;
		this.data = data;
		this.reason = reason;
	}

	public Player getPlayer()
	{
		return player;
	}

	public LoginPlayerData getData()
	{
		return data;
	}

	public LoginFailReason getReason()
	{
		return reason;
	}
}
