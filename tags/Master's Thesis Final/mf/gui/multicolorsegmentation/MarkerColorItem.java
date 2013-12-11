package mf.gui.multicolorsegmentation;

import javafx.scene.paint.Color;

public class MarkerColorItem {

	private Color c;
	private String name; 
	
	public MarkerColorItem(String name, Color c) {
		this.name = name;
		this.c = c;
	}
	
	public Color getValue() {
		return c;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
}
