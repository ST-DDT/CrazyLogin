package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.locales.Localized;

public class CrazyLoginCommandPlayerPassword extends CrazyLoginCommandExecutor
{

	public CrazyLoginCommandPlayerPassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.PLAYER.PASSWORD.SUCCESS $Name$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!sender.hasPermission("crazylogin.player.password"))
			throw new CrazyCommandPermissionException();
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Player> <Passwort...>");
		final LoginPlayerData data = plugin.getPlayerData(args[0]);
		if (data == null)
			throw new CrazyCommandNoSuchException("Player (with Account)", args[0]);
		final String password = ChatHelper.listingString(ChatHelperExtended.shiftArray(args, 1));
		data.setPassword(password);
		plugin.sendLocaleMessage("COMMAND.PLAYER.PASSWORD.SUCCESS", sender, data.getName());
		plugin.getCrazyDatabase().save(data);
	}
}
