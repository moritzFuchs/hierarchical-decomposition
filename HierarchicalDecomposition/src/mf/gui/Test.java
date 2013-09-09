package mf.gui;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import javafx.scene.image.Image;

public class Test {

	public static void main(String[] args) throws IOException, TransformerConfigurationException, SAXException {
		
		SuperpixelImport imp = new SuperpixelImport("Input/Fry/superpixel.mat" , new Image("file:Input/Fry/image.jpg"));
		SuperpixelGraph g = new SuperpixelGraph(imp.getSuperpixels() , "Test" , new DrawableImageView("Input/Fry/image.jpg"));
		g.exportGraph(new File("Input/Fry/superpixelGraph.dot"));

	}

}
