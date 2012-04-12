package de.st_ddt.crazylogin.databases;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.Column;
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabase;

public class CrazyLoginMySQLDatabase extends MySQLDatabase<LoginPlayerData>
{

	public CrazyLoginMySQLDatabase(MySQLConnection connection, String table, String colName, String colPassword, String colIPs)
	{
		super(LoginPlayerData.class, connection, table, getColumns(colName, colPassword, colIPs), getPrimaryColumn(colName));
		checkTable();
	}

	private static Column[] getColumns(String colName, String colPassword, String colIPs)
	{
		Column[] columns = new Column[3];
		columns[0] = getPrimaryColumn(colName);
		columns[1] = new Column(colPassword, "CHAR(255)", null, false, false);
		columns[2] = new Column(colIPs, "CHAR(255)", null, false, false);
		return columns;
	}

	private static Column getPrimaryColumn(String colName)
	{
		return new Column(colName, "CHAR(50)", true, false);
	}
}
