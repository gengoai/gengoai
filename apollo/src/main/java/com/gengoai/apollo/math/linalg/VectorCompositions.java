package com.gengoai.apollo.math.linalg;

import com.gengoai.apollo.math.linalg.decompose.SingularValueDecomposition;
import lombok.NonNull;

import java.util.Collection;

/**
 * <p>Common vector compositions</p>
 *
 * @author David B. Bracewell
 */
public enum VectorCompositions implements VectorComposition {
   /**
    * Averages the elements of the vectors
    */
   Average {
      @Override
      public NDArray compose(@NonNull Collection<NDArray> vectors) {
         if(vectors.size() == 0) {
            return NDArrayFactory.ND.empty();
         }
         return Sum.compose(vectors).divi(vectors.size());
      }
   },
   /**
    * Sums the elements of the vectors
    */
   Sum {
      @Override
      public NDArray compose(Collection<NDArray> vectors) {
         if(vectors.size() == 0) {
            return NDArrayFactory.ND.empty();
         }
         NDArray toReturn = null;
         for(NDArray v : vectors) {
            if(toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.addi(v);
            }
         }
         return toReturn;
      }
   },
   /**
    * Performs a point-wise multiply of the elements
    */
   PointWiseMultiply {
      @Override
      public NDArray compose(Collection<NDArray> vectors) {
         if(vectors.size() == 0) {
            return NDArrayFactory.ND.empty();
         }
         NDArray toReturn = null;
         for(NDArray v : vectors) {
            if(toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mul(v);
            }
         }
         return toReturn;
      }
   },
   /**
    * Assigns the maximum element
    */
   Max {
      @Override
      public NDArray compose(Collection<NDArray> vectors) {
         if(vectors.size() == 0) {
            return NDArrayFactory.ND.empty();
         }
         NDArray toReturn = null;
         for(NDArray v : vectors) {
            if(toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mapi(v, Math::max);
            }
         }
         return toReturn;
      }
   },
   /**
    * Assigns the minimum element
    */
   Min {
      @Override
      public NDArray compose(Collection<NDArray> vectors) {
         if(vectors.size() == 0) {
            return NDArrayFactory.ND.empty();
         }
         NDArray toReturn = null;
         for(NDArray v : vectors) {
            if(toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mapi(v, Math::min);
            }
         }
         return toReturn;
      }
   },
   /**
    * Concatenates the vectors together and runs svd over them.
    */
   SVD {
      private final SingularValueDecomposition svd = new SingularValueDecomposition(1);

      @Override
      public NDArray compose(Collection<NDArray> vectors) {
         if(vectors.size() == 0) {
            return NDArrayFactory.ND.empty();
         }
         return svd.decompose(NDArrayFactory.DENSE.vstack(vectors))[2].getRow(0);
      }
   }

}//END OF VectorCompositions
