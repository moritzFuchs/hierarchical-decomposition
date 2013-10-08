package org.jgrapht.experimental.decomposition;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ThreadPoolExecutor;

import org.jgrapht.Graph;

public abstract class Decomposition<V extends Comparable<V>, E> extends Observable implements Observer{

	/**
	 * Graph we want to decompose
	 */
	protected Graph<V,E> originalGraph = null;
	/**
	 * The decomposition tree of {@link RSTDecomposition.originalGraph}
	 */
	protected DecompositionTree<V> decomposition;
	/**
	 * Number of {@link RSTDecompositionSubTreeGenerator} that are currently active.
	 */
	private Integer inProcess = 0;
	/**
	 * {@link ThreadPoolExecutor} for this decomposition 
	 */
	protected ThreadPoolExecutor executor;
	/**
	 * The {@link DecompositionTask} we are currently working on
	 */
	protected DecompositionTask<V,E> task;

	public Decomposition() {
		super();
	}

	public DecompositionTree<V> performDecomposition() {
		
		addTask(task);
		 
		while (inProcess > 0) {
			System.out.println(" In process " + inProcess);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Done");
	
		executor.shutdown();
		//wait for the executor to shut down
		while (!executor.isShutdown()) {
		}
		
		return decomposition;
	}

	public Graph<V,E> getOriginalGraph() {
		return originalGraph;
	}

	protected abstract DecompositionSubTreeGenerator<V,E> getDecompositionSubtreeGenerator(DecompositionTask<V,E> task);
	
	/**
	 * Enqueues a new {@link DecompositionTask} 
	 * 
	 * @param newtask : The new {@link DecompositionTask}
	 */
	public synchronized void addTask(DecompositionTask<V,E> newtask) {
		DecompositionSubTreeGenerator<V,E> generator = getDecompositionSubtreeGenerator(newtask);
		
		inProcess++;
		generator.addObserver(this);
		generator.appendSubTree();
	}

	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		inProcess--;
	}

}