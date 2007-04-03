package imageviewer;



import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;

public abstract class ImageData {
	private File itsFile;
	
	public ImageData(File aFile)
	{
		itsFile = aFile;
	}
	
	public String getName()
	{
		return itsFile.getName();
	}
	
	public abstract void paintThumbnail (Graphics g);
	public abstract Dimension getSize();
}
