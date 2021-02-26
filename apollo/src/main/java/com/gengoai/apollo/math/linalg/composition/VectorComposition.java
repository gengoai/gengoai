package com.gengoai.apollo.math.linalg.composition;

import com.gengoai.apollo.math.linalg.NDArray;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>Defines a method of combining multiple vectors into one</p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface VectorComposition<In, Out> extends Serializable {

   /**
    * Compose the given vectors with given k into a single vector.
    *
    * @param vectors the vectors to compose
    * @return the composed vector
    */
   NDArray<Out> compose(@NonNull List<NDArray<In>> vectors);


}// END OF VectorComposition
