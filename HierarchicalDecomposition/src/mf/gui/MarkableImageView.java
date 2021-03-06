package mf.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Stacks an {@link ImageView} and 2 {@link Canvas} to create a drawable Image view.
 * The Canvas are used as follows:
 *  * Canvas #1: Canvas for drawing over the image
 *  * Canvas #2: Canvas for displaying loading screen/animation
 * 
 * @author moritzfuchs
 * @date 10.09.2013
 *
 */
public class MarkableImageView extends StackPane implements Markable {
	/**
	 * Image for the image view.
	 */
	private Image img;
	
	/**
	 * The Image view (bottom layer)
	 */
	private ImageView img_view;
	
	/**
	 * {@link Canvas} to draw 'on' the image
	 */
	private Canvas canvas;
	
	/**
	 * {@link Canvas} to show loading screen/animation
	 */
	private Canvas loader;
	

	/**
	 * The current {@link MouseEvent}-Handler
	 */
	private EventHandler<? super MouseEvent> current_mouse_handler = null;
	
	/**
	 * The current {@link ScrollEvent}-Handler
	 */
	private EventHandler<? super ScrollEvent> current_scroll_handler;
	
	/**
	 * The current {@link KeyEvent}-Handler
	 */
	private EventHandler<? super KeyEvent> current_key_handler;

	private PixelWriter canvas_writer;
	
	/**
	 * True if loading animation is shown, false otherwise
	 */
	private Boolean loading = false;
	
	public MarkableImageView(String url) {
		super();

		img = new Image("file:" + url);
		img_view = new ImageView(img);
		
		canvas = new Canvas(img.getWidth() , img.getHeight());
		canvas.setMouseTransparent(true);
		canvas.setOpacity(0.8);
		canvas.setCache(true);
		
		loader = new Canvas(img.getWidth() , img.getHeight());
		loader.setOpacity(0.5);
		loader.getGraphicsContext2D().setFill(new Color(0.0,0.0,0.0,1.0));
		loader.getGraphicsContext2D().fillRect(0, 0, img.getWidth(), img.getHeight());
		
		this.setHeight(img.getHeight());
		this.setWidth(img.getWidth());
		
		getChildren().add(this.img_view);
		getChildren().add(this.canvas);
		getChildren().add(this.loader);

		loader.toBack();
		
		canvas_writer = canvas.getGraphicsContext2D().getPixelWriter();
		
	}
	
	
	public int getImageWidth() {
		return (int)img.getWidth();
	}
	
	public int getImageHeight() {
		return (int)img.getHeight();
	}
	
	/**
	 * Exports the Image and the drawing-canvas into an image.
	 * 
	 * @param file : Export file
	 * @throws IOException 
	 */
	@Override
	public void export(File file) throws IOException {
		Integer width = (int)canvas.getWidth();
		Integer height = (int)canvas.getHeight();
		
		WritableImage wim = new WritableImage(width, height);
		
		SnapshotParameters param = new SnapshotParameters();
		param.setFill(Color.TRANSPARENT);
		canvas.snapshot(param, wim);

		BufferedImage combined = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = combined.getGraphics();
		g.drawImage(SwingFXUtils.fromFXImage(img_view.getImage(), null), 0, 0, null);
		g.drawImage(SwingFXUtils.fromFXImage(wim, null), 0, 0, null);
		ImageIO.write(combined, "PNG", file);
	}
	
	/**
	 * Set a different {@Image} to be painted
	 * 
	 * @param image : New {@link Image} to be painted
	 */
	public void setImage(Image image) {
		img_view.setImage(image);
	}
	
	/**
	 * Restore {@link Image} that was originally painted
	 */
	public void resetImage() {
		img_view.setImage(img);
	}
	
	/**
	 * Resets the whole Markable; Image is set back, canvas is cleared and handlers are unregistered.
	 */
	public void reset() {
		resetImage();
		clear();
		
		if (current_mouse_handler != null) {
			img_view.removeEventFilter(MouseEvent.ANY, current_mouse_handler);
		}
		
		if (current_scroll_handler != null) {
			img_view.removeEventFilter(ScrollEvent.ANY, current_scroll_handler);
		}
		
		if (current_key_handler != null) {
			img_view.removeEventFilter(KeyEvent.ANY, current_key_handler);
		}
		
	}
	
	/**
	 * Returns a {@link PixelWriter}. Use the {@link PixelWriter} and a buffer for high performance. 
	 * 
	 * @return {@link PixelWriter}
	 */
	public PixelWriter getPixelWriter() {
		return canvas_writer;
	}
	
	/**
	 * Marks a given coordinate using the default color.
	 * 
	 * @param x : x-coordinate
	 * @param y : y-coordinate 
	 */
	public void markPixel(Integer x , Integer y) {
		canvas_writer.setColor(x, y, Color.rgb(255, 255, 255));
		
	}
	
	/**
	 * Marks a given coordinate using a 'complementary' (approximately!) color.
	 * 
	 * @param x : x-coordinate
	 * @param y : y-coordinate
	 */
	public void markPixelComplementary(Integer x , Integer y) {
		Color pixel_color = getColor(x,y);
		Color c = new Color(1.0-pixel_color.getRed(), 1.0-pixel_color.getGreen(), 1.0-pixel_color.getBlue(), 1.0);
		canvas_writer.setColor(x, y, c);
	}
	
	/**
	 * Marks the given coordinate using the given color. Does nothing if the given pixel coordinates are outside of the image range.
	 * 
	 * @param x : x-coordinate
	 * @param y : y-coordinate
	 * @param c : color 
	 */
	public void markPixel(Integer x , Integer y, Color c) {
		if (x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) {
			canvas_writer.setColor(x, y, c);
		}
	}
	
	@Override
	public void clearPixel(Integer x, Integer y) {
		canvas_writer.setColor(x, y, Color.TRANSPARENT);
	}
	
	/**
	 * Returns the color of the specified pixel
	 * 
	 * @param x : x-coordinate of the pixel
	 * @param y : y-coordinate of the pixel
	 * @return Color : Color of the given pixel 
	 */
	public Color getColor(Integer x , Integer y) {
		return img_view.getImage().getPixelReader().getColor(x, y);
	}
	
	/**
	 * Marks all pixels given by the x- and y-Array
	 * 
	 * @param x : x-coordinates of pixels that shall be marked
	 * @param y : y-coordinates of pixels that shall be marked
	 */
	public void markArea(Integer[] x , Integer[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Dimensions of x- and y-coordinates must match.");
		}
		
		for (int i=0;i<x.length;i++) {
			markPixel(x[i],y[i]);
		}
	}
		
	public void drawText(String s) {
		canvas.getGraphicsContext2D().fillText(s, 10, 20);
	}
	
	/**
	 * Returns the underlying image
	 * 
	 * @return Image : The Image on the bottom layer
	 */
	public Image getImage() {
		return img;
	}
	
	/**
	 * Returns True if loading animation is shown, false otherwise
	 * 
	 * @return Boolean : True if loading animation is shown, false otherwise
	 */
	public Boolean isLoading() {
		return loading;
	}
	
	/**
	 * Show loading screen / animation
	 */
	public void startLoading() {
		loader.toFront();
		loading = true;
	}
	
	/**
	 * Hide loading screen / animation
	 */
	public void stopLoading() {
		loader.toBack();
		loading = false;
	}
	
	/**
	 * Clears the drawing-canvas, s.t. only the Image on the bottom layer is visible
	 */
	public void clear() {
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	/**
	 * Register {@link EventHandler} for {@link MouseEvent}s. Deletes the old handler, s.t. max. 1 handler is active at each point in time.
	 * 
	 * @param handler : The {@link EventHandler} for {@link MouseEvent}s.
	 */
	public void registerMouseHandler(EventHandler<? super MouseEvent> handler) {
		if (current_mouse_handler != null) {
			img_view.removeEventFilter(MouseEvent.ANY, current_mouse_handler);
		}
		
		current_mouse_handler = handler;
		img_view.addEventFilter(MouseEvent.ANY, current_mouse_handler);
	}
	
	/**
	 * Register {@link EventHandler} for {@link ScrollEvent}s. Deletes the old handler, s.t. max. 1 handler is active at each point in time.
	 * 
	 * @param handler : The {@link EventHandler} for {@link ScrollEvent}s.
	 */
	public void registerScrollHandler(EventHandler<? super ScrollEvent> handler) {
		if (current_scroll_handler != null) {
			img_view.removeEventFilter(ScrollEvent.ANY, current_scroll_handler);
		}
		
		current_scroll_handler = handler;
		img_view.addEventFilter(ScrollEvent.ANY, current_scroll_handler);
	}
	
	/**
	 * Register {@link EventHandler} for {@link KeyEvent}s. Deletes the old handler, s.t. max. 1 handler is active at each point in time.
	 * 
	 * @param handler : The {@link EventHandler} for {@link KeyEvent}s.
	 */
	public void registerKeyHandler(EventHandler<? super KeyEvent> handler) {
		if (current_key_handler != null) {
			img_view.removeEventFilter(KeyEvent.ANY, current_key_handler);
		}
		
		current_key_handler = handler;
		img_view.addEventFilter(KeyEvent.ANY, current_key_handler);
	}

}
