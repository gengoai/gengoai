package com.gengoai.apollo.math.linalg.decompose;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.math.linalg.dense.DenseFloat32NDArray;
import com.gengoai.conversion.Cast;
import org.jblas.FloatMatrix;

import java.io.Serializable;
import java.lang.reflect.Array;

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
   public final NumericNDArray[] decompose(NumericNDArray input) {
      if (input.rank() < 3) {
         return onMatrix(input);
      }

      FloatMatrix[][] results = new FloatMatrix[components][input.shape().sliceLength()];
      for (int i = 0; i < input.shape().sliceLength(); i++) {
         NumericNDArray[] slice = onMatrix(input.slice(i));
         for (int j = 0; j < components; j++) {
            results[j][i] = slice[j].toFloatMatrix()[0];
         }
      }

      NumericNDArray[] out = Cast.as(Array.newInstance(DenseFloat32NDArray.class, components));
      for (int j = 0; j < components; j++) {
         out[j] = new DenseFloat32NDArray(input.shape().kernels(),
                                          input.shape().channels(),
                                          results[j]);
         for (int i = 0; i < results[j].length; i++) {
            out[j].setSlice(i, nd.DFLOAT32.array(results[j][i]));
         }
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
   protected abstract NumericNDArray[] onMatrix(NumericNDArray matrix);

}//END OF Decomposition
