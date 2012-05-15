package de.st_ddt.crazylogin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.crypt.Encryptor;

public interface LoginPlugin
{

	public boolean isLoggedIn(final Player player);

	public boolean hasAccount(final OfflinePlayer player);

	public Encryptor getEncryptor();

	public LoginPlayerData getPlayerData(OfflinePlayer player);
}
