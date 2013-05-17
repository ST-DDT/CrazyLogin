package de.st_ddt.crazylogin.commands;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.commands.CrazyPlayerDataPluginCommandExecutor;

public abstract class CommandExecutor extends CrazyPlayerDataPluginCommandExecutor<LoginData, CrazyLogin> implements CommandExecutorInterface<CrazyLogin>
{

	public CommandExecutor(final CrazyLogin plugin)
	{
		super(plugin);
	}
}
