package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.source.Localized;

public class CommandPlayerExpirePassword extends CommandExecutor
{

	public CommandPlayerExpirePassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.PLAYER.EXPIREPASSWORD.SUCCESS $Player$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		final String arg = ChatHelper.listingString(" ", args);
		if (arg.equals("*"))
		{
			for (final LoginPlayerData data : owner.getPlayerData())
				owner.expirePassword(data);
			owner.sendLocaleMessage("COMMAND.PLAYER.EXPIREPASSWORD.SUCCESS", sender, arg);
		}
		else
		{
			final LoginPlayerData data = owner.getPlayerData(arg);
			if (data == null)
				throw new CrazyCommandNoSuchException("Player (with Account)", arg);
			owner.expirePassword(data);
			owner.sendLocaleMessage("COMMAND.PLAYER.EXPIREPASSWORD.SUCCESS", sender, data.getName());
		}
	}
}
