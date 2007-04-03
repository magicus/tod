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
package games.snake2;

import games.snake.Snake;

import javax.swing.JFrame;

public class Play
{
	private static Universe createUniverse()
	{
		Universe theUniverse = new Universe();
		theUniverse.setupSnake();
		
		for (int i=0;i<50;i++)
		{
			Bug theBug = new Bug(theUniverse);
			theBug.setPos(new UPoint((float) (Math.random()*500), (float) (Math.random()*500)));
			theUniverse.add(theBug);
		}
		
		return theUniverse;
	}
	
	public static void main(String[] args)
	{
		JFrame theFrame = new JFrame("Snake 2!");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setContentPane(createUniverse());
		theFrame.pack();
		theFrame.setVisible(true);
	}
}
