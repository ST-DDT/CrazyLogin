package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandAlreadyExistsException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.locales.Localized;

public class CrazyLoginCommandPlayerCreate extends CrazyLoginCommandExecutor
{

	public CrazyLoginCommandPlayerCreate(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.PLAYER.CREATE.SUCCESS $Name$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!sender.hasPermission("crazylogin.player.create"))
			throw new CrazyCommandPermissionException();
		if (plugin.getCrazyDatabase() == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Name> <Password>");
		final String name = args[0];
		if (plugin.hasPlayerData(name))
			throw new CrazyCommandAlreadyExistsException("Account", name);
		final LoginPlayerData data = new LoginPlayerData(name);
		final String password = ChatHelper.listingString(ChatHelperExtended.shiftArray(args, 1));
		data.setPassword(password);
		plugin.sendLocaleMessage("COMMAND.PLAYER.CREATE.SUCCESS", sender, name);
		plugin.getCrazyDatabase().save(data);
	}
}
