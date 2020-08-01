package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.DenseMatrix;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.SparkLinearAlgebra;
import org.jblas.FloatMatrix;
import org.jblas.Singular;

/**
 * <p>Performs <a href="https://en.wikipedia.org/wiki/Singular_value_decomposition">Singular Value Decomposition</a> on
 * the given input NDArray. The returned array is in order {U, S, V}</p>
 *
 * @author David B. Bracewell
 */
public class SingularValueDecomposition extends Decomposition {
   private static final long serialVersionUID = 1L;

   private final boolean distributed;
   private final boolean sparse;
   private final int K;

   /**
    * Instantiates a new Singular value decomposition with K=-1
    */
   public SingularValueDecomposition() {
      this(false, false, -1);
   }

   /**
    * Instantiates a new Singular value decomposition.
    *
    * @param K the number of components to truncate the SVD to
    */
   public SingularValueDecomposition(int K) {
      this(false, false, K);
   }

   /**
    * Instantiates a new Singular value decomposition.
    *
    * @param distributed True - run using Spark in distributed mode, False locally use JBlas
    * @param sparse      True - run using SparseSVD, False full SVD (only used when not distributed).
    */
   public SingularValueDecomposition(boolean distributed, boolean sparse) {
      this(distributed, sparse, -1);
   }

   /**
    * Instantiates a new Singular value decomposition.
    *
    * @param distributed True - run using Spark in distributed mode, False locally use JBlas
    * @param sparse      True - run using SparseSVD, False full SVD (only used when not distributed).
    * @param K           the number of components to truncate the SVD to
    */
   public SingularValueDecomposition(boolean distributed, boolean sparse, int K) {
      super(3);
      this.distributed = distributed;
      this.sparse = sparse;
      this.K = K;
   }

   @Override
   protected NDArray[] onMatrix(NDArray input) {
      if(distributed) {
         return SparkLinearAlgebra.svd(input,
                                       K <= 0
                                       ? input.columns()
                                       : K);
      }

      NDArray[] result;
      FloatMatrix[] r;
      if(sparse) {
         r = Singular.sparseSVD(input.toFloatMatrix()[0]);
      } else {
         r = Singular.fullSVD(input.toFloatMatrix()[0]);
      }
      result = new NDArray[]{
            new DenseMatrix(r[0]),
            new DenseMatrix(FloatMatrix.diag(r[1])),
            new DenseMatrix(r[2]),
      };

      if(K > 0) {
         result[0] = result[0].getSubMatrix(0, result[0].rows(), 0, K);
         result[1] = result[1].getSubMatrix(0, K, 0, K);
         result[2] = result[2].getSubMatrix(0, result[2].rows(), 0, K);
      }

      return result;
   }
}// END OF SingularValueDecomposition
