package imageviewer;




import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ThumbnailGenerator {
	
	public static List<ImageData> generate (File base)
	{
		List<ImageData> theThumbnails = new ArrayList<ImageData>();
		String[] fileNames = base.list();
		Arrays.sort(fileNames);
		
		for (String fileName : fileNames) 
		{
			File file = new File(base, fileName);
			if (file.isDirectory()) continue;
			else theThumbnails.add(ThumbnailFactory.create(file));
		}
		
		return theThumbnails;
	}
}
