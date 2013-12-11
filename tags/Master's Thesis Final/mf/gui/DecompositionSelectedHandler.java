package mf.gui;

import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

/**
 * Handles the selection of {@link Drawable}s from a ListView.
 * 
 * @author moritzfuchs
 * @date 05.09.2013
 *
 */
public class DecompositionSelectedHandler implements EventHandler<MouseEvent> {

	/**
	 * Activates the selected {@link Drawable}
	 * 
	 * @param : The {@link MouseEvent} of clicking on a {@link Drawable}.
	 */
	@Override
	public void handle(MouseEvent event) {
		ListView<Drawable >view = (ListView<Drawable>) event.getSource();
		view.getSelectionModel().getSelectedItem().activate();
	}

}
