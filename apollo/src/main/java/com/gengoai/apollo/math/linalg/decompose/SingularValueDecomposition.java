package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.math.linalg.SparkLinearAlgebra;
import com.gengoai.apollo.math.linalg.nd;
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
   protected NumericNDArray[] onMatrix(NumericNDArray input) {
      if (distributed) {
         return SparkLinearAlgebra.svd(input,
                                       K <= 0
                                             ? input.shape().columns()
                                             : K);
      }

      FloatMatrix[] usvT;
      if (sparse) {
         usvT = Singular.sparseSVD(input.toFloatMatrix()[0]);
      } else {
         usvT = Singular.fullSVD(input.toFloatMatrix()[0]);
      }

      var u = nd.DFLOAT32.array(usvT[0]);
      var s = nd.DFLOAT32.array(FloatMatrix.diag(usvT[1]));
      var vT = nd.DFLOAT32.array(usvT[2]);

      if (K > 0) {
         u = u.get(u.shape().with(Shape.COLUMN, K).range()).reshape(Shape.shape(u.rows(),K));
         s = s.get(s.shape().with(Shape.ROW,K, Shape.COLUMN, K).range()).reshape(Shape.shape(K,K));
         vT = vT.get(vT.shape().with(Shape.ROW, K).range()).reshape(Shape.shape(K, vT.columns()));
      }

      return new NumericNDArray[]{u,s,vT};
   }
}// END OF SingularValueDecomposition
