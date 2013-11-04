package mf.gui.decomposition.rst;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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

import mf.gui.ButtonRow;
import mf.gui.Markable;
import mf.gui.Pixel;
import mf.gui.decomposition.Drawable;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;

public class RSTDecompositionDrawable extends Drawable{

	/**
	 * The underlying {@link SuperpixelDecomposition}
	 */
	private SuperpixelDecomposition superpixel_decomposition;

	/**
	 * The decomposition tree computed by the RST algorithm
	 */
	private DecompositionTree<Integer> rst_decomposition;

	/**
	 * Each Superpixel is part of a partition. This map points each Superpixel to its current partition represented by a tree vertex.
	 */
	private Map<Superpixel , TreeVertex<Integer>> current_tree_vertex;
	
	/**
	 * Height of the tree
	 */
	private Integer height;
	
	/**
	 * True => INF-edges in tree are skipped; False => INF-edges in tree are not skipped (skipping is meaningful in the context of the paper, however
	 * it yields unexpected - but correct - bahavior in the GUI)
	 */
	private static final Boolean COLLAPSE_INF_EDGES = false;
	
	public RSTDecompositionDrawable(String num, String path_to_krv_dec , SuperpixelDecomposition superpixel_decomposition , Markable m, ButtonRow buttonRow) {
		super("RST Decomposition " + num , m, buttonRow);
		
		this.superpixel_decomposition = superpixel_decomposition;
		try {
			FileInputStream fileIn;
			fileIn = new FileInputStream(path_to_krv_dec);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.rst_decomposition = (DecompositionTree<Integer>)in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		height = rst_decomposition.getHeight(COLLAPSE_INF_EDGES);		
		current_tree_vertex = new HashMap<Superpixel , TreeVertex<Integer>>();

		for (Superpixel sp : superpixel_decomposition.getSuperpixelMap().values()) {
			current_tree_vertex.put(sp, rst_decomposition.getRoot());
		}
	}

	@Override
	public void draw() {
		
	}
	
	/**
	 * Returns the decomposition tree
	 * 
	 * @return The decomposition tree
	 */
	public DecompositionTree<Integer> getDecompositionTree() {
		return rst_decomposition;
	}
	
	/**
	 * Saves given {@link TreeVertex} (marker) as segmentation point in the tree for all {@link Superpixel} below the given {@link TreeVertex} (marked).
	 * 
	 * @param marker : Point in the tree
	 * @param marked : Superpixels below this {@link TreeVertex} will be marked with the given marker
	 */
	private void mark_superpixel_below(TreeVertex<Integer> marker, TreeVertex<Integer> marked) {
		if (marked.getType() == TreeVertexType.LEAF) {
			current_tree_vertex.put(superpixel_decomposition.getSuperpixelById(marked.getVertex()), marker);
		} else {
			DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree = rst_decomposition.getGraph();
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(marked)) {
				mark_superpixel_below(marker , tree.getEdgeTarget(e));
			}
		}
	}
	
	/**
	 * Draws the current state of the segmentation given by {@link RSTDecompositionDrawable.current_vertex_tree}.
	 */
	private void  drawCurrentSegmentation() {
		m.startLoading();
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
		m.stopLoading();
	}
	
	/**
	 * Shows the given level of the decomposition tree. 
	 * 
	 * @param level : The level we want to show
	 */
	public void showLevel(Integer level) {
		DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree = rst_decomposition.getGraph();
		TreeVertex<Integer> root = rst_decomposition.getRoot();
		
		decompose(tree, root, level);
		
		drawCurrentSegmentation();
	}
	
	/**
	 * Takes current state of decomposition and moves n level down. 
	 * 
	 * @param n : Number of levels to move down
	 */
	public void decomposeNLevel(Integer n) {
		
		Collection<TreeVertex<Integer>> state = new HashSet<TreeVertex<Integer>>();
		state.addAll(current_tree_vertex.values());
		
		for (TreeVertex<Integer> v : state) {
			decompose(rst_decomposition.getGraph() , v , n);
		}
		
		drawCurrentSegmentation();
	}
	
	/**
	 * Takes current state of decomposition and moves n level up.
	 * 
	 * @param n : Number of levels to move up
	 */
	public void composeNLevel(Integer n) {

		Collection<TreeVertex<Integer>> state = new HashSet<TreeVertex<Integer>>();
		state.addAll(current_tree_vertex.values());
		
		for (TreeVertex<Integer> v : state) {
			compose(rst_decomposition.getGraph() , v , n);
		}
		
		drawCurrentSegmentation();
	}
	
	/**
	 * Takes a {@link Superpixel}, gets the segmentation area it is part of and makes one more segmentation step on it.
	 * 
	 * @param sp : The {@link Superpixel}
	 */
	private void decompose(Superpixel sp) {
		TreeVertex<Integer> tree_vertex = current_tree_vertex.get(sp);
		decompose(rst_decomposition.getGraph(), tree_vertex, 1);
	}
	
	/**
	 * Takes a {@link Superpixel}, gets the segmentation area it is part of and reverses the last decomposition step on this area.
	 * 
	 * @param sp : The {@link Superpixel}.
	 */
	private void compose(Superpixel sp) {
		TreeVertex<Integer> tree_vertex = current_tree_vertex.get(sp);
		compose(rst_decomposition.getGraph(), tree_vertex , 1);
	}
	
	/**
	 * Recursively traverses the decomposition tree steps_left times. (Moves steps_left steps up in the decomposition tree)
	 * When the root element is reached, the algorithm stops
	 * 
	 * @param tree : The decomposition tree
	 * @param vertex : The current vertex
	 * @param steps_left : Number of steps to be taken.
	 */
	private void compose(DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree , TreeVertex<Integer> vertex , Integer steps_left) {
		
		if (vertex == rst_decomposition.getRoot()) {
			mark_superpixel_below(vertex, vertex);
		} else {
			DefaultWeightedEdge e = Iterables.get(tree.incomingEdgesOf(vertex) , 0);
			//There can only be 1 incoming edges since this is a tree!
			TreeVertex<Integer> prev = tree.getEdgeSource(e);
			
			if (COLLAPSE_INF_EDGES && tree.getEdgeWeight(e) == Double.POSITIVE_INFINITY) {
				compose(tree , prev, steps_left);
			} else {
				if (steps_left > 0) {
					compose(tree , prev, steps_left-1);
				} else {
					mark_superpixel_below(vertex, vertex);
				}
			}
		}
	}
	
	/**
	 * Recursively traverses the decomposition tree steps_left times. (Moves steps_left steps down in the decomposition tree)
	 * When a leaf node is reached the recursion stops.  
	 * 
	 * @param tree : The decomposition tree.
	 * @param vertex : The current vertex of the decomposition tree.
	 * @param steps_left : Number of times we still want to descend.
	 */
	private void decompose(DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree , TreeVertex<Integer> vertex , Integer steps_left) {
		
		if (vertex.getType() == TreeVertexType.LEAF) {
			mark_superpixel_below(vertex, vertex);
		} else {
			
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(vertex)) {
				TreeVertex<Integer> target = tree.getEdgeTarget(e);
				
				if (COLLAPSE_INF_EDGES && tree.getEdgeWeight(e) == Double.POSITIVE_INFINITY) {
					decompose(tree, target, steps_left);
				} else {
					if (steps_left > 0) {
						decompose(tree, target, steps_left-1);
					} else {
						mark_superpixel_below(vertex, vertex);
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
	
	/**
	 * Adds buttons to the {@link ButtonRow}; Puts Root node of the tree as current partition for all superpixels.
	 */
	@Override
	public void onActivate() {
		m.clear();
		draw();
		
		MenuButton mb = new MenuButton("Show Level");
		
		for (int i=0;i<=height;i++) {
			MenuItem item = new MenuItem("" + i);
			item.setOnAction(new LevelJumpHandler(this , i));
			mb.getItems().add(item);
		}
	
		this.buttonRow.addButton(mb);
		
		Button up = new Button("Level up");
		up.setOnAction(new LevelChangeHandler(-1,this));
		Button down = new Button("Level down");
		down.setOnAction(new LevelChangeHandler(1,this));
		this.buttonRow.addButton(up);
		this.buttonRow.addButton(down);
		
		//Level up is current problematic since root might be reached instantly!
		up.setDisable(true);
		
		for (Superpixel sp : superpixel_decomposition.getSuperpixelMap().values()) {
			current_tree_vertex.put(sp, rst_decomposition.getRoot());
		}
	}
}
