package com.gengoai.apollo.math.linalg;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.Serializable;

/**
 * Wraps an NDArray treating it as a RealMatrix for use in Apache Commons Math algorithms.
 *
 * @author David B. Bracewell
 */
public class RealMatrixWrapper extends AbstractRealMatrix implements Serializable {
   private static final long serialVersionUID = 1L;
   private final NDArray<? extends Number> array;

   /**
    * Instantiates a new Real matrix wrapper.
    *
    * @param array the array
    */
   public RealMatrixWrapper(NDArray<? extends Number> array) {
      this.array = array;
   }

   @Override
   public RealMatrix copy() {
      return new RealMatrixWrapper(array.copy());
   }

   @Override
   public RealMatrix createMatrix(int i, int i1) throws NotStrictlyPositiveException {
      return new RealMatrixWrapper(array.factory().zeros(i, i1));
   }

   @Override
   public int getColumnDimension() {
      return array.shape().columns();
   }

   @Override
   public double getEntry(int i, int i1) throws OutOfRangeException {
      return array.getDouble(i, i1);
   }

   @Override
   public int getRowDimension() {
      return array.shape().rows();
   }

   @Override
   public void setEntry(int i, int i1, double v) throws OutOfRangeException {
      array.set(i, i1, v);
   }
}// END OF RealMatrixWrapper
