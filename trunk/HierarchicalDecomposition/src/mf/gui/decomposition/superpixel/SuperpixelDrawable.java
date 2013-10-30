package mf.gui.decomposition.superpixel;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mf.gui.ButtonRow;
import mf.gui.Markable;
import mf.gui.Pixel;
import mf.gui.decomposition.Drawable;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;

public class SuperpixelDrawable extends Drawable implements EventHandler<Event>{

	private SuperpixelDecomposition dec;
	
	private Superpixel current_superpixel = null;
	
	/**
	 * Mean Image where every {@link Superpixel} is replaced by a area with its average color. 
	 */
	private Image mean_img = null;
	
	/**
	 * True if all superpixels are shown, false otherwise
	 */
	private Boolean superpixels_shown = false;
	
	/**
	 * True if mean image is currently shown. False otherwise
	 */
	private Boolean mean_image_shown = false;
	
	public SuperpixelDrawable(SuperpixelDecomposition dec, String name, Markable m, ButtonRow buttonRow) {
		super(name , m, buttonRow);		
		this.dec = dec;
	}
	
	/**
	 * Paint the decomposition on a given {@link Markable}
	 * 
	 * @param m : The {@link Markable}
	 */
	public void draw() {
		
		m.startLoading();
		m.clear();
		for (Superpixel sp : dec.getSuperpixelMap().values()) {
			drawSuperpixel(sp);
		}
		m.stopLoading();
		
	}

	/**
	 * Hide / Show superpixel decomposition on the current image
	 */
	public void toggleSuperpixelDecomposition() {
		if (!superpixels_shown) {
			draw();
			superpixels_shown = true;
		} else {
			//Note: only clear NOT restore Image
			m.clear();
			superpixels_shown = false;
		}
	}
	
	/**
	 * Reset the canvas
	 */
	public void reset() {
		current_superpixel = null;
		mean_image_shown = false;
		superpixels_shown = false;
		
		m.clear();
		m.resetImage();
	}
	
	/**
	 * Draws a single {@link Superpixel} onto the drawable.
	 * 
	 * @param sp : The {@link Superpixel} we want to draw.
	 */
	private void drawSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getBoundaryPixels()) {
			m.markPixelComplementary(p.getX(), p.getY());
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
				Double x =  mouse_event.getX();
				Double y = mouse_event.getY();

				current_superpixel = dec.getSuperpixelByPixel(new Pixel(x.intValue(),y.intValue()));
				drawSuperpixel(current_superpixel);
			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				draw();
			}
		}

		if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
			if (current_superpixel != null) {
				Double x =  event.getX();
				Double y = event.getY();

				Superpixel next = dec.getSuperpixelByPixel(new Pixel(x.intValue(),y.intValue()));

				m.clear();
				drawSuperpixel(current_superpixel);
				drawSuperpixel(next);
				m.drawText("Weight: " + current_superpixel.getEdgeWeight(next));
			}
		}
		event.consume();
	}
		
	
	/**
	 * Clear the canvas when this Drawable is activated
	 */
	@Override
	public void onActivate() {
		m.clear();
		
		Button showAll = new Button("Toggle Superpixels");
		showAll.setOnAction(new ToggleSuperpixelHandler(this));
		
		Button hideAll = new Button("Clear");
		hideAll.setOnAction(new HideSuperpixelHandler(this));
		
		Button loadingTest = new Button("Toggle Mean Image");
		loadingTest.setOnAction(new MeanImageHandler(this));
		
		buttonRow.getChildren().add(showAll);
		buttonRow.getChildren().add(hideAll);
		buttonRow.getChildren().add(loadingTest);
	}


	/**
	 * Hide / Show mean image (image where all superpixels are repalced by an area with color equal to the mean color of the superpixel)
	 */
	public void toggleMeanImage() {
		if (!mean_image_shown) {
			if (mean_img == null) {
				WritableImage img = new WritableImage(m.getImageWidth(), m.getImageHeight());
				PixelWriter w = img.getPixelWriter();
		
				for (Superpixel sp : dec.getSuperpixelMap().values()) {
					Double[] c_val = sp.getMeanRGB();
					for (Pixel p : sp.getPixel()) {
						Color c = new Color(c_val[0],c_val[1],c_val[2],1.0);
						w.setColor(p.getX(), p.getY(), c);
					}
				}
				
				mean_img = img;
			}
			
			m.setImage(mean_img);
			mean_image_shown = true;
		} else {
			m.resetImage();
			mean_image_shown = false;
		}
	}
}
