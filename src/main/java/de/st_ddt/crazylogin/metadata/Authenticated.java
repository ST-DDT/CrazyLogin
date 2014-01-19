package de.st_ddt.crazylogin.metadata;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import de.st_ddt.crazylogin.CrazyLogin;

public class Authenticated implements MetadataValue
{

	protected final CrazyLogin plugin;
	protected final OfflinePlayer offlinePlayer;
	protected Player player = null;

	public Authenticated(final CrazyLogin plugin, final OfflinePlayer player)
	{
		super();
		this.plugin = plugin;
		this.offlinePlayer = player;
	}

	@Override
	public Object value()
	{
		return asBoolean();
	}

	@Override
	public int asInt()
	{
		return asBoolean() ? 1 : 0;
	}

	@Override
	public float asFloat()
	{
		return asBoolean() ? 1 : 0;
	}

	@Override
	public double asDouble()
	{
		return asBoolean() ? 1 : 0;
	}

	@Override
	public long asLong()
	{
		return asBoolean() ? 1 : 0;
	}

	@Override
	public short asShort()
	{
		return asBoolean() ? (short) 1 : 0;
	}

	@Override
	public byte asByte()
	{
		return asBoolean() ? (byte) 1 : 0;
	}

	@Override
	public synchronized boolean asBoolean()
	{
		return true;
		// if (player == null)
		// player = offlinePlayer.getPlayer();
		// if (player == null)
		// return false;
		// else
		// return plugin.isLoggedIn(player);
	}

	@Override
	public String asString()
	{
		return Boolean.toString(asBoolean());
	}

	@Override
	public CrazyLogin getOwningPlugin()
	{
		return plugin;
	}

	@Override
	public synchronized void invalidate()
	{
		player = null;
	}
}
