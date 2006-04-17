/*
 * Created on Apr 15, 2006
 */
package tod.experiments.bench;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import zz.utils.Utils;

public class PostgresCollector extends AbstractSQLCollector
{
	public PostgresCollector()
	{
		super(true, true);
		
	}

	@Override
	protected Connection connect() throws Exception
	{
		Process theProcess = Runtime.getRuntime().exec("/usr/bin/psql -p 5433 -d tod");
		Utils.pipe(new FileInputStream("doc/cc55a/db-postgres.sql"), theProcess.getOutputStream());
		theProcess.getOutputStream().close();
		Utils.pipe(theProcess.getInputStream(), System.err);
		Utils.pipe(theProcess.getErrorStream(), System.err);
		theProcess.waitFor();
		
		Class.forName("org.postgresql.Driver");
		return DriverManager.getConnection("jdbc:postgresql://localhost:5433/tod", "gpothier", "");
	}

	public long getStoredSize()
	{
		return InsertBench.getDirSize("/var/lib/postgresql/8.1/main/");
	}
}
