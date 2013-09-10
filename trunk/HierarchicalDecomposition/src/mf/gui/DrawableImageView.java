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
public class DrawableImageView extends StackPane implements Markable {
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
	 * URL of the image
	 */
	private String url;
	
	/**
	 * The current {@link MouseEvent}-Handler
	 */
	private EventHandler<? super MouseEvent> current_mouse_handler = null;
	
	/**
	 * The current {@link ScrollEvent}-Handler
	 */
	private EventHandler<? super ScrollEvent> current_scroll_handler;
	
	public DrawableImageView(String url) {
		super();

		this.url = url;
		
		img = new Image("file:" + url);
		img_view = new ImageView(img);
		
		canvas = new Canvas(img.getWidth() , img.getHeight());
		canvas.setMouseTransparent(true);
		canvas.setOpacity(0.8);
		
		loader = new Canvas(img.getWidth() , img.getHeight());
		loader.setMouseTransparent(true);

		loader.getGraphicsContext2D().setFill(new Color(1.0,1.0,1.0,1.0));
		loader.getGraphicsContext2D().fill();
		loader.setOpacity(0.0);
		
		this.setHeight(img.getHeight());
		this.setWidth(img.getWidth());
		
		getChildren().add(this.img_view);
		getChildren().add(this.canvas);
		getChildren().add(this.loader);
		//TODO: Add context menu for saving image
		
	}
	
	/**
	 * Exports the Image and the drawing-canvas into an image.
	 * 
	 * @param file : Export file
	 */
	@Override
	public void export(File file) {
		Integer width = (int)canvas.getWidth();
		Integer height = (int)canvas.getHeight();
		
		WritableImage wim = new WritableImage(width, height);
		
		try {
			BufferedImage out = ImageIO.read(new File(url));
			
			SnapshotParameters param = new SnapshotParameters();
			param.setFill(Color.TRANSPARENT);
			canvas.snapshot(param, wim);

			BufferedImage combined = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			Graphics g = combined.getGraphics();
			g.drawImage(out, 0, 0, null);
			g.drawImage(SwingFXUtils.fromFXImage(wim, null), 0, 0, null);
			ImageIO.write(combined, "PNG", file);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Marks a given coordinate using the default color.
	 * 
	 * @param x : x-coordinate
	 * @param y : y-coordinate 
	 */
	public void markPixel(Integer x , Integer y) {
		PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();
		writer.setColor(x, y, Color.rgb(255, 255, 255));
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
			PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();
			writer.setColor(x, y, c);
		}
	}
	
	/**
	 * Returns the color of the specified pixel
	 * 
	 * @param x : x-coordinate of the pixel
	 * @param y : y-coordinate of the pixel
	 * @return Color : Color of the given pixel 
	 */
	@Override
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
		
	/**
	 * Returns the underlying image
	 * 
	 * @return Image : The Image on the bottom layer
	 */
	public Image getImage() {
		return img;
	}
	
	/**
	 * Show loading screen / animation
	 */
	public void startLoading() {
		loader.setOpacity(0.8);
	}
	
	/**
	 * Hide loading screen / animation
	 */
	public void stopLoading() {
		loader.setOpacity(0.0);
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
}
