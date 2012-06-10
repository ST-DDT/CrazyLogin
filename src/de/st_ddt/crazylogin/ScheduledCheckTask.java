package de.st_ddt.crazylogin;

public class ScheduledCheckTask implements Runnable
{

	protected final CrazyLogin plugin;

	public ScheduledCheckTask(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	public CrazyLogin getPlugin()
	{
		return plugin;
	}

	@Override
	public void run()
	{
		plugin.checkTimeOuts();
	}
}
