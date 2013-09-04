package org.jgrapht.experimental.clustering;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public abstract class PartitionMatrix {

	/**
	 * Type of the partition Matrix (1 = deletion or 2 = matching)
	 */
	protected Integer type = -1;
	
	private SparseDoubleMatrix2D matrix;
	
	protected PartitionMatrix(SparseDoubleMatrix2D matrix , Integer type) {
		this.matrix = matrix;
		this.type = type;
	}
	
	public SparseDoubleMatrix2D getMatrix() {
		return matrix;
	}
	
	public Boolean isDeletion() {
		return type == 1;
	}
	
	public Boolean isMatching() {
		return type == 2;
	}
	
}
