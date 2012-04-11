package de.st_ddt.crazylogin.databases;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.Column;
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabase;

public class CrazyLoginMySQLDatabase extends MySQLDatabase<LoginPlayerData>
{

	public CrazyLoginMySQLDatabase(MySQLConnection connection, String table)
	{
		super(LoginPlayerData.class, connection, table, getColumns(), getPrimaryColumn());
		checkTable();
	}

	private static Column[] getColumns()
	{
		Column[] columns = new Column[3];
		columns[0] = getPrimaryColumn();
		columns[1] = new Column("Password", "CHAR(255)", null, false, false);
		columns[2] = new Column("IPs", "CHAR(255)", null, false, false);
		return columns;
	}

	private static Column getPrimaryColumn()
	{
		return new Column("Name", "CHAR(50)", true, false);
	}
}
