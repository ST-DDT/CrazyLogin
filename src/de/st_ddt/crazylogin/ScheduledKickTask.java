package de.st_ddt.crazylogin;

import org.bukkit.entity.Player;

import de.st_ddt.crazyutil.locales.CrazyLocale;

public class ScheduledKickTask implements Runnable
{

	protected final Player player;
	protected final CrazyLocale locale;
	protected final boolean requireAccount;

	public ScheduledKickTask(final Player player, final CrazyLocale message)
	{
		super();
		this.player = player;
		this.locale = message;
		this.requireAccount = false;
	}

	public ScheduledKickTask(final Player player, final CrazyLocale message, final boolean requireAccount)
	{
		super();
		this.player = player;
		this.locale = message;
		this.requireAccount = requireAccount;
	}

	@Override
	public void run()
	{
		if (requireAccount)
			if (!CrazyLogin.getPlugin().hasAccount(player))
				player.kickPlayer(locale.getLanguageText(player));
		if (!CrazyLogin.getPlugin().isLoggedIn(player))
			player.kickPlayer(locale.getLanguageText(player));
	}
}
