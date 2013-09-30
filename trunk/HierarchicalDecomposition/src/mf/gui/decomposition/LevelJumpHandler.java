package mf.gui.decomposition;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

public class LevelJumpHandler implements EventHandler<ActionEvent>{
		
		private KRVDecomposition dec;
		private Integer level;
		
		public LevelJumpHandler(KRVDecomposition dec , Integer level) {
			this.dec = dec;
			this.level = level;
		}
		
		@Override
		public void handle(ActionEvent value) {
			dec.showLevel(level);
		}
}
