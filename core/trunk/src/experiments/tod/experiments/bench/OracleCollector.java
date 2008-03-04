/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
