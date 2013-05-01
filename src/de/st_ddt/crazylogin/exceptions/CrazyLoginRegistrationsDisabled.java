package de.st_ddt.crazylogin.exceptions;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.source.Localized;

public class CrazyLoginRegistrationsDisabled extends CrazyException
{

	private static final long serialVersionUID = 2834302908669502218L;

	public CrazyLoginRegistrationsDisabled()
	{
		super();
	}

	@Override
	public String getLangPath()
	{
		return "CRAZYLOGIN.EXCEPTION.REGISTER.DISABLED";
	}

	@Override
	@Localized("CRAZYLOGIN.EXCEPTION.REGISTER.DISABLED")
	public void print(final CommandSender sender, final String header)
	{
		ChatHelper.sendMessage(sender, header, locale);
	}
}
