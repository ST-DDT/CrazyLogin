package de.st_ddt.crazylogin.commands;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.commands.CrazyPlayerDataCommandExecutor;

public abstract class CommandExecutor extends CrazyPlayerDataCommandExecutor<LoginData, CrazyLogin>
{

	public CommandExecutor(final CrazyLogin plugin)
	{
		super(plugin);
	}
}
