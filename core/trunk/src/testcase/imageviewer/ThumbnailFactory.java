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
