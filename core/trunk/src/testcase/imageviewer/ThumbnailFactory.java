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
package imageviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.imageio.ImageIO;

public class ThumbnailFactory
{

	/**
	 * Creates a thumbnail of the specified file.
	 */
	public static ImageData create(File file)
	{
		String name = file.getName();

		if (name.endsWith(".jpg") || name.endsWith(".png"))
		{
			try
			{
				return new ImageThumbnail(file, ImageIO.read(file));
			}
			catch (IOException e)
			{
				return null;
			}
		}
		else if (name.endsWith(".txt"))
		{
			try
			{
				FileReader reader = new FileReader(file);
				String line = new BufferedReader(reader).readLine();
				return new TextThumbnail(file, line);
			}
			catch (IOException e)
			{
				return null;
			}
		}
		else return null;
	}

	private static class ImageThumbnail extends ImageData
	{
		private BufferedImage image;

		public ImageThumbnail(File aFile, BufferedImage image)
		{
			super(aFile);
			this.image = image;
		}

		public Dimension getSize()
		{
			int width = image.getWidth();
			int height = image.getHeight();
			return new Dimension(width, height);
		}

		public void paintThumbnail(Graphics g)
		{
			g.drawImage(image, 0, 0, null);
		}
	}

	private static class TextThumbnail extends ImageData
	{
		String text;

		public TextThumbnail(File aFile, String text)
		{
			super(aFile);
			this.text = text;
		}

		public Dimension getSize()
		{
			return new Dimension(50, 50);
		}

		@Override
		public void paintThumbnail(Graphics g)
		{
			g.setColor(Color.BLACK);
			g.drawString(text, 0, 20);
		}
	}
}
