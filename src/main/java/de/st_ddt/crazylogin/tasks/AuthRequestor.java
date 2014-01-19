package de.st_ddt.crazylogin.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;

public class AuthRequestor implements Runnable
{

	private final CrazyLogin plugin;
	private final Player player;
	private final String path;
	private int taskID = -2;

	public AuthRequestor(final CrazyLogin plugin, final Player player, final String path)
	{
		super();
		this.plugin = plugin;
		this.player = player;
		this.path = path;
	}

	@Override
	public void run()
	{
		if (plugin.isLoggedIn(player) || !player.isOnline())
			cancel();
		else
			plugin.sendLocaleMessage(path, player);
	}

	public void start(final long delay)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, delay + 1);
	}

	public void start(final long delay, final long interval)
	{
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay + 1, interval);
	}

	public void cancel()
	{
		if (taskID >= 0)
		{
			Bukkit.getScheduler().cancelTask(taskID);
			taskID = -1;
		}
	}

	public void setTaskID(final int taskID)
	{
		if (taskID == -2)
			this.taskID = taskID;
	}
}
