package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.paramitrisable.PlayerDataParamitrisable;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandPlayerCheckPassword extends CommandExecutor
{

	public CommandPlayerCheckPassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized({ "CRAZYLOGIN.COMMAND.PLAYER.CHECKPASSWORD.SUCCESS $Player$", "CRAZYLOGIN.COMMAND.PLAYER.CHECKPASSWORD.FAILED $Player$" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (owner.getCrazyDatabase() == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Player> <Passwort>");
		final String name = args[0];
		final LoginPlayerData data = owner.getPlayerData(name);
		if (data == null)
			throw new CrazyCommandNoSuchException("Account", name);
		final String password = ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(args, 1));
		if (data.isPassword(password))
			owner.sendLocaleMessage("COMMAND.PLAYER.CHECKPASSWORD.SUCCESS", sender, data.getName());
		else
			owner.sendLocaleMessage("COMMAND.PLAYER.CHECKPASSWORD.FAILED", sender, data.getName());
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length != 1)
			return null;
		else
			return PlayerDataParamitrisable.tabHelp(owner, args[0]);
	}

	@Override
	@Permission("crazylogin.player.checkpassword")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return sender.hasPermission("crazylogin.player.checkpassword");
	}
}
