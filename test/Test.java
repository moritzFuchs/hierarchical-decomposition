package org.jgrapht.experimental.clustering.test;


import org.jgrapht.experimental.clustering.Util;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

/**
 * Random tests..
 * @author moritzfuchs
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SparseDoubleMatrix2D d = new SparseDoubleMatrix2D(4, 4);
		d.setQuick(1, 2, 3);
		d.setQuick(2, 1, 3);
		d.setQuick(3, 3, 3);
		
		DoubleMatrix1D v = new DenseDoubleMatrix1D(4);
		v.setQuick(0, 1);
		v.setQuick(1, 2);
		v.setQuick(2, 3);
		v.setQuick(3, 4);
		
		
		v =  d.zMult(v.copy(), v);
		
		System.out.println(v);

	}

}
