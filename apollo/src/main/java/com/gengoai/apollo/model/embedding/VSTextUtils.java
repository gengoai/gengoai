/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.apollo.model.embedding;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

public final class VSTextUtils {

    private VSTextUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Convert line to vector nd array.
     *
     * @param line      the line
     * @param dimension the dimension
     * @return the nd array
     */
    public static NumericNDArray convertLineToVector(String line, int dimension) {
        NumericNDArray vector = nd.DFLOAT32.zeros(1, dimension);
        String[] parts = line.split("[ \t]+");
        if (parts.length < dimension + 1) {
            throw new IllegalStateException("Invalid Line: " + line);
        }
        for (int i = 1; i < parts.length; i++) {
            vector.set(i - 1, Double.parseDouble(parts[i]));
        }
        vector.setLabel(parts[0].replace('_', ' '));
        return vector;
    }

    /**
     * Determine dimension int.
     *
     * @param r the r
     * @return the int
     * @throws IOException the io exception
     */
    public static int determineDimension(Resource r) throws IOException {
        try (BufferedReader reader = new BufferedReader(r.reader())) {
            while (true) {
                String line = reader.readLine();
                if (Strings.isNullOrBlank(line)) {
                    throw new IOException("Unexpected empty line at beginning of file: " + r);
                } else if (line.startsWith("#")) {
                    continue;
                }
                String[] cells = line.trim().split("[ \t]+");
                if (cells.length > 4) {
                    return cells.length - 1;
                }
                return Integer.parseInt(cells[1]);
            }
        }
    }

    /**
     * Determine unknown word string.
     *
     * @param r the r
     * @return the string
     * @throws IOException the io exception
     */
    public static String determineUnknownWord(Resource r) throws IOException {
        try (BufferedReader reader = new BufferedReader(r.reader())) {
            String line = reader.readLine();
            if (Strings.isNullOrBlank(line)) {
                throw new IOException("Unexpected empty line at beginning of file: " + r);
            } else if (line.startsWith("#")) {
                return line.substring(1).strip();
            }
            return null;
        }
    }

    /**
     * Vector to line string.
     *
     * @param vec the vec
     * @return the string
     */
    public static String vectorToLine(NumericNDArray vec) {
        double[] array = vec.toDoubleArray();
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%.3f", array[0]));
        for (int i = 1; i < array.length; i++) {
            builder.append(" ").append(String.format("%.3f", array[i]));
        }
        return builder.toString();
    }





}//END OF VSTextUtils
