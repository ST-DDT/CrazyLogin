package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.commands.CrazyCommandListEditor;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ListFormat;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandMainCommands extends CrazyCommandListEditor<CrazyLogin, String>
{

	private final CrazyLogin owner;
	private final ListFormat format = new ListFormat()
	{

		@Override
		@Localized("CRAZYLOGIN.COMMAND.COMMANDS.LISTHEAD $CurrentPage$ $MaxPage$ $ChatHeader$ $DateTime$")
		public String headFormat(final CommandSender sender)
		{
			return owner.getLocale().getLanguageEntry("COMMAND.COMMANDS.LISTHEAD").getLanguageText(sender);
		}

		@Override
		public String listFormat(final CommandSender sender)
		{
			return "$1$\n";
		}

		@Override
		public String entryFormat(final CommandSender sender)
		{
			return "$0$";
		}
	};

	public CommandMainCommands(final CrazyLogin plugin)
	{
		super(plugin, true, false, true);
		this.owner = plugin;
	}

	@Override
	@Permission("crazylogin.commands")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		if (sender instanceof Player)
			if (!owner.isLoggedIn((Player) sender))
				return false;
		return sender.hasPermission("crazylogin.commands");
	}

	@Override
	public List<String> getCollection()
	{
		return owner.getCommandWhiteList();
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.COMMANDS.ADDED $Element$")
	public String addLocale()
	{
		return "CRAZYLOGIN.COMMAND.COMMANDS.ADDED";
	}

	@Override
	public String addViaIndexLocale()
	{
		return null;
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.COMMANDS.REMOVED $Element$")
	public String removeLocale()
	{
		return "CRAZYLOGIN.COMMAND.COMMANDS.REMOVED";
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.COMMANDS.REMOVED $Element$")
	public String removeViaIndexLocale()
	{
		return "COMMAND.COMMANDS.REMOVED";
	}

	@Override
	public ListFormat listFormat()
	{
		return format;
	}

	@Override
	public String getEntry(final CommandSender sender, final String... args) throws CrazyException
	{
		return ChatHelper.listingString(" ", args);
	}

	@Override
	protected void saveChanges()
	{
		owner.saveConfiguration();
	}
}
