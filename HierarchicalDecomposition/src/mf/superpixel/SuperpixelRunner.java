package mf.superpixel;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import javafx.scene.image.Image;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import mf.gui.Superpixel;
import mf.gui.SuperpixelGraph;
import mf.gui.SuperpixelImport;

public class SuperpixelRunner {

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, TransformerConfigurationException, IOException, SAXException {
		MatlabProxyFactory factory = new MatlabProxyFactory();
		MatlabProxy proxy = factory.getProxy();
		
		proxy.eval("cd '" + args[0] + "'");
		proxy.eval("addpath('superpixels64')");
		proxy.eval("export_superpixel('" + args[1] + "')");
		proxy.exit();
		
		File dir = new File(args[1]);
		for (File file : dir.listFiles()) {
			String new_name;
			if (file.getName().startsWith("superpixel") && file.getName().endsWith(".mat")) {
				new_name = file.getName();
				new_name = new_name.substring(0, new_name.length()-4);
				new_name = new_name + ".gml";
			
				System.out.println("Generating graph for " + file.getName());
				
				SuperpixelImport importer = new SuperpixelImport(args[1] +"/"+ file.getName() , new Image("file:" + args[1] + "/image.jpg"));
				Map<Integer , Superpixel> pixels = importer.getSuperpixels();
				
				SuperpixelGraph graph = new SuperpixelGraph(pixels,"",null);
				graph.exportGraph(new File(args[1] +"/"+ new_name));
			}
		}
	}

}
