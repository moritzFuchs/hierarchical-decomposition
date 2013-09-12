package mf.krvrunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.jgrapht.experimental.decomposition.Decomposition;
import org.jgrapht.experimental.decomposition.DecompositionTree;
import org.jgrapht.graph.DefaultWeightedEdge;

import javafx.scene.image.Image;
import mf.superpixel.SuperpixelDecomposition;
import mf.superpixel.SuperpixelGraph;
import mf.superpixel.SuperpixelImport;

//TODO: Think of better name than 'KRV-Decomposition'

/**
 * Takes the {@link Superpixel-Graph} and computes a KRV-Decomposition.
 * 
 * @author moritzfuchs
 *
 */
public class KrvRunner {

	public static void main(String[] args) {
		
		String base_path = args[0];
		String path_to_image = args[0] + "/image.jpg";
		String path_to_superpixel = args[0] + "/superpixel1000.mat";
		
		SuperpixelImport importer = new SuperpixelImport(path_to_superpixel , new Image("file:" + path_to_image));
		SuperpixelDecomposition superpixel_dec = new SuperpixelDecomposition(importer.getSuperpixels(), importer.getPixelMap());
		
		SuperpixelGraph graph = new SuperpixelGraph(superpixel_dec);
		
		System.out.println(graph.getGraph().vertexSet().size());
		System.out.println(graph.getGraph().edgeSet().size());
		
		Decomposition<Integer , DefaultWeightedEdge> graph_dec = new Decomposition<Integer , DefaultWeightedEdge>(graph.getGraph());
		DecompositionTree<Integer> tree = graph_dec.performDecomposition();
		
		System.out.println(tree.getGraph().vertexSet().size());
		System.out.println(tree.getGraph().edgeSet().size());
				
		try {
			FileOutputStream fileOut =
			new FileOutputStream(base_path + "/tree1000.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(tree);
			
			out.close();
			fileOut.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

		
		
		
		
	}
	
}
