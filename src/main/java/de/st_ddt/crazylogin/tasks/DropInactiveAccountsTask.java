package de.st_ddt.crazylogin.tasks;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class DropInactiveAccountsTask implements Runnable
{

	private final CrazyLogin plugin;

	public DropInactiveAccountsTask(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@Override
	@Permission("crazylogin.warndelete")
	@Localized("CRAZYLOGIN.COMMAND.DROPOLDDATA.DELETED $DropCauser$ $KeptDays$ $DroppedAmount$")
	public void run()
	{
		final int amount = plugin.dropInactiveAccounts();
		if (amount > 0)
			plugin.broadcastLocaleMessage(true, "crazylogin.warndelete", "COMMAND.DROPOLDDATA.DELETED", "DropTask", plugin.getAutoDelete(), amount);
	}
}
