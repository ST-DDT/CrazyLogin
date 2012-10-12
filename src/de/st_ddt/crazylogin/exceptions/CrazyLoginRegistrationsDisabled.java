package de.st_ddt.crazylogin.exceptions;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.locales.Localized;

public class CrazyLoginRegistrationsDisabled extends CrazyLoginException
{

	private static final long serialVersionUID = 2834302908669502217L;

	public CrazyLoginRegistrationsDisabled()
	{
		super();
	}

	@Override
	public String getLangPath()
	{
		return super.getLangPath() + ".REGISTER.DISABLED";
	}

	@Override
	@Localized("CRAZYLOGIN.EXCEPTION.REGISTER.DISABLED")
	public void print(final CommandSender sender, final String header)
	{
		ChatHelper.sendMessage(sender, header, locale);
	}
}
