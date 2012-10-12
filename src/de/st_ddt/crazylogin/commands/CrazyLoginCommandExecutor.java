package de.st_ddt.crazylogin.commands;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.commands.CrazyPlayerDataCommandExecutor;

public abstract class CrazyLoginCommandExecutor extends CrazyPlayerDataCommandExecutor<LoginData, CrazyLogin>
{

	public CrazyLoginCommandExecutor(final CrazyLogin plugin)
	{
		super(plugin);
	}
}
