package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.paramitrisable.PlayerDataParamitrisable;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandPlayerReverify extends CommandExecutor
{

	public CommandPlayerReverify(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.PLAYER.REVERIFY.SUCCESS $Player$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		final String arg = ChatHelper.listingString(" ", args);
		if (arg.equals("*"))
		{
			for (final LoginPlayerData data : owner.getPlayerData())
				data.setLoggedIn(false);
			for (final Player player : Bukkit.getOnlinePlayers())
				owner.forceRelogin(player);
			owner.sendLocaleMessage("COMMAND.PLAYER.REVERIFY.SUCCESS", sender, arg);
		}
		else
		{
			final LoginPlayerData data = owner.getPlayerData(arg);
			if (data == null)
				throw new CrazyCommandNoSuchException("Player (with Account)", arg);
			owner.forceRelogin(data);
			owner.sendLocaleMessage("COMMAND.PLAYER.REVERIFY.SUCCESS", sender, data.getName());
		}
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
	@Permission("crazylogin.player.reverify")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return sender.hasPermission("crazylogin.player.reverify");
	}
}
