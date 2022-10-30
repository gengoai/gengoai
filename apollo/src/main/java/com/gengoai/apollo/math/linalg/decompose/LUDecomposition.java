package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import org.jblas.Decompose;
import org.jblas.FloatMatrix;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * <p>Performs <a href="https://en.wikipedia.org/wiki/LU_decomposition">LU Decomposition</a> on the
 * given input NDArray. The returned array is in order {L, U, P}</p>
 *
 * @author David B. Bracewell
 */
public class LUDecomposition extends Decomposition {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Lu decomposition.
    */
   public LUDecomposition() {
      super(3);
   }

   @Override
   protected NumericNDArray[] onMatrix(NumericNDArray m) {
      Validation.checkArgument(m.shape().isSquare(), "Only square matrices are supported");
      if (m.isDense()) {
         Decompose.LUDecomposition<FloatMatrix> r = Decompose.lu(m.toFloatMatrix()[0]);
         return arrayOf(
               nd.DFLOAT32.array(r.l),
               nd.DFLOAT32.array(r.u),
               nd.DFLOAT32.array(r.p)
         );
      }
      org.apache.commons.math3.linear.LUDecomposition luDecomposition =
            new org.apache.commons.math3.linear.LUDecomposition(m.asRealMatrix());
      return arrayOf(
            nd.DFLOAT32.array(luDecomposition.getL().getData()),
            nd.DFLOAT32.array(luDecomposition.getU().getData()),
            nd.DFLOAT32.array(luDecomposition.getP().getData())
      );
   }

}// END OF LUDecomposition
