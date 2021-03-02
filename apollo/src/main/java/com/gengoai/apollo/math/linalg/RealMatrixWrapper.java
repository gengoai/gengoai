package com.gengoai.apollo.math.linalg;

import lombok.NonNull;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.Serializable;

/**
 * <p>Wraps an NDArray treating it as a RealMatrix for use in Apache Commons Math algorithms.</p>
 */
class RealMatrixWrapper extends AbstractRealMatrix implements Serializable {
   private static final long serialVersionUID = 1L;
   private final NumericNDArray array;

   /**
    * Instantiates a new RealMatrixWrapper.
    *
    * @param array the NDArray to wrap
    */
   public RealMatrixWrapper(@NonNull NumericNDArray array) {
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
