/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.io;

import com.gengoai.SystemInfo;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.StringMatcher;
import com.gengoai.string.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.gengoai.Validation.notNull;
import static com.gengoai.Validation.notNullOrBlank;

/**
 * Common methods for parsing and handling XML files
 *
 * @author David B. Bracewell
 */
public final class Xml {

   /**
    * An EventFilter that ignores character elements that are white space
    */
   public static final EventFilter WHITESPACE_FILTER = event -> !(event.isCharacters() && event.asCharacters()
                                                                                               .isWhiteSpace());

   /**
    * <p>
    * Selects the first node passing a given predicate using a breadth first search
    * </p>
    *
    * @param p     The {@link Predicate} to use for evaluation
    * @param queue The breadth-first queue
    * @return The first {@link Node} to match the predicate or null
    */
   private static Node breadthFirstSearch(Predicate<Node> p, Queue<Node> queue) {
      while(queue.size() > 0) {
         Node n = queue.remove();

         if(p.test(n)) {
            return n;
         }

         NodeList children = n.getChildNodes();
         for(int i = 0; i < children.getLength(); i++) {
            queue.add(children.item(i));
         }
      }

      return null;
   }

   /**
    * Creates an {@link XPath} instance
    *
    * @return An {@link XPath} instance
    */
   public static XPath createXPath() {
      return javax.xml.xpath.XPathFactory.newInstance().newXPath();
   }

   /**
    * Gets the value of an attribute for a node
    *
    * @param n       {@link Node} to get attribute value for
    * @param atrName Name of attribute whose value is desired
    * @param matcher {@link StringMatcher} to match attribute names
    * @return The attribute value or null
    */
   public static String getAttributeValue(Node n, String atrName,
                                          BiPredicate<String, String> matcher) {
      NamedNodeMap attrs = n.getAttributes();
      if(attrs == null) {
         return null;
      }

      String rval = null;
      for(int j = 0; j < attrs.getLength(); j++) {
         String name = attrs.item(j).getNodeName();

         boolean equals = matcher.test(name, atrName);
         if(equals) {
            return attrs.getNamedItem(name).getNodeValue();
         }
      }
      return rval;
   }

   /**
    * Gets the value of an attribute for a node using a case insensitive string matcher
    *
    * @param n       {@link Node} to get attribute value for
    * @param atrName Name of attribute whose value is desired
    * @return The attribute value or null
    */
   public static String getAttributeValue(Node n, String atrName) {
      return getAttributeValue(n, atrName, (s1, s2) -> Strings.safeEquals(s1, s2, false));
   }

   private static Optional<String> getTagName(XMLEvent event) {
      if(event.isStartElement()) {
         return Optional.of(event.asStartElement().getName().getLocalPart());
      }
      if(event.isEndElement()) {
         return Optional.of(event.asEndElement().getName().getLocalPart());
      }
      return Optional.empty();
   }

   /**
    * <p>
    * Gathers the text from the child TEXT_NODE of the given node
    * </p>
    *
    * @param n {@link Node} to get text from
    * @return String with text from the child TEXT_NODE
    */
   public static String getTextContent(Node n) {
      if(n == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();
      short type = n.getNodeType();
      switch(type) {
         case Node.TEXT_NODE:
            rval.append(n.getNodeValue());
            break;
         case Node.CDATA_SECTION_NODE:
            rval.append(n.getNodeValue());
            break;
         default:
            NodeList children = n.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
               Node c = children.item(i);
               if(c.getNodeType() == Node.TEXT_NODE
                     || c.getNodeType() == Node.CDATA_SECTION_NODE) {
                  rval.append(c.getNodeValue());
               }
            }
            break;
      }

      return rval.toString();
   }

   /**
    * <p>
    * Gathers the text from the child TEXT_NODE from each of the nodes in the given collection.
    * </p>
    * <p>
    * A line separator is added between the content of each node
    * </p>
    *
    * @param nodes the nodes
    * @return String with text from the child TEXT_NODE
    */
   public static String getTextContent(Collection<Node> nodes) {
      if(nodes == null) {
         return null;
      }
      StringBuilder rval = new StringBuilder();
      for(Node n : nodes) {
         rval.append(getTextContent(n)).append(SystemInfo.LINE_SEPARATOR);
      }
      return rval.toString();
   }

   /**
    * <p>
    * Gathers the text from the child TEXT_NODE from each of the nodes in the given NodeList.
    * </p>
    * <p>
    * A line separator is added between the content of each node
    * </p>
    *
    * @param nodes the nodes
    * @return String with text from the child TEXT_NODE
    */
   public static String getTextContent(NodeList nodes) {
      if(nodes == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();

      for(int i = 0; i < nodes.getLength(); i++) {
         Node n = nodes.item(i);
         rval.append(getTextContent(n)).append(SystemInfo.LINE_SEPARATOR);
      }

      return rval.toString();
   }

   /**
    * Gets all text from the node and its child nodes
    *
    * @param n {@link Node} to get text from
    * @return String with all text from the node and its children or null
    */
   public static String getTextContentRecursive(Node n) {
      if(n == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();

      short type = n.getNodeType();
      switch(type) {
         case Node.TEXT_NODE:
            rval.append(n.getNodeValue());
            break;
         default:
            NodeList children = n.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
               String text = getTextContentRecursive(children.item(i));
               if(text != null) {
                  rval.append(text);
               }
            }
            break;
      }

      return rval.toString();
   }

   /**
    * Gets all text from all the nodes and their children in a list of nodes
    *
    * @param nl the nl
    * @return String with all text from all the nodes and their children in a list of nodes or null
    */
   public static String getTextContentRecursive(NodeList nl) {

      if(nl == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();
      for(int i = 0; i < nl.getLength(); i++) {
         rval.append(getTextContentRecursive(nl.item(i))).append(SystemInfo.LINE_SEPARATOR);
      }

      if(rval.length() > 0) {
         return rval.toString();
      }

      return null;
   }

   /**
    * Gets all text from all the nodes and their children in a list of nodes
    *
    * @param nl the nl
    * @return String with all text from all the nodes and their children in a list of nodes or null
    */
   public static String getTextContentRecursive(Collection<Node> nl) {

      if(nl == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();
      for(Node n : nl) {
         rval.append(getTextContentRecursive(n)).append(SystemInfo.LINE_SEPARATOR);
      }

      if(rval.length() > 0) {
         return rval.toString();
      }

      return null;
   }

   /**
    * <p>
    * Gathers the text from the child TEXT_NODE and COMMENT_NODE of the given node
    * </p>
    *
    * @param n {@link Node} to get text from
    * @return String with text from the child TEXT_NODE
    */
   public static String getTextContentWithComments(Node n) {
      if(n == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();
      short type = n.getNodeType();
      switch(type) {
         case Node.TEXT_NODE:
            rval.append(n.getNodeValue());
            break;
         case Node.CDATA_SECTION_NODE:
            rval.append(n.getNodeValue());
            break;
         case Node.COMMENT_NODE:
            rval.append(n.getNodeValue());
            break;
         default:
            NodeList children = n.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
               Node c = children.item(i);
               short nt = c.getNodeType();
               if(nt == Node.TEXT_NODE || nt == Node.COMMENT_NODE
                     || nt == Node.CDATA_SECTION_NODE) {
                  rval.append(c.getNodeValue());
               }
            }
            break;
      }

      return rval.toString();
   }

   /**
    * <p>
    * Gathers the text from the child TEXT_NODE and COMMENT_NODE from each of the nodes in the given collection.
    * </p>
    * <p>
    * A line separator is added between the content of each node
    * </p>
    *
    * @param nodes the nodes
    * @return String with text from the child TEXT_NODE and COMMENT_NODE
    */
   public static String getTextContentWithComments(Collection<Node> nodes) {
      if(nodes == null) {
         return null;
      }

      StringBuffer rval = new StringBuffer();

      for(Node n : nodes) {
         rval.append(getTextContentWithComments(n)).append(
               SystemInfo.LINE_SEPARATOR);
      }

      return rval.toString();
   }

   /**
    * <p>
    * Gathers the text from the child TEXT_NODE and COMMENT_NODE from each of the nodes in the given NodeList.
    * </p>
    * <p>
    * A line separator is added between the content of each node
    * </p>
    *
    * @param nodes the nodes
    * @return String with text from the child TEXT_NODE and COMMENT_NODE
    */
   public static String getTextContentWithComments(NodeList nodes) {
      if(nodes == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();
      for(int i = 0; i < nodes.getLength(); i++) {
         Node n = nodes.item(i);
         rval.append(getTextContentWithComments(n)).append(
               SystemInfo.LINE_SEPARATOR);
      }
      return rval.toString();
   }

   /**
    * Gets all text from all the nodes and their children in a list of nodes
    *
    * @param nl the nl
    * @return String with all text from all the nodes and their children in a list of nodes or null
    */
   public static String getTextContentWithCommentsRecursive(NodeList nl) {

      if(nl == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();
      for(int i = 0; i < nl.getLength(); i++) {
         rval.append(getTextContentWithCommentsRecursive(nl.item(i))).append(SystemInfo.LINE_SEPARATOR);
      }
      if(rval.length() > 0) {
         return rval.toString();
      }
      return null;
   }

   /**
    * Gets all text from all the nodes and their children in a list of nodes
    *
    * @param nl the nl
    * @return String with all text from all the nodes and their children in a list of nodes or null
    */
   public static String getTextContentWithCommentsRecursive(Collection<Node> nl) {

      if(nl == null) {
         return null;
      }
      StringBuilder rval = new StringBuilder();
      for(Node n : nl) {
         rval.append(getTextContentWithCommentsRecursive(n)).append(SystemInfo.LINE_SEPARATOR);
      }
      if(rval.length() > 0) {
         return rval.toString();
      }
      return null;
   }

   /**
    * Gets all text from the node and its child nodes
    *
    * @param n {@link Node} to get text from
    * @return String with all text from the node and its children or null
    */
   public static String getTextContentWithCommentsRecursive(Node n) {
      if(n == null) {
         return null;
      }

      StringBuilder rval = new StringBuilder();

      short type = n.getNodeType();
      switch(type) {
         case Node.TEXT_NODE:
            rval.append(n.getNodeValue());
            break;
         case Node.COMMENT_NODE:
            rval.append(n.getNodeValue());
            break;
         default:
            NodeList children = n.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
               String text = getTextContentWithCommentsRecursive(children.item(i));
               if(text != null) {
                  rval.append(text);
               }
            }
            break;
      }

      return rval.toString();
   }

   /**
    * Loads a {@link Document} from a given {@link File}
    *
    * @param f {@link File} containing the XML
    * @return A {@link Document} representing the XML
    * @throws Exception the exception
    */
   public static Document loadXMLFromFile(File f) throws Exception {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
   }

   /**
    * Parses the given XML resource creating sub-documents (DOM) from the elements with the given tag name. An example
    * of usage for this method is constructing DOM documents from each "page" element in the Wikipedia dump.
    *
    * @param xmlResource the xml resource
    * @param tag         the tag to create documents on
    * @return the iterable
    * @throws IOException        the io exception
    * @throws XMLStreamException the xml stream exception
    */
   public static Iterable<Document> parse(Resource xmlResource, String tag) throws IOException, XMLStreamException {
      return parse(xmlResource, tag, null);
   }

   /**
    * Parses the given XML resource creating sub-documents (DOM) from the elements with the given tag name and filtering
    * out events using the given event filter. An example of usage for this method is constructing DOM documents from
    * each "page" element in the Wikipedia dump.
    *
    * @param xmlResource the xml resource
    * @param tag         the tag to create documents on
    * @param eventFilter the event filter
    * @return the iterable
    * @throws IOException        the io exception
    * @throws XMLStreamException the xml stream exception
    */
   public static Iterable<Document> parse(Resource xmlResource,
                                          String tag,
                                          EventFilter eventFilter) throws IOException, XMLStreamException {
      notNull(xmlResource, "Must specify a resource");
      notNullOrBlank(tag, "Must specify a valid xml tag to capture");
      XMLInputFactory factory = XMLInputFactory.newFactory();
      final XMLEventReader reader;
      if(eventFilter == null) {
         reader = factory.createXMLEventReader(xmlResource.inputStream(), "UTF-8");
      } else {
         reader = factory.createFilteredReader(factory.createXMLEventReader(xmlResource.inputStream(), "UTF-8"),
                                               eventFilter);
      }
      return () -> new XmlIterator(tag, reader);
   }

   /**
    * <p>
    * Removes a node by first recursively removing of all of it's children and their children
    * </p>
    *
    * @param n {@link Node} to remove
    */
   public static void removeNodeAndChildren(Node n) {
      NodeList nl = n.getChildNodes();
      if(nl != null) {
         for(int i = 0; i < nl.getLength(); i++) {
            Node c = nl.item(i);
            removeNodeAndChildren(c);
         }
         n.getParentNode().removeChild(n);
      } else {
         n.getParentNode().removeChild(n);
      }
   }

   /**
    * <p>Removes a set of nodes in  a{@link NodeList} from the document.</p>
    *
    * @param nl The nodes to remove
    */
   public static void removeNodes(NodeList nl) {
      if(nl == null) {
         return;
      }
      for(int i = 0; i < nl.getLength(); i++) {
         Node n = nl.item(i);
         removeNodeAndChildren(n);
      }
   }

   /**
    * <p>Removes a set of nodes in a {@link Collection} from the document.</p>
    *
    * @param nl The nodes to remove
    */
   public static void removeNodes(Collection<? extends Node> nl) {
      if(nl == null) {
         return;
      }
      for(Node n : nl) {
         removeNodeAndChildren(n);
      }
   }

   /**
    * Selects all nodes under and including the given node
    *
    * @param n {@link Node} to start at
    * @return A {@link List} of {@link Node} that has the given node and all the nodes under it
    */
   public static List<Node> selectAllNodes(Node n) {
      if(n == null) {
         return null;
      } else {
         List<Node> nodes = new ArrayList<Node>();
         nodes.add(n);

         NodeList children = n.getChildNodes();
         for(int i = 0; i < children.getLength(); i++) {
            List<Node> grandChildren = selectAllNodes(children.item(i));
            if(grandChildren != null) {
               nodes.addAll(grandChildren);
            }
         }

         return nodes;
      }
   }

   /**
    * <p>
    * Selects the first node passing a given predicate using a breadth first search
    * </p>
    *
    * @param n         {@link Node} to start at
    * @param predicate The {@link Predicate} to use for evaluation
    * @return The first {@link Node} to match the predicate or null
    */
   public static Node selectChildNodeBreadthFirst(Node n,
                                                  Predicate<Node> predicate) {
      if(n == null || predicate == null) {
         return null;
      } else {
         Queue<Node> q = new LinkedList<Node>();
         NodeList children = n.getChildNodes();
         for(int i = 0; i < children.getLength(); i++) {
            q.add(children.item(i));
         }
         return breadthFirstSearch(predicate, q);
      }
   }

   /**
    * Selects nodes under and including the given node that are of a given type
    *
    * @param n         {@link Node} to start at
    * @param predicate the predicate
    * @return A {@link List} of {@link Node} that are of the given type
    */
   public static List<Node> selectChildNodes(Node n, Predicate<Node> predicate) {
      if(n == null || predicate == null) {
         return null;
      } else {
         List<Node> nodes = new ArrayList<Node>();

         NodeList children = n.getChildNodes();
         for(int i = 0; i < children.getLength(); i++) {
            List<Node> grandChildren = selectNodes(children.item(i),
                                                   predicate);
            if(grandChildren != null) {
               nodes.addAll(grandChildren);
            }
         }

         return nodes;
      }
   }

   /**
    * <p>
    * Selects nodes under and including the given node that are of a given type
    * </p>
    *
    * @param n         {@link Node} to start at
    * @param predicate The {@link Predicate} to use for evaluation
    * @return A {@link List} of {@link Node} that are of the given type
    */
   public static List<Node> selectNodes(Node n, Predicate<Node> predicate) {
      if(n == null || predicate == null) {
         return null;
      } else {
         List<Node> nodes = new ArrayList<Node>();

         if(predicate.test(n)) {
            nodes.add(n);
         }

         NodeList children = n.getChildNodes();
         for(int i = 0; i < children.getLength(); i++) {
            List<Node> grandChildren = selectNodes(children.item(i),
                                                   predicate);
            if(grandChildren != null) {
               nodes.addAll(grandChildren);
            }
         }

         return nodes;
      }
   }

   /**
    * <p>
    * Creates an {@link Predicate} that evaluates to true if the input node's name is the same as the given node name.
    * String matches are case insensitive
    * </p>
    *
    * @param tagName The name to match
    * @return The {@link Predicate}
    */
   public static Predicate<Node> tagMatchPredicate(String tagName) {
      return n -> Strings.safeEquals(n.getNodeName(), tagName, false);
   }

   /**
    * <p>
    * Creates an {@link Predicate} that evaluates to true if the input node's type is the same as the given node types
    * </p>
    *
    * @param nodeType The {@link Node} type to match
    * @return The {@link Predicate}
    */
   public static Predicate<Node> typeMatchPredicate(short nodeType) {
      return n -> n.getNodeType() == nodeType;
   }

   private Xml() {
      throw new IllegalAccessError();
   }

   private static class XmlIterator implements Iterator<Document> {
      private final MonitoredObject<XMLEventReader> monitoredObject;
      private final XMLEventReader reader;
      private final String tag;
      /**
       * The Document.
       */
      Document document;

      private XmlIterator(String tag, XMLEventReader reader) {
         this.monitoredObject = ResourceMonitor.monitor(reader, r -> {
            try {
               r.close();
            } catch(XMLStreamException e) {
               e.printStackTrace();
            }
         });
         this.tag = tag;
         this.reader = reader;
      }

      private boolean advance() throws Exception {
         if(document != null) {
            return true;
         }
         if(!reader.hasNext()) {
            return false;
         }
         XMLEvent event;
         while(reader.hasNext() && (event = reader.nextEvent()).getEventType() != XMLEvent.END_DOCUMENT) {
            if(getTagName(event).map(n -> n.equals(tag)).orElse(false)) {
               document = DocumentBuilderFactory.newInstance()
                                                .newDocumentBuilder()
                                                .newDocument();
               final XMLEventWriter writer = XMLOutputFactory.newInstance()
                                                             .createXMLEventWriter(new DOMResult(document));
               writer.add(event);
               while(reader.hasNext() && (event = reader.nextEvent()).getEventType() != XMLEvent.END_DOCUMENT) {
                  writer.add(event);
                  if(getTagName(event).map(n -> n.equals(tag)).orElse(false)) {
                     break;
                  }
               }
               writer.close();
               return true;
            }
         }
         return document != null;
      }

      @Override
      public boolean hasNext() {
         try {
            return advance();
         } catch(Exception e) {
            return false;
         }
      }

      @Override
      public Document next() {
         try {
            advance();
         } catch(Exception e) {
            throw new RuntimeException(e);
         }
         Document toReturn = document;
         document = null;
         return toReturn;
      }
   }

}//END OF Xml
