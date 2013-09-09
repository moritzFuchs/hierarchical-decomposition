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
import matlabcontrol.MatlabProxyFactoryOptions;


/**
 * Runs the superpixel-script from www.cs.sfu.ca/~mori/research/superpixels/ and generates graphs from the superpixels (one for each granularity)
 * Arguments:
 * 1) MATLAB directory with superpixel script (you might have to set it up before - see superpixel-README for that)
 * 2) Directory with the image file. Has to contain "image.jpg". CAUTION: preexisting files might be overwritten! 
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public class SuperpixelRunner {

	/**
	 * Hide the Matlab window?
	 */
	private static final Boolean HIDDEN = false;
	
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, TransformerConfigurationException, IOException, SAXException {
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
		.setHidden(HIDDEN)
        .build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		
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
				
				SuperpixelGraph graph = new SuperpixelGraph(pixels);
				graph.exportGraph(new File(args[1] +"/"+ new_name));
			}
		}
	}

}
