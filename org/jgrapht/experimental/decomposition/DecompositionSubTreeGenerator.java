package org.jgrapht.experimental.decomposition;

import java.util.Observable;
import java.util.concurrent.ExecutorService;

/**
 * Abstract class for a SubTreeGenerator. Gets a DecompositionTask containing a Graph G, a subgraph G[S] and a vertex in the decomposition tree.
 * All extending classes are supposed to take G[S] and decompose it using any algorithm. They are then supposed to add a new vertex to the decomposition
 * tree with the vertex from the task as parent.
 * 
 * This abstract class also contains:
 *  * The {@link DecompositionTree}
 *  * An {@link ExecutorService} to allow multithreaded applications. (Just throw in an ExecutorService with max. 1 Thread if you do not want multithreading)
 *  * The {@link RSTDecompositionDrawable} which handles new tasks created within extensions of this class
 *  
 * The purpose of this class is to get a common interface for all hierarchical decompositions.
 * 
 * @author moritzfuchs
 * @date 07.10.2013
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public abstract class DecompositionSubTreeGenerator<V extends Comparable<V>, E> extends Observable{

	protected DecompositionTask<V, E> task;
	protected ExecutorService exec;
	protected DecompositionTree<V> tree;
	protected Decomposition<V, E> decomposition;

	public DecompositionSubTreeGenerator(DecompositionTree<V> tree , DecompositionTask<V,E> task , ExecutorService exec , Decomposition<V, E> d) {
		this.task = task;
		this.exec = exec;
		this.tree = tree;
		this.decomposition = d;
	}
	
	/**
	 * Start the given decomposition task, append the resulting subtrees to the given tree and notify all observers once we are done 
	 */
	public abstract void appendSubTree();

}