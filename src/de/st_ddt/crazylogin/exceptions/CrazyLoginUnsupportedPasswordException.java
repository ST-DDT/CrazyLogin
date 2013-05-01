package de.st_ddt.crazylogin.exceptions;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.source.Localized;

public class CrazyLoginUnsupportedPasswordException extends CrazyCommandException
{

	private static final long serialVersionUID = 8895923307636294839L;

	public CrazyLoginUnsupportedPasswordException()
	{
		super();
	}

	@Override
	public String getLangPath()
	{
		return "CRAZYLOGIN.EXCEPTION.PASSWORD.UNSUPPORTED";
	}

	@Override
	@Localized("CRAZYLOGIN.EXCEPTION.PASSWORD.UNSUPPORTED")
	public void print(final CommandSender sender, final String header)
	{
		ChatHelper.sendMessage(sender, header, locale);
	}
}
