package mf.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class CustomCanvas extends Canvas{

	private String img_path;
	
	public CustomCanvas(String path) {
		super(new Image("file:" + path).getWidth(),new Image("file:" + path).getHeight());
		this.img_path = path;
		GraphicsContext graphics_context = getGraphicsContext2D();
		graphics_context.drawImage(new Image("file:" + path),0,0);
	}
	
	//TODO: Currently overrides pixels, replace canvas with ImageView + Canvas overlay?
	public void markPixel(Integer x , Integer y) {
		PixelWriter writer = this.getGraphicsContext2D().getPixelWriter();
		writer.setColor(x, y, new Color(1f,1f,1f,0.5f));
	}
	
	public void clear() {
		this.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
		this.getGraphicsContext2D().drawImage(new Image("file:"+img_path), 0, 0);
	}
}
