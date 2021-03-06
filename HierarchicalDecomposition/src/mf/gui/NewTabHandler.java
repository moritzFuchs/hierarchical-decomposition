package mf.gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import name.antonsmirnov.javafx.dialog.Dialog;
import mf.gui.decomposition.NoDecomposition;
import mf.gui.decomposition.rst.RSTDecompositionDrawableFactory;
import mf.gui.multicolorsegmentation.MultiColorSegmentationDrawableFactory;
import mf.gui.segmentation.InteractiveSegmentationDrawableFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
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
	 * A Tab consists of a {@link BorderPane} with a {@link ListView} on the left position and a {@link MarkableImageView} on the center position. 
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
		
			BorderPane outer = new BorderPane();
			
			ListView<Drawable> list = new ListView<Drawable>();
			ObservableList<Drawable> items = FXCollections.observableArrayList ();

		    BorderPane inner = new BorderPane();
		    outer.setCenter(inner);
		    
			MarkableImageView drawable = new MarkableImageView(img_path);
		    
			//Create Button row
            List<ButtonBase> defaultButtons = new LinkedList<ButtonBase>();
            
            Button button = new Button("Save Image");
			button.setOnAction(new SaveMarkableHandler(drawable));
            
			defaultButtons.add(button);
            ButtonRow buttonRow = new ButtonRow(defaultButtons);
            
		    inner.setBottom(buttonRow);
			
		    //Generate all possible Drawables and put them onto the list
		    DrawableGenerator gen = new DrawableGenerator(dir, items, buttonRow, drawable);
		    gen.addFactory(new RSTDecompositionDrawableFactory());
		    gen.addFactory(new InteractiveSegmentationDrawableFactory());
		    gen.addFactory(new MultiColorSegmentationDrawableFactory());
		    gen.generate();

			list.setItems(items);
			list.setOnMouseClicked(new DecompositionSelectedHandler());
			list.setEditable(false);
			outer.setLeft(list);

			items.add(new NoDecomposition(drawable, buttonRow));
			
            inner.setCenter(drawable);
            
			new_tab.setContent(outer);
		
			pane.getTabs().add(pane.getTabs().size()-2, new_tab);
			pane.getSelectionModel().select(new_tab);
		}
	}
}
