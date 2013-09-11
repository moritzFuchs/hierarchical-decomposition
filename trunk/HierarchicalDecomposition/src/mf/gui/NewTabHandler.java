package mf.gui;

import java.io.File;

import name.antonsmirnov.javafx.dialog.Dialog;
import mf.gui.decomposition.Drawable;
import mf.gui.decomposition.KRVDecomposition;
import mf.gui.decomposition.NoDecomposition;
import mf.gui.decomposition.SuperpixelDrawable;
import mf.superpixel.SuperpixelDecomposition;
import mf.superpixel.SuperpixelImport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;

/**
 * Handles the creation of a new Tab.
 * 
 * @author moritzfuchs
 * @date 05.09.2013
 *
 */
public class NewTabHandler implements EventHandler<Event> {

	/**
	 * Create a new Tab:
	 * A Tab consists of a {@link BorderPane} with a {@link ListView} on the left position and a {@link DrawableImageView} on the center position. 
	 */
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
			String img_path = path + "/image.jpg";
			
			File img = new File(img_path);
			
			if (!dir.isDirectory()) {
				Dialog.showWarning("Not a folder.", "Sorry, this is not a folder.");
				pane.getSelectionModel().select(0);
				return;
			}
			
			if (!img.exists()) {
				Dialog.showWarning("No image found in folder", "Sorry, I could not find a image in this folder. Make sure that there exists an image named 'image.jpg'.");
				pane.getSelectionModel().select(0);
				return;
			}
			
			Tab new_tab = new Tab();
			new_tab.setText(dir.getName());
		
			BorderPane b = new BorderPane();
			
			ListView<Drawable> list = new ListView<Drawable>();
			ObservableList<Drawable> items = FXCollections.observableArrayList ();

		    BorderPane inner = new BorderPane();
		    b.setCenter(inner);
		    
		    
		    
		    
			DrawableImageView drawable = new DrawableImageView(img_path);
		    
//          canvas.setOnMouseClicked(new ClickHandler<MouseEvent>());
		    for (File file : dir.listFiles()) {
		    	String name = file.getName();
		    	
		    	if (name.toLowerCase().startsWith("superpixel") && name.toLowerCase().endsWith(".mat")) {
		    		SuperpixelImport imp = new SuperpixelImport(file.getPath() , drawable.getImage());
		    		SuperpixelDecomposition dec = new SuperpixelDecomposition(imp.getSuperpixels(),imp.getPixelMap());
		    		SuperpixelDrawable super_drawable = new SuperpixelDrawable(dec, file.getName(),drawable);
		    		
		    		items.add(super_drawable);
		    	}
		    	
		    	if (name.toLowerCase().startsWith("tree") && name.toLowerCase().endsWith(".ser")) {
		    		
		    		String num_str = name.substring(4, name.length()-4);
		    		
		    		SuperpixelImport imp = new SuperpixelImport(file.getParent() + "/superpixel"+num_str+".mat" , drawable.getImage());
		    		SuperpixelDecomposition dec = new SuperpixelDecomposition(imp.getSuperpixels(),imp.getPixelMap());
		    		SuperpixelDrawable super_drawable = new SuperpixelDrawable(dec, file.getName(),drawable);
		    		
		    		KRVDecomposition krv_dec = new KRVDecomposition(file.getPath(), dec, drawable);
		    		
		    		items.add(krv_dec);
		    		
		    	}
		    	
		    	//TODO: Add more decompositions (KRV , Region growing)
		    }
		    
		    items.add(new NoDecomposition(drawable));
		    
			list.setItems(items);
			list.setOnMouseClicked(new DecompositionSelectedHandler());
			list.setEditable(false);
			b.setLeft(list);
		    
			Button button = new Button("Save Image");
			button.setOnAction(new SaveMarkableHandler(drawable));
			b.setBottom(button);
			
            inner.setCenter(drawable);
			new_tab.setContent(b);
		
			pane.getTabs().add(pane.getTabs().size()-2, new_tab);
			pane.getSelectionModel().select(new_tab);
		}
	}
}
