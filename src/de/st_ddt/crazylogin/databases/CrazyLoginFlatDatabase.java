package de.st_ddt.crazylogin.databases;

import java.io.File;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.FlatDatabase;

public class CrazyLoginFlatDatabase extends FlatDatabase<LoginPlayerData>
{

	public CrazyLoginFlatDatabase(File file, String colName, String colPassword, String colIPs)
	{
		super(LoginPlayerData.class, file, new String[] { colName, colPassword, colIPs });
	}
}
