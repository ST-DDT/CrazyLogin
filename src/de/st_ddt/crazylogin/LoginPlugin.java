package de.st_ddt.crazylogin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface LoginPlugin
{

	public boolean isLoggedIn(final Player player);

	public boolean hasAccount(final OfflinePlayer player);
}
