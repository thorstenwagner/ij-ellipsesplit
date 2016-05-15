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

import java.awt.AWTEvent;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;

import org.doube.geometry.FitEllipse;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import com.seisw.util.geom.*;



public class EllipseSplit_ implements ExtendedPlugInFilter, DialogListener {
	
	private static int FLAGS =  DOES_8G  | FINAL_PROCESSING; 
	 
	private boolean addToManager;
	private boolean addToResultsTable;
	private boolean merge;
	private double overlappingThreshold;
	private boolean useSplitImage;
	
	private ArrayList<ManyEllipses> allEllipses;
	private ImagePlus splittedImage;
	private ImagePlus imp;
	private ResultsTable results;
	private ResultsTableSelectionDrawer rtsd;
	private ImageResultsTableSelector irts;
	private static EllipseSplit_ instance = null;
	
	//Geometric Bounds for filtering
	private double[] majorAxisBounds;
	private double[] minorAxisBounds;
	private double[] aspectRatioBounds;
	
	public EllipseSplit_() {
		instance = this;
		addToManager = false;
		addToResultsTable = false;
		merge = false;
		overlappingThreshold = 1;
		useSplitImage = false;
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		if(arg=="final"){
			if(addToResultsTable){
				results.show("Results");
				rtsd = new ResultsTableSelectionDrawer(imp);
				IJ.getTextPanel().addMouseListener(rtsd);
				irts = new ImageResultsTableSelector(imp);
				if(imp.getWindow()!=null){
					imp.getWindow().getComponent(0).addMouseListener(irts);
				}
				
				return DONE;
			}
		}
	
		return FLAGS | DOES_STACKS;
	}

	@Override
	public void run(ImageProcessor ip) {
		// Split ellipses
		ManyEllipses ellipses = splitAndFitEllipse(ip, addToManager, addToResultsTable, merge, overlappingThreshold);
	
		// Output
		RoiManager  rm = RoiManager.getInstance();
		if(rm==null && addToManager){
			rm = new RoiManager();
			rm.runCommand("Show all");
		}
		results = ResultsTable.getResultsTable();
		if(results==null){
			results =  new ResultsTable();
			
		}
		for (Ellipse e : ellipses) {
			if(addToManager){
				Roi r = e.getRoi();
				r.setPosition(ip.getSliceNumber());
				r.setName(""+e.getLabel());
				rm.addRoi(r);
			}
			
			if(addToResultsTable){
				results.incrementCounter();
				results.addValue("Frame",ip.getSliceNumber());
				results.addValue("Label",e.getLabel());
				results.addValue("X", e.getX());
				results.addValue("Y", e.getY());
				results.addValue("Length long axis", e.getLengthLongAxis()*2);
				results.addValue("Length short axis", e.getLengthShortAxis()*2);
				results.addValue("Aspect ratio", 1.0/e.getAspectRatio());
				results.addValue("Rotation angle", e.getRotationAngle());
				//results.addValue("R", e.getRValue());
				//IJ.log("Distance: " + e.shortestDistanceToPoint(0, 0));
				
			}
		}
		
		allEllipses.add(ellipses);
		
	}
	
	/**
	 * Diese Methode trennt binäre Objekt mit einer einfachen Wasserscheidentransforamtion. Anschließend fittet sie
	 * zu allen Konturen eine Ellipse. Dabei lässt sie Konturen aus, die durch die Wasserscheidentransformation entstanden sind.
	 * This method splits binary objects first by a 
	 * @param ip Binary image
	 * @param addToManager True when the ellipses should be added to the ROI Manager
	 * @param addToResultsTable True when the ellipses should be added to the Results Table
	 * @param merge	True when 
	 * @param overlappingThreshold When the parameter merge is true, and the relative overlapping between to ellipses is larger
	 * then this value, then the ellipses will be merged. The value has to be between 0 and 1. 
	 * @return All fitted ellipses
	 */
	public ManyEllipses splitAndFitEllipse(ImageProcessor ip, boolean addToManager,boolean addToResultsTable, boolean merge, double overlappingThreshold){

		ImagePlus origImp = new ImagePlus("",ip.duplicate());
		ImageProcessor ipForBlobDetection;
		if(useSplitImage==false){
			splittedImage = new ImagePlus("", ip.duplicate());
			EDM watershedEDM = new EDM();
			watershedEDM.toWatershed(splittedImage.getProcessor());
			ipForBlobDetection = splittedImage.getProcessor();
		}else{
			ipForBlobDetection = splittedImage.getImageStack().getProcessor(ip.getSliceNumber());
		}
		
		
		ManyBlobs mb = new ManyBlobs(new ImagePlus("",ipForBlobDetection));
		mb.setBackground(0);
		mb.findConnectedComponents();
		
		ArrayList<Blob> blobsOnEdges = new ArrayList<Blob>();
		for (Blob blob : mb) {
			if(blob.isOnEdge(ipForBlobDetection)){
				blobsOnEdges.add(blob);
			}
		}
		mb.removeAll(blobsOnEdges);
		
		ImageCalculator calculateImages = new ImageCalculator();
		ImagePlus extractedSeparatorsImp = calculateImages.run("XOR create",new ImagePlus("",ipForBlobDetection) , origImp);	
		
		ManyEllipses ellipses = new ManyEllipses();
	
		//Calculate Ellipses
		for (Blob blob : mb) {
			
			Polygon contour = blob.getOuterContour();
			ArrayList<Integer> xpoints = new ArrayList<Integer>();
			ArrayList<Integer> ypoints = new ArrayList<Integer>();
			fillSeperatorFreeXYCoordinates(contour, xpoints, ypoints, extractedSeparatorsImp.getProcessor());
			
			if(xpoints.size()>3){
				
				Ellipse ellipse = fitEllipse(xpoints, ypoints);
				if(ellipse != null){
					System.out.println("done");
					ellipses.add(ellipse);
				}
			}
		}

		//Remove bad ellipse fits
		for (int i = 0; i < ellipses.size(); i++) {
			Ellipse e = ellipses.get(i);

			if(Double.isNaN(e.getLengthLongAxis()) ||
					Double.isNaN(e.getLengthShortAxis()) ||
					Double.isNaN(e.getAspectRatio()) ||
					Double.isNaN(e.getRotationAngle()) ||
					1.0/e.getAspectRatio() > 100){
				ellipses.remove(i);
				i--;
			}
		}
		
		if(merge){
		 merge(ellipses,overlappingThreshold);
		}
		
		//Apply geometric filters
		
		for (int i = 0; i < ellipses.size(); i++) {
			Ellipse e = ellipses.get(i);
			
			if(!checkBounds(imp.getCalibration().getRawX(e.getLengthLongAxis()*2), majorAxisBounds) ||
					!checkBounds(imp.getCalibration().getRawX(e.getLengthShortAxis()*2),minorAxisBounds)||
					!checkBounds(1.0/e.getAspectRatio(),aspectRatioBounds)){
				ellipses.remove(i);
				i--;
			}
		}
		
		return ellipses;
	}
	
	private boolean checkBounds(double value, double[] bounds){
		return value >= bounds[0] && value <= bounds[1];
	}
	
	public ArrayList<ManyEllipses> getAllEllipses(){
		return allEllipses;
	}
	
	public ResultsTableSelectionDrawer getResultsTableSelectionDrawer(){
		return rtsd;
	}
	
	public ImageResultsTableSelector getImageResultsTableSelector(){
		return irts;
	}
	
	private Ellipse fitEllipse(ArrayList<Integer> xpoints,ArrayList<Integer> ypoints){
		
		try {
			double[] algebraicEllipseParams = FitEllipse.direct(arraylistToPointArray(xpoints,ypoints));
			Ellipse ellipse = new Ellipse(algebraicEllipseParams);
			ellipse.setXCoordinates(xpoints);
			ellipse.setYCoordinates(ypoints);
			return ellipse;
			
		}
		catch(RuntimeException e){
			return null;
		}
	}
	private void merge(ArrayList<Ellipse> ellipses, double overlappingThreshold){
		for(int i = 0; i < ellipses.size(); i++){
			ArrayList<Integer> mergeEllipses = new ArrayList<Integer>();
			Poly p1 = polygonToPoly(((PolygonRoi)ellipses.get(i).getRoi()).getPolygon());
			double p1Area = p1.getArea();
			for(int j = i+1; j < ellipses.size(); j++){
				if( Math.sqrt(Math.pow(ellipses.get(j).getX()-ellipses.get(i).getX(),2)+
						Math.pow(ellipses.get(j).getY()-ellipses.get(i).getY(),2)) < (ellipses.get(j).getLengthLongAxis()+ellipses.get(i).getLengthLongAxis())/2.0){
					Poly p2 = polygonToPoly(((PolygonRoi)ellipses.get(j).getRoi()).getPolygon());
					double p2Area = p2.getArea();
					
					double interArea = Clip.intersection(p1, p2).getArea();
					if(Math.max(interArea/p2Area,interArea/p1Area)>overlappingThreshold){
						mergeEllipses.add(j);
					}
				}
			}
			
			if(mergeEllipses.size()>0){
				ArrayList<Integer> newXPoints = ellipses.get(i).getXCoordinates();
				ArrayList<Integer> newYPoints = ellipses.get(i).getYCoordinates();
				for(int j = 0; j < mergeEllipses.size(); j++){
					newXPoints.addAll(ellipses.get(mergeEllipses.get(j)).getXCoordinates());
					newYPoints.addAll(ellipses.get(mergeEllipses.get(j)).getYCoordinates());
				}
				ellipses.set(i, fitEllipse(newXPoints, newYPoints));
				Collections.sort(mergeEllipses,Collections.reverseOrder());
				for(int j = 0; j < mergeEllipses.size(); j++){
					ellipses.remove(mergeEllipses.get(j).intValue());
				}
				i = 0;
			}
		}
	}
	
	private Poly polygonToPoly(Polygon p){
		Poly p2 = new PolyDefault();
		for(int i = 0; i < p.npoints; i++){
			p2.add(p.xpoints[i],p.ypoints[i]);
		}
		return p2;
	}
	
	private void fillSeperatorFreeXYCoordinates(Polygon contour, ArrayList<Integer> xpoints, ArrayList<Integer> ypoints, ImageProcessor seperators){
		for(int i = 0; i < contour.npoints; i++){
			int x = contour.xpoints[i];
			int y = contour.ypoints[i];
			if(!hasWatershedInNeighborhood(seperators, x, y) &&
					!isAtImageBoundary(seperators, x, y)){
				xpoints.add(x);
				ypoints.add(y);
			}
		}
	}
	
	private double[][] arraylistToPointArray(ArrayList<Integer> xpoints,ArrayList<Integer> ypoints ){
		double[][] points = new double[xpoints.size()][2];
		for(int i = 0; i < xpoints.size(); i++){
			points[i][0] = xpoints.get(i);
			points[i][1] = ypoints.get(i);
		}
		return points;
	}
	private boolean isAtImageBoundary(ImageProcessor ip, int x, int y){
		return (x==0 || y==0 || x == ip.getWidth() ||
				y == ip.getHeight());
		
	}
	private boolean hasWatershedInNeighborhood(ImageProcessor ip, int x, int y){
		
		if(checkPixelValue(ip,x, y,255) || 
				checkPixelValue(ip,x+1, y,255) ||
				checkPixelValue(ip,x-1, y,255) ||
				checkPixelValue(ip,x, y+1,255) ||
				checkPixelValue(ip,x, y-1,255) ||
				checkPixelValue(ip,x+1, y+1,255) ||
				checkPixelValue(ip,x-1, y+1,255) ||
				checkPixelValue(ip,x-1, y-1,255)){
			return true;
		}
		return false;
	}
	
	
	
	private boolean checkPixelValue(ImageProcessor ip, int x, int y, int value){
		if(x<0 || x >= ip.getWidth() || y < 0 || y >= ip.getHeight()){
			return false;
		}
		else if(ip.get(x,y)==value){
			return true;
		}
		return false;
		
		
	}
	
	public ImagePlus getInputImage(){
		return imp;
	}
	
	public Ellipse getEllipseByFrameAndLabel(int frame, int label){
		return allEllipses.get(frame).getEllipseByLabel(label);
	}
	
	public static EllipseSplit_ getInstance(){
		return instance;
	}

	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		splittedImage=null;
		this.imp = imp;
		GenericDialog gd = new GenericDialog("Ellipse Split");
		int windowNumber = WindowManager.getImageTitles().length;
		String[] splittedImageChoice = new String[windowNumber+1];
		splittedImageChoice[0] = "Use standard watershed";
		for(int i = 1; i < windowNumber+1; i++){
			splittedImageChoice[i] = WindowManager.getImageTitles()[i-1];
		}
		
		gd.addChoice("Binary splitted image", splittedImageChoice, splittedImageChoice[0]);
		gd.addCheckbox("Add_to_manager", true);
		gd.addCheckbox("Add_to_results_table", true);
		
		gd.addCheckbox("Merge_when_relativ_overlap_larger_than_threshold", true);
		gd.addSlider("Overlap threshold in %", 0, 100, 95);
		gd.addMessage("Geometric filters:");
		gd.addStringField("Major axis length", "0-Infinity");
		gd.addStringField("Minor axis length", "0-Infinity");
		gd.addStringField("Aspect ratio", "1-Infinity");
		gd.addHelp("http://fiji.sc/Ellipse_split");
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return DONE;
		}
		int choiceIndex = gd.getNextChoiceIndex();
		addToManager = gd.getNextBoolean();
		addToResultsTable = gd.getNextBoolean();
		merge = gd.getNextBoolean();
		overlappingThreshold = gd.getNextNumber()/100.0;
		majorAxisBounds = stringIntervalToArray(gd.getNextString(), "0-Infinity");
		minorAxisBounds = stringIntervalToArray(gd.getNextString(), "0-Infinity");
		aspectRatioBounds = stringIntervalToArray(gd.getNextString(), "1-Infinity");
		allEllipses = new ArrayList<ManyEllipses>();
		useSplitImage = false;
		if(choiceIndex>0){
			useSplitImage = true;
			splittedImage=WindowManager.getImage(splittedImageChoice[choiceIndex]);
		}
		return IJ.setupDialog(imp, FLAGS);
	}
	
	/**
	 * Splits an interval x-y (only - delimiter is supported)
	 * @param s Interval as string
	 * @param defaultvalue Return value if the splitting fails.
	 * @return [0] lower bound, [1] upper bound
	 */
	private double[] stringIntervalToArray(String s, String defaultvalue){
		double[] bounds = new double[2];
		String lim[] = s.split("-");
		if(lim.length < 2){
			lim = defaultvalue.split("-");
		}
		try{
		bounds[0] = Double.parseDouble(lim[0]);
		bounds[1] = Double.parseDouble(lim[1]);}
		catch(Exception e){
			lim = defaultvalue.split("-");
			bounds[0] = Double.parseDouble(lim[0]);
			bounds[1] = Double.parseDouble(lim[1]);
		}
		
		return bounds;
	}

	@Override
	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		RoiManager  rm = RoiManager.getInstance();
		if(rm!=null && addToManager){
			rm.reset();
			rm.runCommand("Show all");
		}
		if(results!=null&&addToResultsTable){
			results.reset();
		}
		int windowNumber = WindowManager.getImageTitles().length;
		String[] splittedImageChoice = new String[windowNumber+1];
		splittedImageChoice[0] = "Use standard watershed";
		for(int i = 1; i < windowNumber+1; i++){
			splittedImageChoice[i] = WindowManager.getImageTitles()[i-1];
		}
		
		int choiceIndex = gd.getNextChoiceIndex();
		addToManager = gd.getNextBoolean();
		addToResultsTable = gd.getNextBoolean();
		merge = gd.getNextBoolean();
		overlappingThreshold = gd.getNextNumber()/100.0;
		majorAxisBounds = stringIntervalToArray(gd.getNextString(), "0-Infinity");
		minorAxisBounds = stringIntervalToArray(gd.getNextString(), "0-Infinity");
		aspectRatioBounds = stringIntervalToArray(gd.getNextString(), "1-Infinity");
		allEllipses = new ArrayList<ManyEllipses>();
		useSplitImage = false;
		if(choiceIndex>0){
			useSplitImage = true;
			splittedImage=WindowManager.getImage(splittedImageChoice[choiceIndex]);
		}
		
		return !gd.invalidNumber();
	}
	


}
