package mf.gui.decomposition;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import mf.gui.Markable;
import mf.gui.Pixel;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;

public class SuperpixelDrawable extends Drawable implements EventHandler<Event>{

	
	private SuperpixelDecomposition dec;
	
	public SuperpixelDrawable(SuperpixelDecomposition dec, String name, Markable m) {
		super(name , m);		
		this.dec = dec;
	}
	
	/**
	 * Paint the decomposition on a given {@link Markable}
	 * 
	 * @param m : The {@link Markable}
	 */
	public void draw() {
		m.clear();
		m.startLoading();
		for (Superpixel sp : dec.getSuperpixelMap().values()) {
			drawSuperpixel(sp);
		}
		m.stopLoading();
	}

	/**
	 * Draws a single {@link Superpixel} onto the drawable.
	 * 
	 * @param sp : The {@link Superpixel} we want to draw.
	 */
	private void drawSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getBoundaryPixels()) {
			m.markPixel(p.getX(), p.getY());
		}
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
				
				drawSuperpixel(dec.getSuperpixelByPixel(new Pixel(x.intValue(),y.intValue())));

			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				draw();
			}
		}
		
		event.consume();
	}
	
	@Override
	public void handleKeyEvent(KeyEvent event) {
		System.out.println(event);
	}
	
	@Override
	public void handleScrollEvent(ScrollEvent event) {
		event.consume();
	}
	
}
