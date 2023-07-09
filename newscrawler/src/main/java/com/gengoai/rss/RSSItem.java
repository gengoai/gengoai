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

package com.gengoai.rss;

import com.gengoai.collection.Iterables;
import com.gengoai.io.Xml;
import com.gengoai.string.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.w3c.dom.Document;

import java.io.Serializable;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RSSItem implements Serializable {
   String title;
   String pubdate;
   String link;
   String source;
   String guid;
   String description;

   public static RSSItem from(Document document) {
      return new RSSItem(getString(document, "title"),
                         getString(document, "pubDate"),
                         getString(document, "link"),
                         getString(document, "source"),
                         getString(document, "guid"),
                         getString(document, "description"));
   }

   private static String getString(Document document, String node) {
      return Iterables.getFirst(Xml.selectChildNodes(document, n -> n.getNodeName().equals(node)))
                      .map(Xml::getTextContentRecursive)
                      .orElse(Strings.EMPTY);
   }

}
