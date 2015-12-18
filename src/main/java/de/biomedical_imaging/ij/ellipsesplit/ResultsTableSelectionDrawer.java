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
