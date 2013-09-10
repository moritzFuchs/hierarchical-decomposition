package mf.gui.decomposition;


import java.util.Map;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import mf.gui.Markable;
import mf.gui.Pixel;
import mf.superpixel.Superpixel;

public class SuperpixelDecomposition extends Drawable implements EventHandler<Event>{

	/**
	 * The superpixel map from superpixel numbers ({@link Integer}) to {@link Superpixel}
	 */
	private Map<Integer , Superpixel> superpixel;
	
	public SuperpixelDecomposition(Map<Integer , Superpixel> pixel, String name, Markable m) {
		super(name , m);
		this.superpixel = pixel;
	}
	
	/**
	 * Paint the decomposition on a given {@link Markable}
	 * 
	 * @param m : The {@link Markable}
	 */
	public void draw() {
		m.startLoading();
		m.clear();
		for (Superpixel sp : superpixel.values()) {
			for (Pixel p : sp.getBoundaryPixels()) {
				m.markPixel(p.getX(), p.getY());
			}
		}
		m.stopLoading();
	}

	@Override
	public void handleMouseEvent(MouseEvent event) {
		System.out.println("MOUSE!");
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			MouseEvent mouse_event = (MouseEvent) event; 
			if (mouse_event.getButton().equals(MouseButton.PRIMARY)) {
				m.clear();
				System.out.println("Handling click event.");
				Double x =  mouse_event.getSceneX();
				Double y = mouse_event.getSceneY();
				
				m.markPixel(x.intValue(), y.intValue(), new Color(0.0, 0.0, 0.0, 1.0));
			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				draw();
			}
		}
		
		event.consume();
	}
	
	@Override
	public void handleScrollEvent(ScrollEvent event) {
		System.out.println("SCROLLING!!");
		event.consume();
	}
	
}
