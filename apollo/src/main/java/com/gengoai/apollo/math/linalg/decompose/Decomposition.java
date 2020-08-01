package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.Tensor;

import java.io.Serializable;

/**
 * <p>Encapsulates an algorithm to decompose (factorize) a given {@link NDArray} into a product of matrices.</p>
 *
 * @author David B. Bracewell
 */
public abstract class Decomposition implements Serializable {
   private static final long serialVersionUID = 1L;
   private final int components;

   /**
    * Instantiates a new Decomposition.
    *
    * @param components the components
    */
   protected Decomposition(int components) {
      this.components = components;
   }

   /**
    * Decompose the given input NDArray into a product of one or more other NDArrays
    *
    * @param input the input NDArray
    * @return Array of NDArray representing the factors of the product.
    */
   public final NDArray[] decompose(NDArray input) {
      if (input.shape().order() < 3) {
         return onMatrix(input);
      }
      NDArray[][] results = new NDArray[components][input.shape().sliceLength];
      for (int i = 0; i < input.shape().sliceLength; i++) {
         NDArray[] slice = onMatrix(input.slice(i));
         for (int j = 0; j < components; j++) {
            results[j][i] = slice[j];
         }
      }
      NDArray[] out = new NDArray[components];
      for (int j = 0; j < components; j++) {
         out[j] = new Tensor(input.kernels(), input.channels(), results[j]);
      }
      return out;
   }

   /**
    * Gets number of components.
    *
    * @return the number of components
    */
   public int getNumberOfComponents() {
      return components;
   }

   /**
    * Performs the operation on a single matrix
    *
    * @param matrix the matrix
    * @return the components of the decomposition
    */
   protected abstract NDArray[] onMatrix(NDArray matrix);

}//END OF Decomposition
