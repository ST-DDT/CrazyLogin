package de.st_ddt.crazylogin.databases;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.PlayerDataDatabase;

public interface CrazyLoginDataDatabase extends PlayerDataDatabase<LoginPlayerData>
{

	public void saveWithPassword(LoginPlayerData entry);
}
