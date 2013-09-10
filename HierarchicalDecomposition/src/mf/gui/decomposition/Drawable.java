package mf.gui.decomposition;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import mf.gui.Markable;

//TODO: better documentation
/**
 * Interface for drawable 'stuff' that is put into the list on the left. 
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public abstract class Drawable implements EventHandler<Event>{

	/**
	 * Label that will be displayed in the list.
	 */
	private String name;
	
	/**
	 * The {@link Markable} we want to print on.
	 */
	protected Markable m;
	
	public Drawable(String name , Markable m) {
		this.name = name;
		this.m = m;
	}
	
	/**
	 * Draws something on the {@link Markable} {@link Drawable.m}
	 */
	public abstract void draw();

	/**
	 * Returns the label of this item
	 */
	public String toString() {
		return name;
	}

	
	public void handle(Event event) {
		
		if (event instanceof MouseEvent) {
			handleMouseEvent((MouseEvent)event);
		}
		
		if (event instanceof ScrollEvent) {
			handleScrollEvent((ScrollEvent)event);
		}
	}
	
	public void activate() {
		m.registerMouseHandler(this);
		m.registerScrollHandler(this);
	}
	
	public void handleMouseEvent(MouseEvent event) {
		event.consume();
	}
	
	public void handleScrollEvent(ScrollEvent event) {
		event.consume();
	}
	
}
