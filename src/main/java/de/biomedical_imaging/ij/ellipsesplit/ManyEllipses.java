package de.biomedical_imaging.ij.ellipsesplit;

import java.util.ArrayList;

public class ManyEllipses extends ArrayList<Ellipse> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Ellipse getEllipseByLabel(int label){
		for (Ellipse e : this) {
			if(e.getLabel()==label){
				return e;
			}
		}
		return null;
		
	}

}
