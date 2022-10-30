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

package com.gengoai.lucene.field.types;

import com.gengoai.LogUtils;
import com.gengoai.collection.Arrays2;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.lucene.LatLong;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@Log
@MetaInfServices
public class LatLongFieldType extends FieldType {
   @Override
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      if (value instanceof LatLong) {
         LatLong latLong = Cast.as(value);
         return Collections.singletonList(new LatLonPoint(outField, latLong.getLatitude(), latLong.getLongitude()));
      }
      throw new IllegalArgumentException("Expecting a LatLong, but received " +
                                         (value == null ? "null" : value.getClass().getName()));
   }

   @Override
   public Query newTermQuery(@NonNull Term term) {
      String[] splits = term.text().split(",");
      if (splits.length < 2) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' into a LatLong.", term.text());
         return super.newTermQuery(term);
      }
      double latitude = Double.parseDouble(splits[0]);
      double longitude = Double.parseDouble(splits[1]);
      double distance = splits.length > 2 ? Double.parseDouble(splits[2]) : 0;
      return LatLonPoint.newDistanceQuery(term.field(), latitude, longitude, distance);
   }

}//END OF LatLongFieldType
