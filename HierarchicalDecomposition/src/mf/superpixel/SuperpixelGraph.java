package mf.superpixel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.xml.sax.SAXException;

import mf.gui.Pixel;

/**
 * A graph on a given superpixel decomposition.
 *  * Each {@link Superpixel} is represented by a vertex in the graph
 *  * 2 vertices are connected if the boundaries of the corresponding {@link Superpixel} are touching.
 *  * An edge has the following edge weight: Let I_u be the mean RGB-color in Superpixel u and L_uv the length of the boundary between the Superpixels.
 *    Then the edge weight of the edge u--v equals L_uv/||I_u-I_v||_2 + LAMBDA * L_uv.
 *    The edge weight is chosen, s.t. touching superpixels with similar color have huge weight. 
 * 
 * @author moritzfuchs
 * @date 09.09.2013
 *
 */
public class SuperpixelGraph{

	/**
	 * The generated graph
	 */
	private WeightedGraph<Integer , DefaultWeightedEdge> graph;
	
	/**
	 * Constant for the edge weight computation: ||I_u - I_v|| might be 0, therefore we add EPSILON to it.
	 */
	private final static Double EPSILON = 0.000001;
	
	/**
	 * Factor by which the boundary length is factored in.
	 */
	private final static Double LAMBDA = 0.3;
	
	/**
	 * Map from {@link Integer} to {@link Superpixel} with the corresponding ID
	 */
	private Map<Integer , Superpixel> superpixel_map;
	
	public SuperpixelGraph(SuperpixelDecomposition dec) {
		this.superpixel_map = dec.getSuperpixelMap();
		
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
		
		//Make the weights integral
		weight = Math.ceil(weight);
		
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
	
	public WeightedGraph<Integer , DefaultWeightedEdge> getGraph() {
		return graph;
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
		
		VertexNameProvider<Integer> vertexIDProvider = 
			new VertexNameProvider<Integer>() { 
	            @Override 
	            public String getVertexName(Integer vertex) { 
	                return vertex.toString(); 
	            } 
			}; 

		VertexNameProvider<Integer> vertexNameProvider = 
			new VertexNameProvider<Integer>() { 
	            @Override 
	            public String getVertexName(Integer vertex) { 
	                return vertex.toString(); 
	            } 
			};
			
		EdgeNameProvider<DefaultWeightedEdge> edgeIDProvider = 
			new EdgeNameProvider<DefaultWeightedEdge>() { 
				@Override 
		    	public String getEdgeName(DefaultWeightedEdge edge) { 
					return graph.getEdgeSource(edge) + " > " + graph.getEdgeTarget(edge); 
				} 
		    }; 

		EdgeNameProvider<DefaultWeightedEdge> edgeLabelProvider = 
			new EdgeNameProvider<DefaultWeightedEdge>() { 
				@Override 
		    	public String getEdgeName(DefaultWeightedEdge edge) { 
					Double weight = graph.getEdgeWeight(edge);
					System.out.println(edge + " " + weight);
					return weight.toString(); 
		    	} 
		    }; 
		
		GraphMLExporter<Integer , DefaultWeightedEdge> exporter = 
			new GraphMLExporter<Integer , DefaultWeightedEdge>(vertexIDProvider,vertexNameProvider,edgeIDProvider,edgeLabelProvider);
		Writer writer = new FileWriter(file);
		exporter.export(writer, graph);
	}
}