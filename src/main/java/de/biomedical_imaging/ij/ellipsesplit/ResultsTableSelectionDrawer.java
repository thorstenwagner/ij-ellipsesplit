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
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.plugin.filter.Analyzer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
/**
 * This class implements an listener for interactively select a row in the results table and the corresponding blob in an image.
 **/
public class ResultsTableSelectionDrawer implements MouseListener {
	
	public int selectionStart = -1;
	public int selectionStop = -1;
	public ImagePlus imp;
	private int imgID;

	public ResultsTableSelectionDrawer(ImagePlus imp) {
		this.imp = imp;
		imgID = imp.getID();
	
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		update(IJ.getTextPanel().getSelectionStart(), IJ.getTextPanel().getSelectionEnd());
	}
	
	public void update(int start, int end){
		
		if(selectionStart!= start || selectionStop != end ){
			selectionStart = start;
			selectionStop = end;
			if(selectionStart>=0){
				showAsOverlay(selectionStart, selectionStop);
				
			}
		}
	}
	
	public void setTargetImage(int id){
		imgID = id;
		imp = WindowManager.getImage(imgID);
	}
	
	public void showAsOverlay(int start, int end){
		
		IJ.selectWindow(imgID);
		Overlay ov = imp.getOverlay();
		if(ov==null){
			ov = new Overlay();
			imp.setOverlay(ov);
		}else{
			ov.clear();
		}
		int firstSlice = -1;
		
		for(int i = start; i <= end; i++) {
			
			int slice = (int)Analyzer.getResultsTable().getValueAsDouble(0, i);
			int ellipseLabel= (int)Analyzer.getResultsTable().getValueAsDouble(1, i);
			Ellipse el = EllipseSplit_.getInstance().getEllipseByFrameAndLabel(slice-1, ellipseLabel);

			PolygonRoi pr = (PolygonRoi) el.getRoi();
			pr.setStrokeWidth(2);
			pr.setPosition(slice);
			ov.add(pr);
			if(firstSlice==-1){
				firstSlice = slice;
			}

		}


		imp.setSlice(firstSlice);
		imp.repaintWindow();
		
		
		// setOverlay(ov);
	}

}
