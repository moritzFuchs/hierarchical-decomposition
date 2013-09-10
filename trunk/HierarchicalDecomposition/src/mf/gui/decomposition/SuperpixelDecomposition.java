package mf.gui.decomposition;


import java.util.Map;

import javafx.event.Event;
import javafx.event.EventHandler;
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
	/**
	 * Handle a Mouse event: 
	 *  * Left  click => Clear canvas and mark clicked pixel
	 *  * Right click => Show Superpixels
	 *  
	 *  @param event : The {@link MouseEvent}
	 */
	public void handleMouseEvent(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			MouseEvent mouse_event = (MouseEvent) event; 
			if (mouse_event.getButton().equals(MouseButton.PRIMARY)) {
				m.clear();
				Double x =  mouse_event.getX();
				Double y = mouse_event.getY();

				Color c = m.getColor(x.intValue(),y.intValue());
				
				//Mark with 'complementary' color
				m.markPixel(x.intValue(), y.intValue(), new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue()+1, y.intValue(), new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue()-1, y.intValue(), new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue(), y.intValue()+1, new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue(), y.intValue()-1, new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));

			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				draw();
			}
		}
		
		event.consume();
	}
	
	@Override
	public void handleScrollEvent(ScrollEvent event) {
		event.consume();
	}
	
}
