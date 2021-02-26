package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.nd3.dense.DenseFloat32NDArray;
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
   protected NDArray<Float>[] onMatrix(NDArray<? extends Number> input) {
      Decompose.QRDecomposition<FloatMatrix> r = Decompose.qr(input.toFloatMatrix()[0]);
      return new DenseFloat32NDArray[]{
            new DenseFloat32NDArray(r.q),
            new DenseFloat32NDArray(r.r)
      };
   }

}// END OF QRDecomposition
