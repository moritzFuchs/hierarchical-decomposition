package mf.gui.decomposition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Set;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.experimental.decomposition.DecompositionTree;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.Iterables;

import mf.gui.Markable;
import mf.gui.Pixel;
import mf.superpixel.Superpixel;
import mf.superpixel.SuperpixelDecomposition;

public class KRVDecomposition extends Drawable{

	private SuperpixelDecomposition superpixel_decomposition;
	private DecompositionTree<Integer> krv_decomposition;
	private TreeVertex<Integer> current_tree_vertex;
	
	public KRVDecomposition(String path_to_krv_dec , SuperpixelDecomposition superpixel_decomposition , Markable m) {
		super("KRV Decomposition" , m);
		
		this.superpixel_decomposition = superpixel_decomposition;
		try {
			FileInputStream fileIn;
			fileIn = new FileInputStream(path_to_krv_dec);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.krv_decomposition = (DecompositionTree<Integer>)in.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Integer a = 1;
				
		
		
	}

	@Override
	public void draw() {
		System.out.println("DRAWING!");
	}
	
	/**
	 * Draws a single {@link Superpixel} onto the drawable.
	 * 
	 * @param sp : The {@link Superpixel} we want to draw.
	 */
	private void drawSuperpixel(Superpixel sp) {
		for (Pixel p : sp.getBoundaryPixels()) {
			m.markPixel(p.getX(), p.getY());
		}
	}
	
	@Override
	public void handleMouseEvent(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			MouseEvent mouse_event = (MouseEvent) event; 
			if (mouse_event.getButton().equals(MouseButton.PRIMARY)) {
				m.clear();
				Double x =  mouse_event.getX();
				Double y = mouse_event.getY();

				Color c = m.getColor(x.intValue(),y.intValue());
				
				//Mark with 'complementary' color
				m.markPixel(x.intValue(), y.intValue(), new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue()+1, y.intValue(), new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue()-1, y.intValue(), new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue(), y.intValue()+1, new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				m.markPixel(x.intValue(), y.intValue()-1, new Color(1.0-c.getRed(), 1.0-c.getGreen(), 1.0-c.getBlue(), 1.0));
				Superpixel sp = superpixel_decomposition.getSuperpixelByPixel(new Pixel(x.intValue(),y.intValue()));
				drawSuperpixel(sp);
				
				current_tree_vertex = krv_decomposition.getLeaf(sp.getId());

			}
			if (mouse_event.getButton().equals(MouseButton.SECONDARY)) {
				draw();
			}
		}
		
		event.consume();
	}
	
	@Override
	public void handleScrollEvent(ScrollEvent event) {
		Set<DefaultWeightedEdge> incoming = krv_decomposition.getGraph().incomingEdgesOf(current_tree_vertex);
		System.out.println(incoming.size());
		
		if (incoming.size() > 0) {
			DefaultWeightedEdge e = Iterables.get(incoming, 0);
			
			current_tree_vertex =  krv_decomposition.getGraph().getEdgeSource(e);
			
			for (Integer id : krv_decomposition.getAll(current_tree_vertex)) {
				Superpixel sp = superpixel_decomposition.getSuperpixelById(id);
				drawSuperpixel(sp);
			}
		}
		
		
		System.out.println("scrolling..");
	}
	
	@Override
	public void onActivate() {
		draw();
	}
}
