package mf.gui.decomposition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.experimental.clustering.TreeVertexType;
import org.jgrapht.experimental.decomposition.DecompositionTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.Iterables;

import mf.gui.Markable;
import mf.gui.Pixel;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;

public class KRVDecomposition extends Drawable{

	private SuperpixelDecomposition superpixel_decomposition;
	private DecompositionTree<Integer> krv_decomposition;
//	private TreeVertex<Integer> current_tree_vertex;
	private Map<Superpixel , TreeVertex<Integer>> current_tree_vertex;
	
	public KRVDecomposition(String path_to_krv_dec , SuperpixelDecomposition superpixel_decomposition , Markable m) {
		super("KRV Decomposition" , m);
		
		this.superpixel_decomposition = superpixel_decomposition;
		try {
			FileInputStream fileIn;
			fileIn = new FileInputStream(path_to_krv_dec);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.krv_decomposition = (DecompositionTree<Integer>)in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		current_tree_vertex = new HashMap<Superpixel , TreeVertex<Integer>>();
		
		for (Superpixel sp : superpixel_decomposition.getSuperpixelMap().values()) {
			current_tree_vertex.put(sp, krv_decomposition.getRoot());
		}
		
	}

	@Override
	public void draw() {
		
	}
	
	/**
	 * Takes a {@link Superpixel}, gets the segmentation area it is part of and makes one more segmentation step on it.
	 * 
	 * @param sp
	 */
	private void decompose(Superpixel sp) {
		TreeVertex<Integer> tree_vertex = current_tree_vertex.get(sp);
		
		for (DefaultWeightedEdge e : krv_decomposition.getGraph().outgoingEdgesOf(tree_vertex)) {
			TreeVertex<Integer> next = krv_decomposition.getGraph().getEdgeTarget(e);
			mark_superpixel_below(next, next);
		}
	}
	
	/**
	 * Takes a {@link Superpixel}, gets the segmentation area it is part of and reverses the last decomposition step on this area.
	 * 
	 * @param sp : The {@link Superpixel}.
	 */
	private void compose(Superpixel sp) {
		TreeVertex<Integer> tree_vertex = current_tree_vertex.get(sp);
		
		for (DefaultWeightedEdge e : krv_decomposition.getGraph().incomingEdgesOf(tree_vertex)) {
			TreeVertex<Integer> parent = krv_decomposition.getGraph().getEdgeSource(e);
			mark_superpixel_below(parent, parent);
		}
	}
	
	/**
	 * Saves given {@link TreeVertex} (marker) as segmentation point in the tree fow all {@link Superpixel} below the given {@link TreeVertex} (marked).
	 * 
	 * @param marker : Point in the tree
	 * @param marked : Superpixels below this {@link TreeVertex} will be marked with the given marker
	 */
	private void mark_superpixel_below(TreeVertex<Integer> marker, TreeVertex<Integer> marked) {
		if (marked.getType() == TreeVertexType.LEAF) {
			current_tree_vertex.put(superpixel_decomposition.getSuperpixelById(marked.getVertex()), marker);
		} else {
			DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree = krv_decomposition.getGraph();
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(marked)) {
				mark_superpixel_below(marker , tree.getEdgeTarget(e));
			}
		}
	}
	
	/**
	 * Draws the current state of the segmentation given by {@link KRVDecomposition.current_vertex_tree}.
	 */
	private void  drawCurrentSegmentation() {
		m.clear();
		for (Superpixel sp : current_tree_vertex.keySet()) {
			for (Superpixel neighbor : sp.getNeighbors()) {
				if (current_tree_vertex.get(sp) != current_tree_vertex.get(neighbor)) {
					for (Pixel p : sp.getBoundaryPixels(neighbor)) {
						m.markPixelComplementary(p.getX(), p.getY());
					}
				}
			}
		}
	}
	
	@Override
	public void handleMouseEvent(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			MouseEvent mouse_event = (MouseEvent) event; 
			Double x =  mouse_event.getX();
			Double y = mouse_event.getY();
			if (mouse_event.getButton().equals(MouseButton.PRIMARY)) {
				Superpixel sp = superpixel_decomposition.getSuperpixelByPixel(new Pixel(x.intValue(),y.intValue()));				
				decompose(sp);
			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				Superpixel sp = superpixel_decomposition.getSuperpixelByPixel(new Pixel(x.intValue(),y.intValue()));
				compose(sp);
			}
			
			drawCurrentSegmentation();
		}
		
		event.consume();
	}
	
	@Override
	public void onActivate() {
		draw();
	}
}
