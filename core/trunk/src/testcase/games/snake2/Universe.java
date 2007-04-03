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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class Universe extends JPanel implements Runnable
{
	private List<Entity> itsEntities = new ArrayList<Entity>();
	private List<Runnable> itsUpdates = new ArrayList<Runnable>();
	
	private int itsSnakeSize = 25;
	
	public Universe()
	{
		setPreferredSize(new Dimension(500, 500));
		new Thread(this).start();
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		grabFocus();
	}
	
	public int getSnakeSize()
	{
		return itsSnakeSize;
	}
	
	public void setupSnake()
	{
		SnakeHead theHead = new SnakeHead(this);
		theHead.setPos(new UPoint(100, 100));
		add(theHead);
		
		ISnakePart thePrevious = theHead;
		for (int i=1;i<=25;i++)
		{
			SnakeBodyRing theRing = new SnakeBodyRing(this, i);
			theRing.connect(thePrevious);
			thePrevious = theRing;
			theRing.setPos(new UPoint((float) (Math.random()*500), (float) (Math.random()*500)));
			add(theRing);
		}

	}
	
	/**
	 * Returns the number of bugs in the specified area.
	 */
	public float getEnnemyWeightAt(UPoint p, float r)
	{
		float w = 0;
		float rSq = r*r;
		for (Entity theEntity : itsEntities)
		{
			if (UPoint.distSq(p, theEntity.getPos()) < rSq) w += theEntity.ennemyWeight();
		}
		
		return w;
	}
	
	public void ringLost(final SnakeBodyRing aRing)
	{
		postUpdate(new Runnable()
		{
			public void run()
			{
				remove(aRing);
				itsSnakeSize--;
			}
		});
	}
	
	public synchronized void add(Entity e) 
	{
		itsEntities.add(e);
	}
	
	public synchronized void remove(Entity e) 
	{
		itsEntities.remove(e);
	}
	
	public void draw(Graphics2D g)
	{
		for (Entity theEntity : itsEntities) theEntity.draw(g);
	}
	
	public void postUpdate(Runnable aRunnable)
	{
		itsUpdates.add(aRunnable);
	}
	
	public synchronized void update()
	{
		for (Entity theEntity : itsEntities) theEntity.updatePosition();
		for (Runnable theUpdate : itsUpdates) theUpdate.run();
		itsUpdates.clear();
	}
	
	public UVector getForce(String aField, Entity e)
	{
		float dx = 0;
		float dy = 0;
		for (Entity theEntity : itsEntities)
		{
			if (theEntity == e) continue;
			UVector theForce = theEntity.getForceFor(aField, e);
			dx += theForce.dx;
			dy += theForce.dy;
		}
		
		return new UVector(dx, dy);
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		draw((Graphics2D) g);
	}
	
	public void run()
	{
		while(true)
		{
			update();
			repaint();
			
			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	

}
