package com.gengoai.apollo.math.linalg;

import com.gengoai.Validation;
import com.gengoai.stream.MStream;
import com.gengoai.stream.spark.SparkStream;
import com.gengoai.stream.StreamingContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.jblas.DoubleMatrix;

import java.util.List;

/**
 * Convenience methods for working Spark's linear algebra structures and methods
 *
 * @author David B. Bracewell
 */
public final class SparkLinearAlgebra {

   private SparkLinearAlgebra() {
      throw new IllegalAccessError();
   }

   /**
    * Performs Principal component analysis on the given Spark <code>RowMatrix</code> with the given number of principle
    * components
    *
    * @param mat                    the matrix to perform PCA on
    * @param numPrincipalComponents the number of principal components
    */
   public static NDArray pca(RowMatrix mat, int numPrincipalComponents) {
      Validation.checkArgument(numPrincipalComponents > 0, "Number of principal components must be > 0");
      return toMatrix(mat.multiply(mat.computePrincipalComponents(numPrincipalComponents)));
   }

   /**
    * Performs Principal component analysis on the given Matrix with the given number of principle components
    *
    * @param mat                    the matrix to perform PCA on
    * @param numPrincipalComponents the number of principal components
    */
   public static NDArray pca(NDArray mat, int numPrincipalComponents) {
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
   public static NDArray[] svd(RowMatrix mat, int K) {
      org.apache.spark.mllib.linalg.SingularValueDecomposition<RowMatrix, org.apache.spark.mllib.linalg.Matrix> svd = sparkSVD(
         mat, K);
      return new NDArray[]{toMatrix(svd.U()), toDiagonalMatrix(svd.s()), toMatrix(svd.V())};
   }

   /**
    * Performs Singular Value Decomposition on an Apollo Matrix using Spark returning the decomposition as an array of
    * Apollo matrices in (U,S,V) order.
    *
    * @param mat the matrix to perform svd on
    * @param K   the number of singular values
    * @return Thee resulting decomposition
    */
   public static NDArray[] svd(NDArray mat, int K) {
      org.apache.spark.mllib.linalg.SingularValueDecomposition<RowMatrix, org.apache.spark.mllib.linalg.Matrix> svd = sparkSVD(
         toRowMatrix(mat), K);
      return new NDArray[]{toMatrix(svd.U()), toDiagonalMatrix(svd.s()), toMatrix(svd.V())};
   }

   /**
    * Converts a Spark vector into a diagonal Apollo matrix
    *
    * @param v the vector to convert
    * @return the diagonal matrix
    */
   public static NDArray toDiagonalMatrix(org.apache.spark.mllib.linalg.Vector v) {
      return new DenseMatrix(DoubleMatrix.diag(new DoubleMatrix(v.toArray())));
   }

   /**
    * Converts a <code>RowMatrix</code> to an Apollo <code>DenseMatrix</code>
    *
    * @param m the matrix to convert
    * @return the Apollo matrix
    */
   public static NDArray toMatrix(RowMatrix m) {
      final DoubleMatrix mprime = new DoubleMatrix((int) m.numRows(), (int) m.numCols());
      m.rows()
       .toJavaRDD()
       .zipWithIndex()
       .toLocalIterator()
       .forEachRemaining(t -> mprime.putRow(t._2().intValue(), new DoubleMatrix(1, t._1.size(), t._1.toArray())));
      return new DenseMatrix(mprime);
   }

   /**
    * Converts a Spark <code>Matrix</code> to an Apollo <code>DenseMatrix</code>
    *
    * @param m the matrix to convert
    * @return the Apollo matrix
    */
   public static NDArray toMatrix(org.apache.spark.mllib.linalg.Matrix m) {
      return NDArrayFactory.DENSE.array(m.numRows(), m.numCols(), m.toArray());
   }

   public static RowMatrix toRowMatrix(NDArray matrix) {
      JavaRDD<Vector> rdd = StreamingContext
                               .distributed()
                               .range(0, matrix.rows())
                               .map(r -> Vectors.dense(matrix.getRow(r).toDoubleArray()))
                               .cache()
                               .getRDD();
      return new RowMatrix(rdd.rdd());
   }

   public static RowMatrix toRowMatrix(List<NDArray> vectors) {
      JavaRDD<Vector> rdd = StreamingContext
                               .distributed()
                               .range(0, vectors.size())
                               .map(r -> Vectors.dense(vectors.get(r).toDoubleArray()))
                               .cache()
                               .getRDD();
      return new RowMatrix(rdd.rdd());
   }

   public static JavaRDD<Vector> toVectors(MStream<NDArray> stream) {
      SparkStream<NDArray> sparkStream = new SparkStream<>(stream);
      return sparkStream.getRDD()
                        .map(v -> (Vector) new org.apache.spark.mllib.linalg.DenseVector(v.toDoubleArray()))
                        .cache();
   }


}// END OF SparkLinearAlgebra
