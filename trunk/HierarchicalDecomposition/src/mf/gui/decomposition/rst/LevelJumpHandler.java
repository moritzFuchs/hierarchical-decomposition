package mf.gui.decomposition.rst;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

public class LevelJumpHandler implements EventHandler<ActionEvent>{
		
		private RSTDecomposition dec;
		private Integer level;
		
		public LevelJumpHandler(RSTDecomposition dec , Integer level) {
			this.dec = dec;
			this.level = level;
		}
		
		@Override
		public void handle(ActionEvent value) {
			dec.showLevel(level);
		}
}
