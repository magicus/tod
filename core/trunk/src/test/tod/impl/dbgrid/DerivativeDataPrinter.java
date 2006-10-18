/*
 * Created on Oct 18, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DerivativeDataPrinter
{
	private PrintWriter itsWriter;
	
	private double itsFirstX;
	private double itsLastX;
	private double itsLastY;
	
	private int i;
	
	public DerivativeDataPrinter(
			PrintWriter aWriter,
			String aXLabel,
			String aYLabel)
	{
		itsWriter = aWriter;
		
		itsWriter.println("# Col. 1: "+aXLabel);
		itsWriter.println("# Col. 2: "+aYLabel);
		itsWriter.println("# Col. 3: dy/dx");
		itsWriter.println("# Col. 4: avg");
	}
	
	public DerivativeDataPrinter(
			File aFile,
			String aXLabel,
			String aYLabel) throws IOException
	{
		this(new PrintWriter(new FileWriter(aFile)), aXLabel, aYLabel);
	}
	
	public void addPoint(double aX, double aY)
	{
		if (itsFirstX == 0) itsFirstX = aX;
		
		double theDX = aX-itsLastX;
		double theDY = aY-itsLastY;
		
		double theD = theDY/theDX;
		double theAvg = aY / (aX-itsFirstX);
		
		itsWriter.println(aX+" "+aY+" "+theD+" "+theAvg);

		if (i % 10 == 0) itsWriter.flush();
		i++;
		
		itsLastX = aX;
		itsLastY = aY;
	}
	
	public void close()
	{
		itsWriter.close();
	}
}
