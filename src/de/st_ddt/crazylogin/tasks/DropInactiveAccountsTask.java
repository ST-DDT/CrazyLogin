package de.st_ddt.crazylogin.tasks;

import de.st_ddt.crazylogin.CrazyLogin;

public class DropInactiveAccountsTask implements Runnable
{

	private final CrazyLogin plugin;

	public DropInactiveAccountsTask(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@Override
	public void run()
	{
		final int amount = plugin.dropInactiveAccounts();
		if (amount > 0)
		{
			final int autoDelete = plugin.getAutoDelete();
			plugin.broadcastLocaleMessage("ACCOUNTS.DELETED", "DropTask", autoDelete, amount);
		}
	}
}
