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
					graph.setEdgeWeight(e, p.getEdgeWeight(neighbor));
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