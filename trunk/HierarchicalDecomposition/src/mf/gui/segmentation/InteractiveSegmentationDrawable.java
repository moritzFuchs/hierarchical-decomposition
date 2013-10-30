package mf.gui.segmentation;

import java.util.HashSet;
import java.util.Set;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.jgrapht.experimental.decomposition.DecompositionTree;

import mf.gui.ButtonRow;
import mf.gui.Markable;
import mf.gui.Pixel;
import mf.gui.decomposition.Drawable;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;

public class InteractiveSegmentationDrawable extends Drawable{

	private SuperpixelDecomposition dec;
	
	private DecompositionTree<Integer> t;
	
	private Set<Superpixel> marker;
	
	public InteractiveSegmentationDrawable(String name, Markable m,
			ButtonRow buttonRow, DecompositionTree<Integer> t, SuperpixelDecomposition dec) {
		super(name, m, buttonRow);

		this.t = t;
		this.dec = dec;
		
		this.marker = new HashSet<Superpixel>();
	}

	@Override
	public void draw() {
		//TODO: compute segmentation
	}

	@Override
	public void onActivate() {
		Button done = new Button("Done");
		Button reset = new Button("Reset");
		
		//TODO: Add Handler for done button
		
		//TODO: Add Handler for reset button
		
		buttonRow.addButton(done);
		buttonRow.addButton(reset);
	}
	
	public void reset() {
		marker.clear();
		m.clear();
	}
	
	public void addMarker(Pixel p) {
		Superpixel sp = dec.getSuperpixelByPixel(p);
		marker.add(sp);
		
		markSuperpixel(sp);
	}
	
	private void markSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getBoundaryPixels()) {
			m.markPixelComplementary(p.getX(), p.getY());
		}
	}
	
	private void unmarkSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getBoundaryPixels()) {
			m.clearPixel(p.getX(),p.getY());
		}
	}
	
	public void removeMarker(Pixel p) {
		Superpixel sp = dec.getSuperpixelByPixel(p);
		marker.remove(sp);
		unmarkSuperpixel(sp);
	}

	
	public void handleMouseEvent(MouseEvent event) {
		
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			MouseEvent mouse_event = (MouseEvent) event; 
			Double x =  mouse_event.getX();
			Double y = mouse_event.getY();
			if (mouse_event.getButton().equals(MouseButton.PRIMARY)) {
				addMarker(new Pixel(x.intValue(),y.intValue()));		
			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				removeMarker(new Pixel(x.intValue(),y.intValue()));
			}
		}
	}
	
	
	
	
	
	
}
