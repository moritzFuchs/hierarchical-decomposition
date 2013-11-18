package mf.gui.multisegmentation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;

public class ColorPickerListener implements ChangeListener {

	private ChoiceBox<MarkerColorItem> c;
	
	private MultiSegmentationDrawable drawable;
	
	public ColorPickerListener(ChoiceBox<MarkerColorItem> c , MultiSegmentationDrawable drawable) {
		this.c = c;
		this.drawable = drawable;
	}
	
	@Override
	public void changed(ObservableValue arg0, Object oldSelected, Object newSelected) {
		MarkerColorItem m = c.getItems().get(((Number) newSelected).intValue());
		Color color = m.getValue();
		
		drawable.setMarkerColor(color);
	}

}
