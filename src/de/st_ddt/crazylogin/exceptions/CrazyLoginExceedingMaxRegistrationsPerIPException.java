package de.st_ddt.crazylogin.exceptions;

import java.util.Collection;
import java.util.TreeSet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.source.Localized;

public class CrazyLoginExceedingMaxRegistrationsPerIPException extends CrazyLoginException
{

	private static final long serialVersionUID = -1015895366114344961L;
	protected final int maxCount;
	private final TreeSet<String> associates = new TreeSet<String>();

	public CrazyLoginExceedingMaxRegistrationsPerIPException(final int maxCount, final OfflinePlayer... players)
	{
		super();
		this.maxCount = maxCount;
		for (final OfflinePlayer player : players)
			associates.add(player.getName());
	}

	public CrazyLoginExceedingMaxRegistrationsPerIPException(final int maxCount, final String... names)
	{
		super();
		this.maxCount = maxCount;
		for (final String name : names)
			associates.add(name);
	}

	public CrazyLoginExceedingMaxRegistrationsPerIPException(final int maxCount, final Collection<? extends LoginData> players)
	{
		super();
		this.maxCount = maxCount;
		for (final LoginData player : players)
			associates.add(player.getName());
	}

	@Override
	public String getLangPath()
	{
		return super.getLangPath() + ".ACCOUNTSPERIP.TOMUCH";
	}

	@Override
	@Localized("CRAZYLOGIN.EXCEPTION.ACCOUNTSPERIP.TOMUCH $MaxAllowed$ $Associates$")
	public void print(final CommandSender sender, final String header)
	{
		ChatHelper.sendMessage(sender, header, locale, maxCount, ChatHelper.listingString(associates));
	}
}
