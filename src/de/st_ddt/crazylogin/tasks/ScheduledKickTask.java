package de.st_ddt.crazylogin.tasks;

import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyutil.locales.CrazyLocale;

public class ScheduledKickTask implements Runnable
{

	protected final Player player;
	protected final CrazyLocale locale;
	protected final boolean requireAccount;
	protected final long tempBan;

	public ScheduledKickTask(final Player player, final CrazyLocale message)
	{
		super();
		this.player = player;
		this.locale = message;
		this.requireAccount = false;
		this.tempBan = 0;
	}

	public ScheduledKickTask(final Player player, final CrazyLocale message, final boolean requireAccount)
	{
		super();
		this.player = player;
		this.locale = message;
		this.requireAccount = requireAccount;
		this.tempBan = 0;
	}

	public ScheduledKickTask(final Player player, final CrazyLocale message, final long tempBan)
	{
		super();
		this.player = player;
		this.locale = message;
		this.requireAccount = false;
		this.tempBan = tempBan;
	}

	@Override
	public void run()
	{
		if (!player.isOnline())
			return;
		if (requireAccount)
			if (!CrazyLogin.getPlugin().hasPlayerData(player))
				player.kickPlayer(locale.getLanguageText(player));
		if (!CrazyLogin.getPlugin().isLoggedIn(player))
			player.kickPlayer(locale.getLanguageText(player));
		if (tempBan > 0)
			CrazyLogin.getPlugin().setTempBanned(player, tempBan);
	}
}
