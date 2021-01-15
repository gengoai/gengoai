package com.gengoai.graph.io;

import com.gengoai.collection.multimap.Multimap;
import com.gengoai.graph.Graph;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * @author David B. Bracewell
 */
public class GraphJson<V> implements GraphReader<V>, GraphWriter<V> {
   private final Type vertexClass;

   public GraphJson(Type vertexClass) {
      this.vertexClass = vertexClass;
   }

   @Override
   public void setVertexDecoder(VertexDecoder<V> vertexDecoder) {

   }

   @Override
   public void setEdgeDecoder(EdgeDecoder<V> edgeDecoder) {

   }

   @Override
   public Graph<V> read(Resource location) throws IOException {
      return Json.parse(location).as(parameterizedType(Graph.class, vertexClass));
   }

   @Override
   public void setVertexEncoder(VertexEncoder<V> encoder) {

   }

   @Override
   public void setEdgeEncoder(EdgeEncoder<V> encoder) {

   }

   @Override
   public void write(Graph<V> graph, Resource location, Multimap<String, String> parameters) throws IOException {
      Json.dump(graph, location);
   }

}//END OF GraphJson
