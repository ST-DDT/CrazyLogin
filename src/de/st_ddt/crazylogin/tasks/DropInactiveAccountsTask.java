package de.st_ddt.crazylogin.tasks;

import de.st_ddt.crazylogin.CrazyLogin;

public class DropInactiveAccountsTask implements Runnable
{

	private final CrazyLogin plugin;

	public DropInactiveAccountsTask(CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@Override
	public void run()
	{
		int amount = plugin.dropInactiveAccounts();
		if (amount > 0)
		{
			int autoDelete = plugin.getAutoDelete();
			plugin.broadcastLocaleMessage("ACCOUNTS.DELETED", "DropTask", autoDelete, amount);
		}
	}
}
