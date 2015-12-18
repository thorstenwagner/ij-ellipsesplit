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
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.plugin.filter.Analyzer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
/**
 * This class implements an listener for interactive select a blob in an image and the corresponding row in the results table.
 **/
public class ImageResultsTableSelector implements MouseListener {
	
	private ImagePlus imp;
	int imgID;
	public static boolean isEllipseSelected;
	public ImageResultsTableSelector(ImagePlus imp) {
		// TODO Auto-generated constructor stub
		this.imp = imp;
		isEllipseSelected = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		double mag = ((ImageCanvas)imp.getWindow().getComponent(0)).getMagnification();
		double x = (e.getX()*1.0/mag );
		double y = (e.getY()*1.0/mag);
		
		Overlay ov = imp.getOverlay();
		if(ov==null){
			ov = new Overlay();
			imp.setOverlay(ov);
		}else{
			ov.clear();
		}
		isEllipseSelected = false;
		for(int i = 0; i < Analyzer.getResultsTable().getCounter(); i++){
			int slice = (int)Analyzer.getResultsTable().getValueAsDouble(0, i);
			if(slice==imp.getSlice()){
				
				int ellipseLabel= (int)Analyzer.getResultsTable().getValueAsDouble(1, i);
				Ellipse el = EllipseSplit_.getInstance().getEllipseByFrameAndLabel(slice-1, ellipseLabel);
				if(el==null){
					break;
				}
				if(el.getPolygon().contains(x, y)){
					IJ.getTextPanel().setSelection(i, i);
					
					PolygonRoi pr = (PolygonRoi) el.getRoi();
					pr.setStrokeWidth(2);
					pr.setPosition(slice);
					ov.add(pr);
					imp.repaintWindow();
					isEllipseSelected = true;
					break;
				}
			}
			else if(slice >imp.getSlice()){
				break;
			}
		}
	}
	
	public void setTargetImage(int id){
		imgID = id;
		if(imp.getWindow()!=null){
			imp.getWindow().getComponent(0).removeMouseListener(this);
			imp = WindowManager.getImage(imgID);
			imp.getWindow().getComponent(0).addMouseListener(this);
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
