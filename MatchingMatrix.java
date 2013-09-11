package org.jgrapht.experimental.clustering;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class MatchingMatrix extends PartitionMatrix {

	public MatchingMatrix(SparseDoubleMatrix2D matrix) {
		super(matrix, 2);
	}	
}
