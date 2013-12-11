package mf.gui.segmentation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.experimental.decomposition.DecompositionTree;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.Iterables;

import mf.gui.ButtonRow;
import mf.gui.Drawable;
import mf.gui.Markable;
import mf.gui.Pixel;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;


/**
 * Implements the interactive image segmentation. The user puts a set of markers onto the image and the algorithm tries to find maximal regions s.t. all markers
 * are separate.
 * 
 * @author moritzfuchs
 * @date 30.10.2013
 */
public class InteractiveSegmentationDrawable extends Drawable{

	/**
	 * The {@link SuperpixelDecomposition}
	 */
	private SuperpixelDecomposition dec;
	
	/**
	 * The {@link DecompositionTree} we use to segment the image
	 */
	private DecompositionTree<Integer> t;
	
	/**
	 * The current set of markers on the image
	 */
	private Set<Superpixel> marker;
	
	public InteractiveSegmentationDrawable(String num_str, Markable m,
			ButtonRow buttonRow, String path, SuperpixelDecomposition dec) {
		super("Single Color Segmentation (" + num_str + ")", m, buttonRow);
		
		try {
			FileInputStream fileIn;
			fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.t = (DecompositionTree<Integer>)in.readObject();
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			return;
		}
		
		this.dec = dec;
		
		this.marker = new HashSet<Superpixel>();
	}

	//FIXME: Every now and then markers that were removed stay in marker set but are no longer displayed. 
	//This screws up up the whole segmentation. Find out why this happens
	public void draw() {
		
		//If less than two markers have been set, the segmentation is trivial (none)
		if (marker.size() < 2) {
			return;
		}
		
		Set<Set<Superpixel>> segmentation = new HashSet<Set<Superpixel>>();
		
		DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> g = t.getGraph();
		
		//Foreach marker: walk up the tree as long as leaves below current node do not contain another marker
		for (Superpixel sp : marker) {
			Integer id = sp.getId();
			TreeVertex<Integer> leaf = t.getLeaf(id);
			TreeVertex<Integer> vertex = leaf;
			//Walk up the tree
			while (vertex != t.getRoot()) {
				Set<DefaultWeightedEdge> inEdges = g.incomingEdgesOf(vertex);
				DefaultWeightedEdge e = Iterables.get(inEdges, 0);
				TreeVertex<Integer> parent = g.getEdgeSource(e);
				Set<Integer> leaves = t.getAll(parent);
				
				//Check if leaves below contain other markers apart from sp
				Integer count = 0;
				for (Superpixel sp2 : marker) {
					if (leaves.contains(sp2.getId())) {
						count++;
					}
					//If we found >= 2 markers we know that we can stop
					if (count >= 2) {
						break;
					}
				}
				
				//Add current segment if parent contains more than 2 markers
				if (count >= 2) {
					Set<Superpixel> newSet = new HashSet<Superpixel>();
					for (Integer i : t.getAll(vertex)) {
						newSet.add(dec.getSuperpixelById(i));
					}
					
					segmentation.add(newSet);
					break;
				}
				
				vertex = parent;
			}
		}
		
		//Draw segmentation
		
		//For each cluster of superpixels...
		for (Set<Superpixel> s : segmentation) {
			// ... take each superpixel ...
			for (Superpixel sp : s) {
				// ... and check if any of their neighbours is in a different cluster ...
				for (Superpixel neighbor : sp.getNeighbors()) {
					if (!s.contains(neighbor)) {
						// ... If so, highlight the border to that neighbour.
						for (Pixel p : sp.getBoundaryPixels(neighbor)) {
							m.markPixel(p.getX(), p.getY(), new Color(1.0, 0.0, 0.0, 1.0));
						}
					}
				}
			}
		}
	}

	@Override
	public void onActivate() {
		Button done = new Button("Segment!");
		Button reset = new Button("Reset");
		
		done.setOnAction(new DoneButtonHandler(this));
		reset.setOnAction(new ResetButtonHandler(this));
		
		buttonRow.addButton(done);
		buttonRow.addButton(reset);
	}
	
	/**
	 * Resets the markers (and unmarks all currently marked superpixels)
	 */
	public void reset() {
		marker.clear();
		m.clear();
	}
	
	/**
	 * Adds a marker to the image
	 * 
	 * @param p
	 */
	public void addMarker(Pixel p) {
		Superpixel sp = dec.getSuperpixelByPixel(p);
		marker.add(sp);
		
		markSuperpixel(sp);
	}
	
	/**
	 * Marks a {@link Superpixel} on the image
	 * 
	 * @param sp
	 */
	private void markSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getPixel()) {
			m.markPixel(p.getX(), p.getY(),new Color(1.0,0.0,0.0,1.0));
		}
	}
	
	/**
	 * 'unmarks' a {@link Superpixel}
	 * 
	 * @param sp
	 */
	private void unmarkSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getPixel()) {
			m.clearPixel(p.getX(),p.getY());
		}
	}
	
	/**
	 * Removes a marker from the set of markers and unmarks the superpixel on the image
	 * 
	 * @param p
	 */
	public void removeMarker(Pixel p) {
		Superpixel sp = dec.getSuperpixelByPixel(p);
		marker.remove(sp);
		unmarkSuperpixel(sp);
	}

	/**
	 * Handles the mouse event: left click --> add marker; right click --> remove marker
	 * 
	 * @param event: The mouse event
	 */
	@Override
	public void handleMouseEvent(MouseEvent event) {
		
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			MouseEvent mouse_event = (MouseEvent) event; 
			Double x =  mouse_event.getX();
			Double y = mouse_event.getY();
			if (mouse_event.getButton().equals(MouseButton.PRIMARY)) {
				addMarker(new Pixel(x.intValue(),y.intValue()));		
			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				removeMarker(new Pixel(x.intValue(),y.intValue()));
			}
		}
	}
}
