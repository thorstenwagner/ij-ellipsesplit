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


import ij.gui.EllipseRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiRotator;

import java.awt.Polygon;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.doube.geometry.FitEllipse;


/**
 * 
 * @author Undral Erdenetsogt
 *
 */
public class Ellipse {
	/*
	 * aus org.doube.geometry.FitEllipse:
	 * Output: A = [a b c d e f]' is the vector of algebraic parameters of the
	 * fitting ellipse: ax² + bxy + cy² +dx + ey + f = 0 the vector A is normed,
	 * so that ||A||=1
	 */
	
	// a bis f sind die 6 algebraischen Parameter
	private double a;	
	private double b;	
	private double c;	
	private double d;	
	private double e;	
	private double f;
	private double[] parameterVector;
	
	// Geometrische Parameter:
	private double x;
	private double y;
	private double shortAxis;
	private double longAxis;
	private double phi;
	private double aspectRatio;
	
	// Polygon data which was used for fitting
	private ArrayList<Integer> xpoints;
	private ArrayList<Integer> ypoints;
	
	/**
	 * Mean algebraic distance of segment to ellipse
	 */
	private double dis;
	
	private Calibration cal;
	
	private int label = 0;
	private static int labelCounter = 0;
	
	public Ellipse() {
		label = labelCounter++;
	}
	
	public Ellipse(double[] parameterVector) {
		label = labelCounter++;
		cal = EllipseSplit_.getInstance().getInputImage().getCalibration();
		xpoints = new ArrayList<Integer>();
		ypoints = new ArrayList<Integer>();
		this.parameterVector = parameterVector;
		this.a = parameterVector[0];
		this.b = parameterVector[1];
		this.c = parameterVector[2];
		this.d = parameterVector[3];
		this.e = parameterVector[4];
		this.f = parameterVector[5];
		
		double[] geometricParams = FitEllipse.varToDimensions(parameterVector);
		this.x = geometricParams[0];
		this.y = geometricParams[1];
		this.shortAxis = geometricParams[2]<=geometricParams[3] ? geometricParams[2] : geometricParams[3];
		this.longAxis = geometricParams[2]>=geometricParams[3] ? geometricParams[2] : geometricParams[3];
		this.phi = geometricParams[4];
		this.aspectRatio = shortAxis / longAxis;
	}

	public Ellipse(double a, double b, double c, double d, double e, double f) {
		this(new double[]{a, b, c, d, e, f});
	}
	
	public Roi getRoi() {
		double x1 = x-longAxis;
		double y1 = y;
		double x2 = x+longAxis;
		double y2 = y;
		
		EllipseRoi unrotatedEllipse = new EllipseRoi(x1, y1, x2, y2, aspectRatio);
		//return unrotatedEllipse;		

		double rotationAngle = getRotationAngle();
		
		return RoiRotator.rotate(unrotatedEllipse, rotationAngle);
	}
	
	public Polygon getPolygon() {
		return getRoi().getPolygon();
	}
	
	public void setXCoordinates(ArrayList<Integer> xpoints){
		this.xpoints = xpoints;
	}
	
	public void setYCoordinates(ArrayList<Integer> ypoints){
		this.ypoints = ypoints;
	}
	
	public ArrayList<Integer> getXCoordinates(){
		return xpoints;
	}
	
	public ArrayList<Integer> getYCoordinates(){
		return ypoints;
	}
	
	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public double getC() {
		return c;
	}

	public double getD() {
		return d;
	}

	public double getE() {
		return e;
	}

	public double getF() {
		return f;
	}

	public double[] getParameterVector() {
		return parameterVector;
	}
	
	public double getPhi() {
		return phi;
	}
	
	public double getRotationAngle(){
		double rotationAngle = phi*180/Math.PI;
		if (f < 0) {
			rotationAngle = rotationAngle + 90;
		}
		return rotationAngle;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getLengthShortAxis() {
		return shortAxis*cal.pixelHeight;
	}

	public double getLengthLongAxis() {
		return longAxis*cal.pixelHeight;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public double getDis() {
		return dis;
	}

	public void setDis(double dis) {
		this.dis = dis;
	}
	
	public int getLabel(){
		return label;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat( "0.00000" );
		return "Ellipse [" + 
					"phi(deg)= " + df.format(phi*180/Math.PI) + 
					"phi= " + df.format(phi) +
					", a= " + df.format(a) +
					", b= " + df.format(b) +
					", c= " + df.format(c) +
					", d= " + df.format(d) +
					", e= " + df.format(e) +
					", f= " + df.format(f) +
					", x= " + df.format(x) +
					", y= " + df.format(y) +
					", minA= " + df.format(shortAxis) +
					", maxA= " + df.format(longAxis) +
					", AR= " + df.format(aspectRatio) +
				"]";
	}	
	
	@Override
	public boolean equals(Object obj) {
	
		return getLabel() == ((Ellipse)obj).getLabel();
	}

}
