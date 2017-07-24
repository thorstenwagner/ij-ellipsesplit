/*
 * The MIT License (MIT)
 * Copyright (c) 2015 Thorsten Wagner (wagner@biomedical-imaging.de)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.biomedical_imaging.ij.ellipsesplit;

import ij.IJ;
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
		
		plugin.setup("", ip);
		plugin.run(ip.getProcessor());
	}
}
