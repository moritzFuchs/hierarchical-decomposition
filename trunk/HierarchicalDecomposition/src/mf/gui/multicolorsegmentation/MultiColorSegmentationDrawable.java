package mf.gui.multicolorsegmentation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.experimental.clustering.TreeVertexType;
import org.jgrapht.experimental.decomposition.DecompositionTree;
import org.jgrapht.graph.DefaultWeightedEdge;

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
 * @date 18.11.2013
 */
public class MultiColorSegmentationDrawable extends Drawable{

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
	private Map<Color, Set<Superpixel>> marker;
	
	/**
	 * Marker color of superpixels
	 */
	private Map<Superpixel , Color> superpixelColors;
	
	/**
	 * 'Done counter': counts number of children that have finished their DP computation 
	 */
	private Map<TreeVertex<Integer> , Integer> done;
	
	/**
	 * Segmentation of the image; Image is segmented by color.
	 */
	private Map<Color , Set<Superpixel>> coloring;
	
	/**
	 * Flag that indicates whether segmentation should be highlighted or not
	 */
	private Boolean highlight = true;

	/**
	 * Button for toggeling {@link highlight}
	 */
	private Button highlightButton;
	
	/**
	 * Current marker color
	 */
	private Color markerColor = new Color(1.0,0.0,0.0,1.0);
	
	
	public MultiColorSegmentationDrawable(String num_str, Markable m,
			ButtonRow buttonRow, String path, SuperpixelDecomposition dec) {
		super("Multi-Segmentation " + num_str, m, buttonRow);
		
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
		
		this.marker = new HashMap<Color, Set<Superpixel>>();
		this.superpixelColors = new HashMap<Superpixel, Color>();
		this.done = new HashMap<TreeVertex<Integer> , Integer>();
		this.coloring = null;
	}

	/**
	 * Takes a set of markers of different colors and finds a segmentation s.t. all markers of different color are separated.
	 * Markers of equal color are NOT neccessarily in the same segment.
	 */
	public void draw() {
		
		if (marker.isEmpty())
			return;
		
		//Get used colors
		Collection<Color> used = marker.keySet();
		Collection<Color> remove = new HashSet<Color>();
		
		for (Color c : used) {
			if (marker.get(c).isEmpty()) {
				remove.add(c);
			}
		}
		used.removeAll(remove);
		
		coloring = new HashMap<Color, Set<Superpixel>>();
		for (Color c : used) {
			coloring.put(c , new HashSet<Superpixel>());
		}
		
		Set<TreeVertex<Integer>> leaves = new HashSet<TreeVertex<Integer>>();
		
		Map<TreeVertex<Integer>, TreeVertexColor> dptable = new HashMap<TreeVertex<Integer>,TreeVertexColor>();
		//create datastructure for TreeVertex colors.
		setup(leaves, dptable);
		
		Queue<TreeVertex<Integer>> q = new LinkedList<TreeVertex<Integer>>();
		DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree = t.getGraph();
		
		Set<TreeVertex<Integer>> contains = new HashSet<TreeVertex<Integer>>();
		for (TreeVertex<Integer> l : leaves) {
			addParentsToQueue(q, tree, l, contains);
		}
		
		//DP phase
		while (!q.isEmpty()) {
			//Get children
			TreeVertex<Integer> v = q.poll();
			
			if (done.get(v) < tree.outgoingEdgesOf(v).size()) {
				//If done.get(v) < |outgoing edges| then there are still children of this edge that have not finished. 
				q.add(v);
			} else {
				TreeVertexColor vc = dptable.get(v);
				
				Set<TreeVertex<Integer>> children = new HashSet<TreeVertex<Integer>>();
				for (DefaultWeightedEdge e : tree.outgoingEdgesOf(v)) {
					children.add(tree.getEdgeTarget(e));
				}
			
				for(Color c : used) {
					Double cost = getColorCost(v, c,children, tree, dptable);
					vc.setCost(c, cost);
				}
				
				//get parent and add him to the queue
				addParentsToQueue(q,tree,v,contains);
			}
		}
		//All DP values have been computed, now we need to decide which colors to choose.
		
		mark(tree, t.getRoot(), dptable, coloring);
		
		//Coloring was decided, let's display it
		displaySegmentation();
	}

	/**
	 * Displays the current segmentation on the canvas. 
	 */
	private void displaySegmentation() {
		m.clear();
		
		for(Superpixel sp : superpixelColors.keySet()) {
			markSuperpixel(sp, superpixelColors.get(sp));
		}
		
		for (Color c : marker.keySet()) {
			Set<Superpixel> superpixels = coloring.get(c);
			if (superpixels == null) {
				continue;
			}
			for (Superpixel sp : superpixels) {
				for (Superpixel neighbor : sp.getNeighbors()) {
					if (!superpixels.contains(neighbor)) {
						for (Pixel p : sp.getBoundaryPixels(neighbor)) {
							markPixel(p, c);
						}
					}
				}
			}
		}
	}

	/**
	 * Mark a pixel with a given color
	 * 
	 * @param p
	 * @param c
	 */
	private void markPixel(Pixel p, Color c) {
		m.markPixel(p.getX(), p.getY(), c);
	}
	
	/**
	 * Picks the 'cheapest' color for a given vertex and colors the subtree below the vertex with it. Cuts edges as computed in the DP phase
	 * 
	 * @param tree
	 * @param vertex
	 * @param dptable
	 * @param coloring
	 */
	private void mark(
			DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree,
			TreeVertex<Integer> vertex,
			Map<TreeVertex<Integer>, TreeVertexColor> dptable,
			Map<Color, Set<Superpixel>> coloring) {	
		
		TreeVertexColor tc = dptable.get(vertex);
		Color min = tc.getMinColor();
		Set<DefaultWeightedEdge> cutEdges = tc.getCutEdges(min);
		if (vertex.getType() == TreeVertexType.LEAF) {
			
			coloring.get(min).add(dec.getSuperpixelById(vertex.getVertex()));
			
		} else {
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(vertex)) {
				if (cutEdges != null && cutEdges.contains(e)) {
					mark(tree, tree.getEdgeTarget(e),dptable,coloring);
				} else {
					markWithColor(min, tree,tree.getEdgeTarget(e),dptable,coloring);
				}
			}
		}
	}
	
	/**
	 * Marks the subtree below 'vertex' with the given color and cuts edges as computed in the DP phase.
	 * 
	 * @param c
	 * @param tree
	 * @param vertex
	 * @param dptable
	 * @param coloring
	 */
	private void markWithColor(
			Color c,
			DirectedGraph<TreeVertex<Integer> , DefaultWeightedEdge> tree,
			TreeVertex<Integer> vertex,
			Map<TreeVertex<Integer> , TreeVertexColor> dptable,
			Map<Color, Set<Superpixel>> coloring
			) {

		TreeVertexColor tc = dptable.get(vertex);
		Set<DefaultWeightedEdge> cutEdges = tc.getCutEdges(c);
		
		if (vertex.getType() == TreeVertexType.LEAF) {
			coloring.get(c).add(dec.getSuperpixelById(vertex.getVertex()));
		} else {
			for (DefaultWeightedEdge e : tree.outgoingEdgesOf(vertex)) {
				if (cutEdges != null && cutEdges.contains(e)) {
					mark(tree, tree.getEdgeTarget(e), dptable,coloring);
				} else {
					markWithColor(c, tree, tree.getEdgeTarget(e), dptable,coloring);
				}
			}
		}

	}

	/**
	 * Computes cost of coloring the vertex v with color c.
	 * 
	 * @param v
	 * @param c
	 * @param children
	 * @param tree
	 * @param dptable
	 * @return : Cost of coloring the vertex v with color c
	 */
	private Double getColorCost(
			TreeVertex<Integer> v, 
			Color c, 
			Set<TreeVertex<Integer>> children,
			DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree, 
			Map<TreeVertex<Integer>, TreeVertexColor> dptable) {
				
		Double cost = 0.0;
		for (DefaultWeightedEdge e : tree.outgoingEdgesOf(v) ) {
			TreeVertex<Integer> child = tree.getEdgeTarget(e);
			TreeVertexColor tc = dptable.get(child);
			
			Double edgeWeight = tree.getEdgeWeight(e);
			Double coloringCost = tc.getCost(c);
			
			//Rather pay the same price without cutting an edge..
			if (coloringCost <= edgeWeight) {
				cost += coloringCost;
			} else {
				cost += edgeWeight;
				//Remember that we need to cut e if we choose color c for vertex v
				dptable.get(v).addCutEdge(c, e);
			}
		}
		
		return cost;
	}
	
	/**
	 * Adds all parents of a given vertex l to the queue.
	 * 
	 * @param q
	 * @param tree
	 * @param l
	 */
	private void addParentsToQueue(Queue<TreeVertex<Integer>> q,
			DirectedGraph<TreeVertex<Integer>, DefaultWeightedEdge> tree,
			TreeVertex<Integer> l,
			Set<TreeVertex<Integer>> contains) {
		for (DefaultWeightedEdge e : tree.incomingEdgesOf(l)) {
			TreeVertex<Integer> parent = tree.getEdgeSource(e);
			if(!contains.contains(parent)){
				q.add(parent);
				contains.add(parent);
			}
			done.put(parent, done.get(parent)+1);
		}
	}

	/**
	 * Setup datastructure; Create {@link TreeVertexColor} for all {@link TreeVertex}s and set costs for leaves
	 * 
	 * @param leaves : Leaves of the {@link DecompositionTree}
	 * @param dptable : Map from {@link TreeVertex} to {@link TreeVertexColor} that is setup during this method
	 */
	private void setup(Set<TreeVertex<Integer>> leaves,
			Map<TreeVertex<Integer>, TreeVertexColor> dptable) {
		for (TreeVertex<Integer> vertex : t.getGraph().vertexSet()) {
			done.put(vertex , 0);
			TreeVertexColor tc = new TreeVertexColor(vertex);
			dptable.put(vertex, tc);
			
			if (vertex.getType() == TreeVertexType.LEAF) {
				leaves.add(vertex);
				Superpixel sp = dec.getSuperpixelById(vertex.getVertex());
				Color c = superpixelColors.get(sp);
				
				if (c == null) {
					
					//If no color is set for this superpixel, set cost to 0 for every color	
					for(Color n : marker.keySet()) {
						tc.setCost(n, 0.0);
					}
					//Reset min color; All colors are optimal
					tc.setMinColor(null);
					
				} else {
					
					//If a color is set for this superpixel, set costs for the set color to 0 and INF for all other colors
					for(Color n : marker.keySet()) {
						if (n != c) {
							tc.setCost(n, Double.POSITIVE_INFINITY);
						} else {
							tc.setCost(n, 0.0);
						}
					}
				}
			}
		}
	}
	

	@Override
	public void onActivate() {
		
		ChoiceBox<MarkerColorItem> cb = new ChoiceBox<MarkerColorItem>();
		MarkerColorItem red = new MarkerColorItem("Red" , new Color(1.0,0.0,0.0,1.0));
		MarkerColorItem green = new MarkerColorItem("Green" , new Color(0.0,1.0,0.0,1.0));
		MarkerColorItem blue = new MarkerColorItem("Blue" , new Color(0.0,0.0,1.0,1.0));
		cb.getItems().addAll(red,green,blue);
		cb.getSelectionModel().selectFirst();
		
		cb.getSelectionModel().selectedIndexProperty().addListener(new ColorPickerListener(cb, this));
		
		Button done = new Button("Segment!");
		Button reset = new Button("Reset");
		highlightButton = new Button("Hightlight ON");
		
		done.setOnAction(new DoneButtonHandler(this));
		reset.setOnAction(new ResetButtonHandler(this));
		highlightButton.setOnAction(new HighlightButtonHandler(this));
		
		buttonRow.addButton(done);
		buttonRow.addButton(reset);
		buttonRow.addButton(cb);
		buttonRow.addButton(highlightButton);
	}
	
	/**
	 * Updates the button text depending on the current state of {@link highlight}
	 */
	public void updateButtons() {
		
		if (isHighlighted()) {
			highlightButton.setText("Hightlight OFF");
		} else {
			highlightButton.setText("Hightlight ON");
		}
	}
	
	/**
	 * Turns hightlight of segmentation on
	 */
	public void setHighlight(Boolean b) {
		highlight = b;
	}
	
	/**
	 * True if hightlight is turned on, false otherwise
	 * 
	 * @return True if highlight is turned on; false otherwise
	 */
	public Boolean isHighlighted() {
		return highlight;
	}
	
	/**
	 * Changes the marker color
	 * 
	 * @param c : New marker color
	 */
	public void setMarkerColor(Color c) {
		this.markerColor = c;
	}
	
	/**
	 * Resets the markers (and unmarks all currently marked superpixels)
	 */
	public void reset() {
		marker.clear();
		superpixelColors.clear();
		done.clear();
		coloring = null;
		
		m.clear();
	}
	
	/**
	 * Adds a marker to the image
	 * 
	 * @param p
	 */
	public void addMarker(Pixel p) {
		
		Superpixel sp = dec.getSuperpixelByPixel(p);
		
		Set<Superpixel> s = marker.get(markerColor);
		if (s == null) {
			s = new HashSet<Superpixel>();
			marker.put(markerColor, s);
		}
		
		s.add(sp);
		
		//Get old color and remove superpixel from other set. (prevent superpixel with multiple marker colors!)
		Color c = superpixelColors.get(sp);
		if (c != null) {
			Set<Superpixel> set = marker.get(c);
			set.remove(sp);
		}
		
		superpixelColors.put(sp, markerColor);
		
		markSuperpixel(sp, markerColor);
	}
	
	/**
	 * Marks a {@link Superpixel} on the image
	 * 
	 * @param sp
	 */
	private void markSuperpixel(Superpixel sp, Color c) {
		for (Pixel p : sp.getPixel()) {
			m.markPixel(p.getX(), p.getY(),c);
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
		
		Color c = superpixelColors.get(sp);
		Set<Superpixel> s = marker.get(c);
		if (s != null) {
			s.remove(sp);
			superpixelColors.remove(sp);
		}
		
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
				setHighlight(false);
				if (coloring != null)
					displaySegmentation();
				addMarker(new Pixel(x.intValue(),y.intValue()));		
			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				removeMarker(new Pixel(x.intValue(),y.intValue()));
			}
		} else {
			if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
				if (coloring != null) {
					displaySegmentation();
				}
			}
			
			if (highlight && coloring != null ) {
				MouseEvent mouse_event = (MouseEvent) event;
				Double x =  mouse_event.getX();
				Double y = mouse_event.getY();
				
				Pixel p = new Pixel(x.intValue(), y.intValue());
				Superpixel sp = dec.getSuperpixelByPixel(p);
				
				for (Color c : coloring.keySet()) {
					if (coloring.get(c).contains(sp)) {
						showArea(coloring.get(c),c);
					}
				}
			}
		}
	}
	
	/**
	 * Marks an area in the appropriate color
	 * 
	 * @param area
	 * @param color
	 */
	private void showArea(Set<Superpixel> area, Color color) {
		m.clear();
		
		//Use the PixelWriter and a buffer to show the segments efficiently
		
		PixelWriter writer = m.getPixelWriter();
		WritablePixelFormat<IntBuffer> format 
        = WritablePixelFormat.getIntArgbInstance();
		
		int buffer[] = new int[m.getImageWidth() * m.getImageHeight()];
		
		int fillColor = (150 << 24) 
                + ((int)(color.getRed()*255) << 16) 
                + ((int)(color.getGreen()*255) << 8) 
                + ((int)(color.getBlue()*255));
		
		int noColor = (0 << 24) 
                + (0 << 16) 
                + (0 << 8) 
                + 0;
		
		Arrays.fill(buffer , noColor);
		
		for(Superpixel sp : superpixelColors.keySet()) {
			markSuperpixel(sp, superpixelColors.get(sp));
		}
		
		for (Color c : marker.keySet()) {
			Set<Superpixel> superpixels = coloring.get(c);
			if (superpixels == null)
				continue;
			for (Superpixel sp : superpixels) {
				if (superpixels == area) {
					for (Pixel p : sp.getPixel()) {
						buffer[p.getY() * m.getImageWidth() + p.getX()] = fillColor;
						//markPixel(p,c);
					}
				} 
			}
		}
		
		writer.setPixels(0, 0, 
                m.getImageWidth(), m.getImageHeight(), 
                format, buffer, 0, m.getImageWidth());
	}
}
