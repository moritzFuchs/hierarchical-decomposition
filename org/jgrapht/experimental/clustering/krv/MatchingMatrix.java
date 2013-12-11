package org.jgrapht.experimental.clustering.krv;

import org.jgrapht.experimental.clustering.PartitionMatrix;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class MatchingMatrix extends PartitionMatrix {

	public MatchingMatrix(SparseDoubleMatrix2D matrix) {
		super(matrix, 2);
	}	
}
