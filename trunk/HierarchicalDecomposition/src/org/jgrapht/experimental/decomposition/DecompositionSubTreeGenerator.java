package org.jgrapht.experimental.decomposition;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Connectivity;
import org.jgrapht.experimental.clustering.PartitionB;
import org.jgrapht.experimental.clustering.PartitionBTask;
import org.jgrapht.experimental.clustering.SubGraphGenerator;
import org.jgrapht.experimental.clustering.TreeVertex;
import org.jgrapht.experimental.clustering.krv.PartitionA;
import org.jgrapht.experimental.clustering.krv.PartitionATask;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Takes a {@link DecompositionTask} and runs the decomposition algorithm in {@link PartitionA} and {@link PartitionB}. 
 * After that it appends a {@link TreeVertex} to the tree for each cluster that it produced. This yields a 2-level-subtree.
 * On the top level is the root node which represents the whole graph that we want to decompose. On the second level there are
 * 1 or 2 nodes depending on the bisection produced by PartitionB. If only 1 node exists on level 1, the cluster produced by PartitionA
 * were 'good'.
 * The weights between the root and nodes on level 1 are as follows:
 *  * The weight between the root and the node representing the set L (the one near t) gets weight equal to the out degree of the induced cluster
 *  * The weight between the root and the node representing the set R (the one near s) gets infinite weight (as discussed with Prof. R�cke)
 * 
 * The nodes on the second level represent the clusters produced by PartitionA intersected with L / R. The weight between the second first level
 * is always infinite (as discussed with Prof. R�cke)
 * 
 * If the intersection of clusters of PartitionA and L / R produces any clusters of size 1 or 2 there will be a third level:
 *  * If a cluster has size 1 we cannot partition it anymore, therefore we connect it to the second level with weight equal to the out degree of the node
 *  * If a cluster has size 2 there is only one possible way to partition it, and we therefore do this right away. Both nodes in the cluster will receive a node
 *    on level 3 that is connected to the corresponding cluster on level 2 with weight equal to the out degree of each node.
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class DecompositionSubTreeGenerator<V extends Comparable<V>,E> extends Observable implements Observer{

	/**
	 * Local logger
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(DecompositionSubTreeGenerator.class
		      .getName());
	
	/**
	 * State flag for this generator. 0 = after creation; 1 = PartitionA is currently performed; 2 = PartitionB is currently performed; 3 = starting mergeResults; 4 = done.
	 */
	private Integer state = 0;
	
	/**
	 * The decomposition tree
	 */
	private DecompositionTree<V> tree;
	
	/**
	 * The decomposition task for this object
	 */
	private DecompositionTask<V,E> task;
	
	/**
	 * The Thread Pool that executes all Threads
	 */
	private ExecutorService exec;
	
	/**
	 * The PartitionA Object
	 */
	private PartitionA<V,E> partA;
	
	/**
	 * The PartitionB object
	 */
	private PartitionB<V,E> partB;

	/**
	 * The composition that created this class
	 */
	private Decomposition<V,E> decomposition;
	
	public DecompositionSubTreeGenerator(DecompositionTree<V> tree , DecompositionTask<V,E> task , ExecutorService exec , Decomposition<V,E> d) {		
		this.task = task;
		this.exec = exec;
		this.tree = tree;
		this.decomposition = d;
	}
	
	/**
	 * Start the given decomposition task, append the resulting subtrees to the given tree and notify all observers once we are done 
	 */
	public void appendSubTree() {
		performPartitionA();
	}
	
	/**
	 * Performs PartitionA (Clustering). This will come back to {@link DecompositionSubTreeGenerator.update}, which will then call {@link DecompositionSubTreeGenerator.performPartitionB}.
	 * Once that is finished it will call update again. After that the results are merged into the subtree
	 */
	private void performPartitionA () {
		
		PartitionATask<V,E> partitionTask = new PartitionATask<V,E>(task); 
		partA = new PartitionA<V,E>(partitionTask);

		partA.addObserver(this);
		
		state = 1;
		
		LOGGER.info("Starting PartitionA");

		exec.execute(partA);
	}
	
	/**
	 * Performs partitionB
	 * 
	 * @param F : precomputed clustering of the given subgraph by {@link PartitionA}.
	 */
	private void performPartitionB (Set<E> F) {
		PartitionBTask<V,E> partTask = new PartitionBTask<V, E>(task.getGraph() , task.getSubGraph(), F);
		partB = new PartitionB<V,E>(partTask);
		
		partB.addObserver(this);
		
		state = 2;
		
		LOGGER.info("Starting PartitionB");
		
		exec.execute(partB);
	}
	
	/**
	 * Takes results of PartitionA and PartitionB and adds tree vertices to the tree accordingly. 
	 * 
	 * @param graph : The complete graph G
	 * @param subgraph : The subgraph of G, G'
	 * @param partAclustering : The clustering computed by PartitionA
	 * @param partBclustering : The clustering computed by PartitionB
	 */
	private void mergeResults(Graph<V,E> graph, Graph<V,E> subgraph, Set<E> partAclustering, Set<E> partBclustering) {
		
		Set<V> L = partB.getL();
		
		Set<V> R = partB.getR();
		
		TreeVertex<V> parent = task.getParent();
		TreeVertex<V> left = null;
		TreeVertex<V> right = null;
		
		//Insert a node for R if R is not empty
		if (!R.isEmpty()) {
			right = tree.addVertex();
			tree.addEdge(parent, right, Double.POSITIVE_INFINITY);
		}
		
		//Insert a node for L if L is not empty
		if (!L.isEmpty()) {
			left = tree.addVertex();
			
			tree.addEdge(parent , left , Connectivity.<V,E>getOutDegree(task.getGraph() , L));
		}
		
		Set<Set<V>> left_clusters = new HashSet<Set<V>>();
		Set<Set<V>> right_clusters = new HashSet<Set<V>>();
		
		//Compute intersection of clusters in PartitionA with L / R 
		for (Graph<V, E> cluster : Connectivity.getClusters(task.getSubGraph(), partA.getClustering())) {
			Set<V> left_cluster_intersection = new HashSet<V>(cluster.vertexSet());
			left_cluster_intersection.removeAll(R);
			
			Set<V> new_left_cluster = new HashSet<V>();
			
			for (V v : left_cluster_intersection) {
				new_left_cluster.add(v);
			}
			if (!new_left_cluster.isEmpty())
				left_clusters.add(new_left_cluster);
			
			Set<V> right_cluster_intersection = new HashSet<V>(cluster.vertexSet());
			right_cluster_intersection.removeAll(L);
			
			Set<V> new_right_cluster = new HashSet<V>();
			
			for (V v : right_cluster_intersection) {
				new_right_cluster.add(v);
			}
			if (!new_right_cluster.isEmpty())
				right_clusters.add(new_right_cluster);
		}
		
		appendLevelTwo(L , left_clusters , left);
		appendLevelTwo(R , right_clusters , right);
		
		state = 4;
		
		setChanged();
		//Tell observers that this DecompositionTask is done
		notifyObservers();
	}
	
	/**
	 * Appends level 2 vertices to the decomposition tree; one vertex for each cluster. If a given cluster is small, we will partition it manually.
	 * 
	 * @param level1 : The level1 cluster we are currently working on
	 * @param clusters : The clustering of level1
	 * @param parent : The parent vertex in the decomposition tree which represents the cluster level1
	 */
	private void appendLevelTwo(Set<V> level1 , Set<Set<V>> clusters , TreeVertex<V> parent) {
		if (!level1.isEmpty()) {
			//For each cluster in R insert a new node below 'right' with weight infinity
			for (Set<V> cluster : clusters) {
				TreeVertex<V> new_vertex = tree.addVertex();
				tree.addEdge(parent, new_vertex , Double.POSITIVE_INFINITY);
				
				if (cluster.size() > 2) {
					DecompositionTask<V,E> new_task = new DecompositionTask<V,E>(task.getGraph() , SubGraphGenerator.<V,E>generateSubGraph(task.getGraph(), cluster) , new_vertex);
					decomposition.addTask(new_task);
				} else {
					specialCases(cluster , new_vertex);
				}
			}
		}
	}

	/**
	 * Deals with special cases where the graph is small enough to partition it manually:
	 *  * If |G| = 1, then there is nothing to partition and we just append the single vertex as leaf to the tree (edge weight = out degree of the vertex)
	 *  * If |G| = 2, then there is only one way to partition the graph, so 2 leaf vertices are appended to the tree, each with edge weight equal to the out degree of each vertex 
	 * 
	 * @param cluster : The special-case-cluster.
	 * @param parent : The parent in the decomposition tree. 
	 */
	private void specialCases(Set<V> cluster, TreeVertex<V> parent) {
		
		if (cluster.size() == 1) {
			V v = (V)cluster.toArray()[0];
			TreeVertex<V> leaf = tree.addVertex(v);
			
			Double weight = Connectivity.getOutDegree(task.getGraph() , (V)cluster.toArray()[0]);
			tree.addEdge(parent, leaf , weight);
			
			LOGGER.fine("Adding leaf: " + (V)cluster.toArray()[0] + " with edge weight " + weight);
		}
		
		if (cluster.size() == 2) {
			Double weight;
			TreeVertex<V> leaf;
			V v = (V)cluster.toArray()[0];
			
			leaf = tree.addVertex(v);
			weight = Connectivity.getOutDegree(task.getGraph() , v);
			tree.addEdge(parent, leaf, weight);
			
			LOGGER.fine("Adding leaf: " + v + " with edge weight " + weight);
			
			v = (V)cluster.toArray()[1];
			weight = Connectivity.getOutDegree(task.getGraph() , v);
			
			leaf = tree.addVertex(v);
			tree.addEdge(parent, leaf, weight);
			
			LOGGER.fine("Adding leaf: " + v + " with edge weight " + weight);
			
		}
	}

	/**
	 * Interface for Observed Objects. Called if PartitionA or PartitionB finishes (recognizable via {@link DecompositionSubTree.state})
	 * 
	 * @param partition : Either PartitionA or PartitionB
	 * @param arg1 : neglected, since all results are contained in the partition object
	 */
	@Override
	public void update(Observable partition, Object arg1) {
		
		LOGGER.info("Incoming update. State=" + state);
		
		partition.deleteObserver(this);
		
		//PartitionA is running => PartitionA is now done
		if (state == 1) {
			performPartitionB(partA.getClustering());
			return;
		}
		
		//Partition B is done
		if (state == 2) {
			
			Set<E> partAclustering = partA.getClustering();
			Set<E> partBclustering = partB.getClustering();
			
			state = 3;
			mergeResults(task.getGraph() , task.getSubGraph() , partAclustering , partBclustering);

			return;
		}
		
	}
}
