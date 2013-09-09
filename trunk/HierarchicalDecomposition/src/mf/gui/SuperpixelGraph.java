package mf.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.xml.sax.SAXException;

import mf.gui.decomposition.Drawable;
import mf.gui.decomposition.SuperpixelDecomposition;

public class SuperpixelGraph extends Drawable{

	private WeightedGraph<Integer , DefaultWeightedEdge> graph;
	private final static Double EPSILON = 0.000001;
	private final static Double LAMBDA = 0.3;
	private Map<Integer , Superpixel> superpixel_map;
	
	public SuperpixelGraph(Map<Integer , Superpixel> superpixel_map , String name , Markable m) {
		super(name , m);
		
		this.superpixel_map = superpixel_map;
		
		graph = new SimpleWeightedGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		for (Superpixel p : superpixel_map.values()) {
			graph.addVertex(p.getId());
		}
		
		for (Superpixel p : superpixel_map.values()) {
			Integer source = p.getId();
			for (Superpixel neighbor : p.getNeighbors()) {
				Integer target = neighbor.getId();
				if (graph.getEdge(source, target) == null) {
					DefaultWeightedEdge e = graph.addEdge(source, target);
					graph.setEdgeWeight(e, getEdgeWeight(p , neighbor));
				}
			}
		}
	} 
	
	/**
	 * Returns the underlying map from Integer to {@link Superpixel}. 
	 * 
	 * @return Map<Integer, Superpixel> : The underlying map from Integer to {@link Superpixel}.
	 */
	public Map<Integer, Superpixel> getSuperpixelMap() {
		return superpixel_map;
	}
	
	/**
	 * Returns the edge weight between two {@link Superpixel}.
	 * 
	 * @param source : The source {@link Superpixel}
	 * @param target : The target {@link Superpixel}
	 * @return Double : The distance between source and target.
	 */
	private Double getEdgeWeight(Superpixel source , Superpixel target) {
		
		Double weight = 0.0;
		
		Set<Pixel> boundary = source.getBoundaryPixels(target);
		Double[] rgb_source = source.getMeanRGB();
		Double[] rgb_target = target.getMeanRGB();
		
		Double rgb_distance = RGBDistance(rgb_source, rgb_target);
		
		weight += boundary.size() / (rgb_distance + EPSILON);
		weight += LAMBDA * boundary.size();
		
		return weight;
	}
	
	/**
	 * Computes the l2-distance between two RGB-vectors.
	 * 
	 * @param rgb1 : First RGB-vector
	 * @param rgb2 : Second RGB-vector
	 * @return Double : The l2-distance of the RGB-vectors
	 */
	private Double RGBDistance(Double[] rgb1 , Double[] rgb2) {
		
		Double distance = 0.0;
		distance += Math.pow(rgb1[0] - rgb2[0], 2);
		distance += Math.pow(rgb1[1] - rgb2[1], 2);
		distance += Math.pow(rgb1[2] - rgb2[2], 2);
		
		distance = Math.sqrt(distance);
		
		return distance;
	}
	
	/**
	 * Exports the generated graph into the given file. Format : DOT
	 * 
	 * @param file : The export file 
	 * @throws IOException
	 * @throws SAXException 
	 * @throws TransformerConfigurationException 
	 */
	public void exportGraph(File file) throws IOException, TransformerConfigurationException, SAXException {
		GraphMLExporter<Integer , DefaultWeightedEdge> exporter = new GraphMLExporter<Integer , DefaultWeightedEdge>();
		Writer writer = new FileWriter(file);
		exporter.export(writer, graph);
	}

	@Override
	public void draw() {
		
		SuperpixelDecomposition dec = new SuperpixelDecomposition(superpixel_map , "",m);
		dec.draw();
		
		//TODO: Draw graph over Superpixel Decomposition!
		
	}
}