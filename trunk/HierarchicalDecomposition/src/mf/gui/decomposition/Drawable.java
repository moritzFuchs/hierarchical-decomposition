package mf.gui.decomposition;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import mf.gui.Markable;

/**
 * Interface for Objects that want to be able to draw on a {@link Markable} and appear in the list of decompositions.
 * Implements Handlers for {@link MouseEvent} and {@link ScrollEvent}, s.t. only {@link Drawable.handleMouseEvent} and 
 * {@link Drawable.handleScrollEvent} need to be overwritten. 
 * 
 * @author moritzfuchs
 * @date 10.09.2013
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

	/**
	 * Receives an event. If the event is a {@link MouseEvent} it calls {@link Drawable.handleMouseEvent}, 
	 * if the event is a {@link ScrollEvent} it calls {@link Drawable.handleMouseEvent}
	 */
	public final void handle(Event event) {
		
		if (event instanceof MouseEvent) {
			handleMouseEvent((MouseEvent)event);
		}
		
		if (event instanceof ScrollEvent) {
			handleScrollEvent((ScrollEvent)event);
		}
		
		if (event instanceof KeyEvent) {
			handleKeyEvent((KeyEvent)event);
		}
		
	}
	
	/**
	 * Registers handlers to the markable object. Currently the following handlers are registered:
	 *  * MouseEventHandler
	 *  * ScrollEventHandler
	 * All EventHandlers point back to {@link Drawable.handle}, which then calls {@link Drawable.handleMouseEvent} or {@link Drawable.handleScrollEvent}.
	 * To implement custom bahavior override {@link Drawable.handleMouseEvent} and {@link Drawable.handleScrollEvent}.
	 */
	public final void activate() {
		m.registerMouseHandler(this);
		m.registerScrollHandler(this);
		m.registerKeyHandler(this);
		
		onActivate();
	}
	
	/**
	 * Handler for a {@link MouseEvent}. Does nothing but consume the event.
	 * To implement custom behavior for mouse events override this method.
	 * 
	 * @param event MouseEvent: The {@link MouseEvent} 
	 */
	public void handleMouseEvent(MouseEvent event) {
		event.consume();
	}
	
	/**
	 * Handler for a {@link ScrollEvent}. Does nothing but consume the event.
	 * To implement custom behavior for scroll events override this method.
	 * 
	 * @param event : The {@link ScrollEvent} 
	 */
	public void handleScrollEvent(ScrollEvent event) {
		event.consume();
	}
	
	/**
	 * Handler for a {@link KeyEvent}. Does nothing but consume the event.
	 * To implement custom behavior for scroll events override this method.
	 * 
	 * @param event : The {@link KeyEvent} 
	 */
	public void handleKeyEvent(KeyEvent event) {
		event.consume();
	}
	
	/**
	 * Called when the Object is activated. OVERRIDE ME!
	 */
	public void onActivate() {}
}
