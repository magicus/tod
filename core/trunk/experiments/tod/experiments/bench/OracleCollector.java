/*
 * Created on Apr 16, 2006
 */
package tod.experiments.bench;

import java.io.FileInputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;
import zz.utils.Utils;

public class OracleCollector extends AbstractSQLCollector
{

	public OracleCollector()
	{
		super(true, true);
	}

	@Override
	protected Connection connect() throws Exception
	{
		String oracleHome = "/usr/lib/oracle/xe/app/oracle/product/10.2.0/server";
		Process theProcess = Runtime.getRuntime().exec(
				oracleHome+"/bin/sqlplus tod@localhost:1521/tod",
				new String[] {"ORACLE_HOME="+oracleHome});

		Utils.pipe(new FileInputStream("doc/cc55a/db-oracle.sql"), theProcess.getOutputStream());
		theProcess.getOutputStream().close();
		Utils.pipe(theProcess.getInputStream(), System.out);
		Utils.pipe(theProcess.getErrorStream(), System.out);
		theProcess.waitFor();

		
		OracleDataSource ds = new OracleDataSource();
	    ds.setURL("jdbc:oracle:thin:tod/tod@//localhost:1521/XE");
	    return ds.getConnection();
	}

	@Override
	public long getStoredSize()
	{
		return 0;
	}
}
