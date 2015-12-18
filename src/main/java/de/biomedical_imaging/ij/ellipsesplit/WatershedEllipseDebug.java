package de.biomedical_imaging.ij.ellipsesplit;

import ij.ImageJ;
import ij.ImagePlus;

import java.net.URL;

public class WatershedEllipseDebug {
	/**
	 * main method for debugging
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );
		EllipseSplit_ plugin = new EllipseSplit_();
		URL url = plugin.getClass().getClassLoader().getResource("Ellipse_Test_Stack2.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		ip.show();
		
		//plugin.setup("", IJ.getImage());
		//plugin.run(IJ.getImage().getProcessor());
	}
}
