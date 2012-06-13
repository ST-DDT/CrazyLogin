package de.st_ddt.crazylogin.databases;

import java.io.File;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.FlatDatabase;

public class CrazyLoginFlatDatabase extends FlatDatabase<LoginPlayerData>
{

	public CrazyLoginFlatDatabase(final File file, final String colName, final String colPassword, final String colIPs, final String colLastAction)
	{
		super(LoginPlayerData.class, file, new String[] { colName, colPassword, colIPs, colLastAction });
	}
}
