package mf.gui.decomposition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jgrapht.experimental.decomposition.DecompositionTree;

import mf.gui.Markable;
import mf.superpixel.SuperpixelDecomposition;

public class KRVDecomposition extends Drawable{

	private SuperpixelDecomposition superpixel_decomposition;
	private DecompositionTree<Integer> krv_decomposition;
	
	
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
	
	@Override
	public void onActivate() {
		draw();
	}
}
