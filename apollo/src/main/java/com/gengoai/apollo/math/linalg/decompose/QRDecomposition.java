package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.DenseMatrix;
import com.gengoai.apollo.math.linalg.NDArray;
import org.jblas.Decompose;
import org.jblas.FloatMatrix;

/**
 * <p>Performs <a href="https://en.wikipedia.org/wiki/QR_decomposition">QR Decomposition</a> on the given input
 * NDArray. The returned array is in order {Q,R}</p>
 *
 * @author David B. Bracewell
 */
public class QRDecomposition extends Decomposition {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Qr decomposition.
    */
   public QRDecomposition() {
      super(2);
   }

   @Override
   protected NDArray[] onMatrix(NDArray m) {
      Decompose.QRDecomposition<FloatMatrix> r = Decompose.qr(m.toFloatMatrix()[0]);
      return new NDArray[]{new DenseMatrix(r.q), new DenseMatrix(r.r)};
   }

}// END OF QRDecomposition
