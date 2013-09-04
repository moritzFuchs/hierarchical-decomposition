package org.jgrapht.experimental.decomposition;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jgrapht.Graph;

//TODO: Document
public class Decomposition<V extends Comparable<V>,E> extends Observable implements Observer{

	/**
	 * Multiple of number of CPUs we want to use
	 */
	private static final int N = 2;

	/**
	 * Graph we want to decompose
	 */
	private Graph<V,E> originalGraph = null;
	
	/**
	 * The decomposition tree of {@link Decomposition.originalGraph}
	 */
	private DecompositionTree<V> decomposition;
		
	/**
	 * Number of {@link DecompositionSubTreeGenerator} that are currently active.
	 */
	private Integer inProcess = 0;
	
	/**
	 * {@link ThreadPoolExecutor} for this decomposition 
	 */
	private ThreadPoolExecutor executor;
	
	/**
	 * The {@link DecompositionTask} we are currently working on
	 */
	private DecompositionTask<V,E> task;
	
	private Set<DecompositionSubTreeGenerator<V,E>> debugDec = new HashSet<DecompositionSubTreeGenerator<V,E>>();
	
	public Decomposition(Graph<V,E> g) {
		this.originalGraph = g;
	
		this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * N);
		
		//Generate decomposition tree
		this.decomposition = new DecompositionTree<V>();
		
		task = new DecompositionTask<V,E>(g,g,decomposition.getRoot());

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
	
	/**
	 * Enqueues a new {@link DecompositionTask} 
	 * 
	 * @param newtask : The new {@link DecompositionTask}
	 */
	public synchronized void addTask(DecompositionTask<V,E> newtask) {
		
		DecompositionSubTreeGenerator<V,E> generator = new DecompositionSubTreeGenerator<V, E>(this.decomposition, newtask, executor, this);
		inProcess++;
		generator.addObserver(this);
		debugDec.add(generator);
		
		generator.appendSubTree();
	}

	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		debugDec.remove(arg0);
		inProcess--;
	}

}
