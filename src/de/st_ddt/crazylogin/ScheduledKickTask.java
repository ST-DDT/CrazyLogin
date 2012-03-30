package de.st_ddt.crazylogin;

import org.bukkit.entity.Player;
import de.st_ddt.crazyutil.locales.CrazyLocale;

public class ScheduledKickTask implements Runnable
{

	protected Player player;
	protected CrazyLocale locale;

	public ScheduledKickTask(Player player, CrazyLocale message)
	{
		super();
		this.player = player;
		this.locale = message;
	}

	@Override
	public void run()
	{
		if (!CrazyLogin.getPlugin().isLoggedIn(player))
			player.kickPlayer(locale.getLanguageText(player));
	}
}
