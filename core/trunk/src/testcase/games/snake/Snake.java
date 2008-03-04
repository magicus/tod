/*
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
package games.snake;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import zz.utils.ui.HorizontalAlignment;
import zz.utils.ui.VerticalAlignment;
import zz.utils.ui.text.TextPainter;

public class Snake extends JPanel
implements Runnable
{
	private static final int CELL_SIZE = 15;
	
	/**
	 * List of the positions occupied by the snake.
	 */
	private LinkedList<Point> itsSnake = new LinkedList<Point>();
	
	/**
	 * Length of the snake.
	 */
	private int itsLength;
	
	private int itsBoardWidth;
	private int itsBoardHeight;
	
	private Direction itsDirection = Direction.UP;
	
	private Point itsBonus;
	
	private Random itsRandom = new Random();
	
	/**
	 * Delay between updates, in milliseconds.
	 */
	private int itsDelay;
	
	private boolean itsPlaying = false;

	public Snake(int aBoardWidth, int aBoardHeight)
	{
		itsBoardWidth = aBoardWidth;
		itsBoardHeight = aBoardHeight;
		
		setBackground(Color.BLUE);
		setPreferredSize(new Dimension(itsBoardWidth*CELL_SIZE, itsBoardHeight*CELL_SIZE));

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent aE)
			{
				switch(aE.getKeyCode())
				{
				case KeyEvent.VK_UP:
					if (itsDirection != Direction.DOWN) itsDirection = Direction.UP;
					break;
				case KeyEvent.VK_DOWN:
					if (itsDirection != Direction.UP) itsDirection = Direction.DOWN;
					break;
				case KeyEvent.VK_LEFT:
					if (itsDirection != Direction.RIGHT) itsDirection = Direction.LEFT;
					break;
				case KeyEvent.VK_RIGHT:
					if (itsDirection != Direction.LEFT) itsDirection = Direction.RIGHT;
					break;
				case KeyEvent.VK_SPACE:
					itsPlaying = true;
					System.out.println("New game!");
					break;
				case KeyEvent.VK_ESCAPE:
					gameOver();
					break;
				}
			}
		});
		newGame();
		Thread theThread = new Thread(this);
		theThread.start();
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		grabFocus();
	}
	
	private void createBonus()
	{
		itsBonus = new Point(
				itsRandom.nextInt(itsBoardWidth),
				itsRandom.nextInt(itsBoardHeight));
	}
	
	private void bonusGrabbed()
	{
		itsLength += 3;
		itsDelay -= itsDelay/5;
		createBonus();
	}
	
	private synchronized void newGame()
	{
		itsPlaying = false;
		itsDelay = 300;
		itsLength = 3;
		itsSnake.clear();
		itsSnake.add(new Point(itsBoardWidth/2, itsBoardHeight/4));
		createBonus();
	}
	
	private void gameOver()
	{
		newGame();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (itsPlaying)
		{
			g.setColor(Color.YELLOW);
			for(Point thePoint : itsSnake)
			{
				int theX = thePoint.x * CELL_SIZE;
				int theY = thePoint.y * CELL_SIZE;
				
				g.fillRect(theX, theY, CELL_SIZE, CELL_SIZE);
			}
			
			g.setColor(Color.RED);
			int theX = itsBonus.x * CELL_SIZE;
			int theY = itsBonus.y * CELL_SIZE;
			g.fillOval(theX, theY, CELL_SIZE, CELL_SIZE);
		}
		else
		{
			TextPainter.paint(
					(Graphics2D) g, 
					getFont(), 
					false, 
					Color.WHITE, 
					"Press space!", 
					getBounds(), 
					VerticalAlignment.CENTER, 
					HorizontalAlignment.CENTER);
		}
	}
	
	public synchronized void run()
	{
		while(true)
		{
			if (itsPlaying)
			{
				Point thePos = itsDirection.move(itsSnake.getFirst());
				
				if (thePos.x == itsBonus.x && thePos.y == itsBonus.y) bonusGrabbed();
				
				for(Point thePoint : itsSnake)
				{
					if (thePoint.x == thePos.x && thePoint.y == thePos.y)
					{
						gameOver();
						break;
					}
				}
				
				itsSnake.addFirst(thePos);
				while (itsSnake.size() > itsLength) itsSnake.removeLast();
				
				repaint();
			}
			
			try
			{
				Thread.sleep(itsDelay);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public static enum Direction
	{
		UP()
		{
			@Override
			public Point move(Point p)
			{
				return new Point(p.x, p.y-1);
			}
		}, 
		DOWN()
		{
			@Override
			public Point move(Point p)
			{
				return new Point(p.x, p.y+1);
			}
		}, 
		LEFT()
		{
			@Override
			public Point move(Point p)
			{
				return new Point(p.x-1, p.y);
			}
		}, 
		RIGHT()
		{
			@Override
			public Point move(Point p)
			{
				return new Point(p.x+1, p.y);
			}
		};
		
		public abstract Point move(Point p);
	}
	
	public static void main(String[] args)
	{
		JFrame theFrame = new JFrame("Snake!");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setContentPane(new Snake(50, 30));
		theFrame.pack();
		theFrame.setVisible(true);
	}
}
