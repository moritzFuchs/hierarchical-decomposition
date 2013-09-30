package mf.gui.decomposition;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class LevelChangeHandler implements EventHandler<ActionEvent> {

	private Integer change;
	private KRVDecomposition dec;
	
	public LevelChangeHandler(Integer change , KRVDecomposition dec) {
		this.change = change;
		this.dec = dec;
	}
	
	@Override
	public void handle(ActionEvent event) {
		if (change < 0) {
			dec.composeNLevel(-change);
		} else {
			dec.decomposeNLevel(change);
		}
	}

}
