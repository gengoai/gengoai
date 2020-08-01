package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.DenseMatrix;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.RealMatrixWrapper;
import org.jblas.Decompose;
import org.jblas.FloatMatrix;

import static com.gengoai.apollo.math.linalg.NDArrayFactory.ND;

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
   protected NDArray[] onMatrix(NDArray m) {
      Validation.checkArgument(m.shape().isSquare(), "Only square matrices are supported");
      if(m instanceof DenseMatrix) {
         Decompose.LUDecomposition<FloatMatrix> r = Decompose.lu(m.toFloatMatrix()[0]);
         return new NDArray[]{new DenseMatrix(r.l),
               new DenseMatrix(r.u),
               new DenseMatrix(r.p)};
      }
      org.apache.commons.math3.linear.LUDecomposition luDecomposition =
            new org.apache.commons.math3.linear.LUDecomposition(new RealMatrixWrapper(m));
      return new NDArray[]{
            ND.array(luDecomposition.getL().getData()),
            ND.array(luDecomposition.getU().getData()),
            ND.array(luDecomposition.getP().getData()),
      };
   }

}// END OF LUDecomposition
