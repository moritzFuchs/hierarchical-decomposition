package mf.gui;

import java.io.File;
import java.io.IOException;

import name.antonsmirnov.javafx.dialog.Dialog;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

/**
 * Handles the save-button events. 
 * 
 * @author moritzfuchs
 * @date 10.09.2013
 *
 */
public class SaveMarkableHandler implements EventHandler<ActionEvent> {

	/**
	 * The {@link Markable} this {@link SaveMarkableHandler} can save. 
	 */
	private Markable m;
	
	public SaveMarkableHandler(Markable m) {
		this.m = m;
	}
	
	/**
	 * Button was clicked => Ask the user where to save the image and do it.
	 * 
	 * @param event : The {@link ActionEvent} of the click.
	 */
	@Override
	public void handle(ActionEvent event) {
		
		Button source = (Button)event.getSource();
		System.out.println(source.getText());
		if (source.getText().compareTo("Save Image") == 0) {
			
			FileChooser chooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
			chooser.getExtensionFilters().add(extFilter);
			File file = chooser.showSaveDialog(null);
			try {
				m.export(file);
			} catch(IOException e) {
				Dialog.showThrowable("Could not export image.", "Sorry, I could not export the Image." , e ); 
			}
			
		}
	}

}
