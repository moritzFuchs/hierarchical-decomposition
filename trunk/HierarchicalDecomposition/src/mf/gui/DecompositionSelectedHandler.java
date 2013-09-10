package mf.gui;

import mf.gui.decomposition.Drawable;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class DecompositionSelectedHandler implements EventHandler<MouseEvent> {

	
	
	@Override
	public void handle(MouseEvent event) {
		
		ListView<Drawable >view = (ListView<Drawable>) event.getSource();
		
		view.getSelectionModel().getSelectedItem().activate();
	}

}
