package mf.gui;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;

import mf.superpixel.SuperpixelGraph;
import mf.superpixel.SuperpixelImport;

import org.xml.sax.SAXException;

import javafx.scene.image.Image;

public class Test {

	public static void main(String[] args) throws IOException, TransformerConfigurationException, SAXException {
		
		Double x = Double.valueOf("1.6000005E7");
		x += 1;
		System.out.println(x);
		
	}

}
