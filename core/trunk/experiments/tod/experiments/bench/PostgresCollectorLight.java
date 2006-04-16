/*
 * Created on Apr 15, 2006
 */
package tod.experiments.bench;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import zz.utils.Utils;

public class PostgresCollectorLight extends AbstractSQLCollector
{
	public PostgresCollectorLight()
	{
		super(false, false);
		
	}

	@Override
	protected Connection connect()
	{
		try
		{
			Process theProcess = Runtime.getRuntime().exec("/usr/bin/psql -p 5433 -d tod");
			Utils.pipe(new FileInputStream("doc/cc55a/db-postgres.sql"), theProcess.getOutputStream());
			theProcess.getOutputStream().close();
			Utils.pipe(theProcess.getInputStream(), System.out);
			Utils.pipe(theProcess.getErrorStream(), System.out);
			theProcess.waitFor();
			
			Class.forName("org.postgresql.Driver");
			return DriverManager.getConnection("jdbc:postgresql://localhost:5433/tod", "gpothier", "");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public long getStoredSize()
	{
		return 0;
	}
}
