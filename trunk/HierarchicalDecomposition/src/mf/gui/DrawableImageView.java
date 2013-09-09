package mf.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class DrawableImageView extends StackPane implements Markable, EventHandler<ActionEvent> {
	private Image img;
	private ImageView img_view;
	private Canvas canvas;
	private Canvas loader;
	private String url;
	
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
	
	//TODO: Add custom color
	public void markPixel(Integer x , Integer y) {
		PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();
		writer.setColor(x, y, Color.rgb(255, 255, 255));
	}
	
	public void markArea(Integer[] x , Integer[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Dimensions of x- and y-coordinates must match.");
		}
		for (int i=0;i<x.length;i++) {
			markPixel(x[i],y[i]);
		}
	}
		

	public Image getImage() {
		return img;
	}
	
	public void startLoading() {
		loader.setOpacity(0.8);
	}
	
	public void stopLoading() {
		loader.setOpacity(0.0);
	}
	
	
	public void clear() {
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}


	@Override
	public void handle(ActionEvent event) {
		
		Button source = (Button)event.getSource();
		System.out.println(source.getText());
		if (source.getText().compareTo("Save Image") == 0) {
			
			FileChooser chooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
			chooser.getExtensionFilters().add(extFilter);
			File file = chooser.showSaveDialog(null);
			
			export(file);
		}
	}
}
