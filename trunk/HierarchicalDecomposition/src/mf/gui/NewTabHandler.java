package mf.gui;

import java.io.File;

import mf.gui.decomposition.Drawable;
import mf.gui.decomposition.NoDecomposition;
import mf.gui.decomposition.SuperpixelDecomposition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class NewTabHandler implements EventHandler<Event> {

	@Override
	public void handle(Event event) {
		
		Tab source = (Tab)event.getSource();
		TabPane pane = source.getTabPane();
		
		if (source.isSelected()) {

			//Replace with custom format for the folder
			DirectoryChooser dir_chooser = new DirectoryChooser();
			dir_chooser.setTitle("Please select a decomposition folder.");
			File dir = dir_chooser.showDialog(null);
			String path = dir.getPath();
			
			if (!dir.isDirectory()) {
				pane.getSelectionModel().select(0);
				return;
			}
			
			//TODO: Assert that image exists in folder
			
			String img_path = path + "/image.jpg";
			
			Tab new_tab = new Tab();
			new_tab.setText("New Tab");
		
			BorderPane b = new BorderPane();
			
			ListView<Drawable> list = new ListView<Drawable>();
			ObservableList<Drawable> items = FXCollections.observableArrayList ();

		    BorderPane inner = new BorderPane();
		    b.setCenter(inner);
		    
			DrawableImageView drawable = new DrawableImageView(img_path);
		    
//          canvas.setOnMouseClicked(new ClickHandler<MouseEvent>());
		    for (File file : dir.listFiles()) {
		    	String name = file.getName();
		    	
		    	System.out.println(name);
		    	
		    	if (name.toLowerCase().startsWith("superpixel") && name.toLowerCase().endsWith(".mat")) {
		    		SuperpixelImport imp = new SuperpixelImport(file.getPath() , drawable.getImage());
		    		SuperpixelDecomposition super_dec = new SuperpixelDecomposition(imp.getSuperpixels(), file.getName(), drawable);
		    		
		    		items.add(super_dec);
		    		super_dec.draw();
		    	}
		    	
		    	//TODO: Add more decompositions (KRV , Region growing)
		    }
		    
		    items.add(new NoDecomposition(drawable));
		    
		    drawable.export(new File("combined.png"));
		    
			list.setItems(items);
			list.setOnMouseClicked(new DecompositionSelectedHandler());
			list.setEditable(false);
			b.setLeft(list);
		    
			Button button = new Button("Save Image");
			button.setOnAction(drawable);
			
			b.setBottom(button);
			
            inner.setCenter(drawable);
			new_tab.setContent(b);
		
			pane.getTabs().add(pane.getTabs().size()-2, new_tab);
			pane.getSelectionModel().select(new_tab);
		}
	}
}
