package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
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
   protected NumericNDArray[] onMatrix(NumericNDArray input) {
      Decompose.QRDecomposition<FloatMatrix> r = Decompose.qr(input.toFloatMatrix()[0]);
      return new NumericNDArray[]{nd.DFLOAT32.array(r.q), nd.DFLOAT32.array(r.r)};
   }

}// END OF QRDecomposition
