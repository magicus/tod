/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
*/
package tod.tools.parsers;

public interface WorkingSetParserConstants
{

    int EOF = 0;

    int PLUS = 1;

    int MINUS = 2;

    int LBRACKET = 3;

    int RBRACKET = 4;

    int DOT = 5;

    int STAR = 6;

    int COLON = 7;

    int CLASSNAME = 8;

    int ID = 9;

    int IDCHAR = 10;

    int DEFAULT = 0;

    String[] tokenImage =
        { "<EOF>", "\"+\"", "\"-\"", "\"[\"", "\"]\"", "\".\"", "\"*\"", "\":\"", "<CLASSNAME>", "<ID>", "<IDCHAR>",
         "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", };

}
