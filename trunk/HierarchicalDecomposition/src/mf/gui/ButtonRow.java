package mf.gui;

import java.util.HashSet;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;

public class ButtonRow extends HBox{

	/**
	 * Buttons that are there by default.
	 */
	private Iterable<ButtonBase> defaultButtons;
	
	/**
	 * Buttons that have been added manually.
	 */
	private Set<ButtonBase> addedButtons;
	
	public ButtonRow(Iterable<Button> defaultButtons){
        addedButtons = new HashSet<ButtonBase>();
        
		setPadding(new Insets(15, 12, 15, 12));
        setSpacing(10);
        setStyle("-fx-background-color: #336699;");
        
        for (Button b : defaultButtons) {
        	getChildren().add(b);
        }
	}
	
	/**
	 * Adds a {@link Button} to the ButtonRow. 
	 * 
	 * @param button : The added button
	 */
	public void addButton(ButtonBase button) {
		addedButtons.add(button);
		getChildren().add(button);
	}
	
	/**
	 * Removes all added buttons; Default Buttons stay.
	 */
	public void reset() {
		getChildren().removeAll(addedButtons);
	}
}
