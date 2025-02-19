package com.gengoai.apollo.math.linalg;

import com.gengoai.Validation;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.stream.spark.SparkStream;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * <p>Convenience methods for working Spark's linear algebra structures and methods</p>
 *
 * @author David B. Bracewell
 */
public final class SparkLinearAlgebra {

   private SparkLinearAlgebra() {
      throw new IllegalAccessError();
   }

   /**
    * <p>Performs Principal component analysis on the given Spark <code>RowMatrix</code> with the given number of principle
    * components</p>
    *
    * @param mat                    the matrix to perform PCA on
    * @param numPrincipalComponents the number of principal components
    */
   public static NumericNDArray pca(RowMatrix mat, int numPrincipalComponents) {
      Validation.checkArgument(numPrincipalComponents > 0, "Number of principal components must be > 0");
      return toMatrix(mat.multiply(mat.computePrincipalComponents(numPrincipalComponents)));
   }

   /**
    * Performs Principal component analysis on the given Matrix with the given number of principle components
    *
    * @param mat                    the matrix to perform PCA on
    * @param numPrincipalComponents the number of principal components
    */
   public static NumericNDArray pca(NumericNDArray mat, int numPrincipalComponents) {
      Validation.checkArgument(numPrincipalComponents > 0, "Number of principal components must be > 0");
      return toMatrix(toRowMatrix(mat).computePrincipalComponents(numPrincipalComponents));
   }

   /**
    * Performs Principal component analysis on the given Spark <code>RowMatrix</code> with the given number of principle
    * components
    *
    * @param mat                    the matrix to perform PCA on
    * @param numPrincipalComponents the number of principal components
    */
   public static RowMatrix sparkPCA(RowMatrix mat, int numPrincipalComponents) {
      Validation.checkArgument(numPrincipalComponents > 0, "Number of principal components must be > 0");
      return mat.multiply(mat.computePrincipalComponents(numPrincipalComponents));
   }

   /**
    * Performs Singular Value Decomposition on a Spark <code>RowMatrix</code>
    *
    * @param mat the matrix to perform svd on
    * @param k   the number of singular values
    * @return Thee resulting decomposition
    */
   public static org.apache.spark.mllib.linalg.SingularValueDecomposition<RowMatrix, org.apache.spark.mllib.linalg.Matrix> sparkSVD(RowMatrix mat, int k) {
      Validation.checkArgument(k > 0, "K must be > 0");
      return mat.computeSVD(k, true, 1.0E-9);
   }

   /**
    * Performs Singular Value Decomposition on a Spark <code>RowMatrix</code> returning the decomposition as an array of
    * Apollo matrices in (U,S,V) order.
    *
    * @param mat the matrix to perform svd on
    * @param K   the number of singular values
    * @return Thee resulting decomposition
    */
   public static NumericNDArray[] svd(RowMatrix mat, int K) {
      org.apache.spark.mllib.linalg.SingularValueDecomposition<RowMatrix, org.apache.spark.mllib.linalg.Matrix> svd =
            sparkSVD(mat, K);
      return arrayOf(toMatrix(svd.U()),
                     toDiagonalMatrix(svd.s()),
                     toMatrix(svd.V()));
   }

   /**
    * Performs Singular Value Decomposition on an Apollo Matrix using Spark returning the decomposition as an array of
    * Apollo matrices in (U,S,V) order.
    *
    * @param mat the matrix to perform svd on
    * @param K   the number of singular values
    * @return Thee resulting decomposition
    */
   public static NumericNDArray[] svd(NumericNDArray mat, int K) {
      org.apache.spark.mllib.linalg.SingularValueDecomposition<RowMatrix, org.apache.spark.mllib.linalg.Matrix> svd =
            sparkSVD(toRowMatrix(mat), K);
      return arrayOf(toMatrix(svd.U()),
                     toDiagonalMatrix(svd.s()),
                     toMatrix(svd.V()));
   }

   /**
    * Converts a Spark <code>Matrix</code> to an Apollo <code>NumericNDArray</code>
    *
    * @param v the matrix to convert
    * @return the Apollo matrix
    */
   public static NumericNDArray toDiagonalMatrix(org.apache.spark.mllib.linalg.Vector v) {
      return nd.DFLOAT32.array(DoubleMatrix.diag(new DoubleMatrix(v.toArray())));
   }

   /**
    * Converts a <code>RowMatrix</code> to an Apollo <code>NumericNDArray</code>
    *
    * @param m the matrix to convert
    * @return the Apollo NumericNDArray
    */
   public static NumericNDArray toMatrix(RowMatrix m) {
      final FloatMatrix mprime = new FloatMatrix((int) m.numRows(), (int) m.numCols());
      m.rows()
       .toJavaRDD()
       .zipWithIndex()
       .toLocalIterator()
       .forEachRemaining(t -> mprime.putRow(t._2().intValue(),
                                            MatrixFunctions
                                                  .doubleToFloat(new DoubleMatrix(1, t._1.size(), t._1.toArray()))));
      return nd.DFLOAT32.array(mprime);
   }

   /**
    * Converts a Spark <code>Matrix</code> to an Apollo <code>NumericNDArray</code>
    *
    * @param m the matrix to convert
    * @return the Apollo matrix
    */
   public static NumericNDArray toMatrix(org.apache.spark.mllib.linalg.Matrix m) {
      return nd.DFLOAT32.array(Shape.shape(m.numRows(), m.numCols()), m.toArray());
   }

   public static RowMatrix toRowMatrix(NumericNDArray matrix) {
      JavaRDD<Vector> rdd = StreamingContext
            .distributed()
            .range(0, matrix.shape().rows())
            .map(r -> Vectors.dense(matrix.getAxis(Shape.ROW, r).toDoubleArray()))
            .cache()
            .getRDD();
      return new RowMatrix(rdd.rdd());
   }

   public static RowMatrix toRowMatrix(List<NumericNDArray> vectors) {
      JavaRDD<Vector> rdd = StreamingContext
            .distributed()
            .range(0, vectors.size())
            .map(r -> Vectors.dense(vectors.get(r).toDoubleArray()))
            .cache()
            .getRDD();
      return new RowMatrix(rdd.rdd());
   }

   public static JavaRDD<Vector> toVectors(MStream<NumericNDArray> stream) {
      SparkStream<NumericNDArray> sparkStream = new SparkStream<>(stream);
      return sparkStream.getRDD()
                        .map(v -> (Vector) new org.apache.spark.mllib.linalg.DenseVector(v.toDoubleArray()))
                        .cache();
   }


}// END OF SparkLinearAlgebra
