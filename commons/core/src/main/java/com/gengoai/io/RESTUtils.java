package com.gengoai.io;

import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import lombok.Data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

public class RESTUtils {
   public static final String APPLICATION_JSON = "application/json";

   public static <T> T getJson(String url,
                               Type type) throws IOException {
      return Json.parse(getJson(url), type);
   }

   public static <T> T getJson(String url,
                               Type type,
                               Map<String, String> headers) throws IOException {
      return Json.parse(getJson(url, headers), type);
   }

   public static String getJson(String url) throws IOException {
      return getJson(url, Map.of());
   }

   public static String getJson(String url, Map<String, String> headers) throws IOException {
      URL serverUrl = null;
      try {
         serverUrl = new URL(url);
      } catch (MalformedURLException e) {
         throw new RuntimeException(e);
      }
      HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
      try {
         connection.setRequestMethod("GET");
      } catch (ProtocolException e) {
         throw new RuntimeException(e);
      }
      connection.setDoOutput(false);
      connection.setDoInput(true);
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(true);
      connection.setRequestProperty("Content-Type", APPLICATION_JSON);
      if (headers != null) {
         for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
         }
      }
      connection.setRequestProperty("Accept", APPLICATION_JSON);

      Resource response = Resources.fromInputStream(connection.getInputStream());
      return response.readToString();
   }

   public static void main(String[] args) {
      try {
         String url = "http://localhost:9191/add/";
         Object postData = Map.<String, Object>of(
               "name", "Moby Dick",
               "price", 23.0f,
               "is_offer", false
                                                 );
         Item response = postJson(url, postData, Item.class);
         System.out.println(response);


         response = getJson("http://localhost:9191/items/?id=5", Item.class);
         System.out.println(response);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static <T> T postJson(String url,
                                Object postData,
                                Type type) throws IOException {
      return Json.parse(postJson(url, postData), type);
   }

   public static <T> T postJson(String url,
                                Object postData,
                                Type type,
                                Map<String, String> headers) throws IOException {
      return Json.parse(postJson(url, postData, headers), type);
   }

   public static String postJson(String url,
                                 Object postData,
                                 Map<String, String> headers) throws IOException {
      URL serverUrl = null;
      try {
         serverUrl = new URL(url);
      } catch (MalformedURLException e) {
         throw new RuntimeException(e);
      }
      HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
      try {
         connection.setRequestMethod("POST");
      } catch (ProtocolException e) {
         throw new RuntimeException(e);
      }
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(true);
      connection.setRequestProperty("Content-Type", APPLICATION_JSON);
      if (headers != null) {
         for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
         }
      }
      connection.setRequestProperty("Accept", APPLICATION_JSON);

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(Json.dumps(postData));
      writer.close();

      Resource response = Resources.fromInputStream(connection.getInputStream());
      return response.readToString();
   }

   public static String postJson(String url,
                                 Object postData) throws IOException {
      return postJson(url, postData, Map.of());
   }

   @Data
   public static class Item {
      public String name;
      public float price;
      public boolean is_offer;
   }

}
