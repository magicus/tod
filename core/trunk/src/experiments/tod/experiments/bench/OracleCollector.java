/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
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
		Utils.pipe(theProcess.getInputStream(), System.err);
		Utils.pipe(theProcess.getErrorStream(), System.err);
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
