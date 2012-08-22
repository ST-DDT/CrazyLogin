package de.st_ddt.crazylogin.exceptions;

import java.util.Collection;
import java.util.TreeSet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExceedingLimitsException;
import de.st_ddt.crazyutil.ChatHelper;

public class CrazyLoginMaxRegistrationsPerIPReached extends CrazyCommandExceedingLimitsException
{

	private static final long serialVersionUID = -1015895366114344961L;
	private final TreeSet<String> associates = new TreeSet<String>();

	public CrazyLoginMaxRegistrationsPerIPReached(final int limit, final OfflinePlayer... players)
	{
		super("Max Registrations per IP", false, limit);
		for (final OfflinePlayer player : players)
			associates.add(player.getName());
	}

	public CrazyLoginMaxRegistrationsPerIPReached(final int limit, final String... names)
	{
		super("Max Registrations per IP", false, limit);
		for (final String name : names)
			associates.add(name);
	}

	public CrazyLoginMaxRegistrationsPerIPReached(final int limit, final Collection<? extends LoginData> players)
	{
		super("Max Registrations per IP", false, limit);
		for (final LoginData player : players)
			associates.add(player.getName());
	}

	@Override
	public String getLangPath()
	{
		return "CRAZYLOGIN.COMMAND.REGISTER.ERROR.MAXREGISTRATIONSPERIP";
	}

	@Override
	public void print(final CommandSender sender, final String header)
	{
		sender.sendMessage(header + locale.getLocaleMessage(sender, "HEAD"));
		sender.sendMessage(header + locale.getLocaleMessage(sender, "LIMIT", limit));
		sender.sendMessage(header + locale.getLocaleMessage(sender, "ASSOCIATES", ChatHelper.listingString(associates)));
	}
}
