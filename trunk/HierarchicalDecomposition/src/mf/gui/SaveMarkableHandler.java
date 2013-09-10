package mf.gui;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

public class SaveMarkableHandler implements EventHandler<ActionEvent> {

	private Markable m;
	
	public SaveMarkableHandler(Markable m) {
		this.m = m;
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
			
			m.export(file);
		}
	}

}
